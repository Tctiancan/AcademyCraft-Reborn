#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord0;
in vec3 viewPosition;

out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash(i), hash(i + vec3(1.0, 0.0, 0.0)), f.x),
            mix(hash(i + vec3(0.0, 1.0, 0.0)), hash(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
        mix(mix(hash(i + vec3(0.0, 0.0, 1.0)), hash(i + vec3(1.0, 0.0, 1.0)), f.x),
            mix(hash(i + vec3(0.0, 1.0, 1.0)), hash(i + vec3(1.0, 1.0, 1.0)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; ++i) {
        value += amplitude * noise(p);
        p *= 2.03;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    // 模型剪影 alpha 裁剪: 仅保留实体贴图不透明的部分。
    if (texture(Sampler0, texCoord0).a < 0.1) discard;

    float time = GameTime * 1200.0;
    vec3 p = vec3(texCoord0 * 14.0, time * 0.12);
    p.y -= time * 0.3;
    float smoke = fbm(p) * 0.7 + fbm(p * 2.4 + 0.13) * 0.3;

    // view-space 法线, 用于 rim 光。
    vec3 normal = normalize(cross(dFdx(viewPosition), dFdy(viewPosition)));
    float rim = pow(1.0 - abs(dot(normal, normalize(-viewPosition))), 2.0);

    float light = clamp(smoke - 0.2, 0.0, 1.0);
    vec3 base = vec3(0.05, 0.68, 0.42);
    vec3 bright = vec3(0.62, 1.0, 0.80);
    vec3 color = mix(base, bright, light) * (0.55 + rim * 0.7);

    float alpha = 0.16 + light * 0.5 + rim * 0.24;
    fragColor = vec4(color, clamp(alpha, 0.0, 1.0));
}
