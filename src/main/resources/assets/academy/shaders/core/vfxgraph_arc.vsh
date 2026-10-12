#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec3 Normal;
in vec2 UV0;
in vec4 Color;

out vec3 vNormal;
out vec2 texCoord0;
out vec4 vColor;
out vec3 vViewDir;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;
    vNormal = mat3(ModelViewMat) * Normal;
    texCoord0 = UV0;
    vColor = Color;
    // Position 已由 CPU 转为相机相对坐标 (world - camPos), 模型视图为纯旋转矩阵;
    // 故 -viewPosition.xyz 即碎片到相机的视图方向 (view-space rim 用它与法线点积计算)。
    vViewDir = normalize(-viewPosition.xyz);
}
