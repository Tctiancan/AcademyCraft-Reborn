#version 330
precision highp float;

#define MAX_FIELDS 16

// 1.21.1 单程合并版 (原 26.2 directional_field_surface/volume/composite 三阶段 + GPU UBO).
// 26.2 依赖 WorldSurfaceMasks 生成的不透明/透明/实体遮罩与 std140 UBO; 本移植无对应 GPU 延迟
// 重建路径, 改为从主帧缓冲深度纹理重建世界位置, 以普通 uniform 数组携带最多 16 个方向场,
// 一趟同时计算表面覆盖与空气散射 (光线束), 输出半透明白色散射色调, 由 DirectionalFieldRenderer
// 以 screen_blit 复用半透混合合成回主帧缓冲。
uniform sampler2D SceneDepth;
uniform mat4 InverseViewProjection;
uniform vec4 SunLight;         // xyz=太阳方向(归一化), w=环境亮度 0..1
uniform vec4 FieldParams;      // x=时间(秒), y=场数量
uniform vec4 NoiseAnchor;      // xyz=相机相对稳定噪声锚点
uniform vec4 FieldOrigins[MAX_FIELDS];     // xyz=原点(相机相对), w=包络半径
uniform vec4 FieldDirections[MAX_FIELDS];  // xyz=方向(归一化), w=空气可见度 0..1
uniform vec4 FieldCones[MAX_FIELDS];       // x=first.radius y=first.minDot z=second.radius w=second.minDot

in vec2 texCoord;
out vec4 fragColor;

vec3 reconstruct(vec2 uv, float depth) {
    vec4 p = InverseViewProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / max(abs(p.w), 1e-8) * sign(p.w);
}

float sector(vec3 d, vec3 axis, float radius, float minimumDot) {
    float distance = length(d);
    if (radius <= 0.0 || distance > radius) return 0.0;
    float side = dot(d, axis) - distance * minimumDot;
    return smoothstep(0.0, 0.22, min(radius - distance, side));
}

float fieldAt(vec3 p, int i) {
    vec3 d = p - FieldOrigins[i].xyz;
    vec4 cones = FieldCones[i];
    return max(sector(d, FieldDirections[i].xyz, cones.x, cones.y),
               sector(d, FieldDirections[i].xyz, cones.z, cones.w));
}

float unionAt(vec3 p) {
    float result = 0.0;
    for (int i = 0; i < MAX_FIELDS; i++) {
        if (i >= int(FieldParams.y)) break;
        result = max(result, fieldAt(p, i));
    }
    return result;
}

vec2 hash2(vec2 p) { return fract(sin(p * vec2(127.1, 311.7) + vec2(269.5, 183.3)) * 43758.5453); }

float fieldNoise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    vec2 a = hash2(i);
    vec2 b = hash2(i + vec2(1, 0));
    vec2 c = hash2(i + vec2(0, 1));
    vec2 d = hash2(i + vec2(1, 1));
    vec2 xblend = mix(a, b, f.x);
    vec2 yblend = mix(c, d, f.x);
    vec2 value = mix(xblend, yblend, f.y);
    return dot(value, vec2(1.0, -1.0)) * 0.5 + 0.5;
}

void main() {
    float depth = texture(SceneDepth, texCoord).r;
    bool hasDepth = depth > 1e-7;
    float limit = hasDepth ? length(reconstruct(texCoord, depth)) : 100000.0;

    // 表面覆盖度: 重建场景表面处的场值, 与 26.2 surface 阶段一致。
    float surface = hasDepth ? unionAt(reconstruct(texCoord, depth)) : 0.0;

    // 空气散射体积: 沿视线对每个场球的球穿越区间做步进积分, 叠加光线束密度。
    vec3 ray = normalize(reconstruct(texCoord, 0.5));
    vec3 sun = normalize(SunLight.xyz);
    vec3 tangent = normalize(cross(abs(sun.y) > 0.95 ? vec3(1, 0, 0) : vec3(0, 1, 0), sun));
    vec3 bitangent = cross(sun, tangent);
    float volume = 0.0;
    for (int i = 0; i < MAX_FIELDS; i++) {
        if (i >= int(FieldParams.y)) break;
        vec3 center = FieldOrigins[i].xyz;
        float radius = FieldOrigins[i].w;
        if (radius <= 0.0) continue;
        float b = dot(ray, center);
        float discriminant = b * b - dot(center, center) + radius * radius;
        if (discriminant <= 0.0) continue;
        float root = sqrt(discriminant);
        float start = max(0.15, b - root), end = min(limit, b + root);
        if (end <= start) continue;
        float stepSize = (end - start) / 16.0;
        float density = 0.0;
        for (int s = 0; s < 16; s++) {
            float distance = start + (float(s) + 0.5) * stepSize;
            vec3 p = ray * distance;
            float coverage = fieldAt(p, i);
            if (coverage <= 0.0) continue;
            vec3 world = p + NoiseAnchor.xyz;
            vec2 band = vec2(dot(world, tangent), dot(world, bitangent));
            float shafts = fieldNoise(band * 0.7 + vec2(FieldParams.x * 0.014, 0.0));
            shafts = 0.12 + 0.88 * smoothstep(0.3, 0.8, shafts);
            density += coverage * shafts * stepSize;
        }
        float angle = 0.75 + 0.25 * pow(abs(dot(ray, sun)), 4.0);
        float amount = (1.0 - exp(-density * 0.11)) * angle * FieldDirections[i].w;
        volume = max(volume, amount);
    }

    float illumination = 0.78 + SunLight.w * 0.22;
    float surfMix = surface * illumination * (0.94 + 0.06 * sin(FieldParams.x * 1.7));
    float volMix = volume * (0.6 + SunLight.w * 0.4);
    // 26.2 composite 的色调: 细微偏白散射光, 峰值 alpha 0.30, 半透明叠加场景。
    float alpha = min(0.30, surfMix * 0.17 + volMix * 0.42);
    if (alpha < 0.001) discard;
    fragColor = vec4(0.91, 0.92, 0.94, alpha);
}
