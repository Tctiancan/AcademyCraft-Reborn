#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in float Flow;
in vec2 MaterialUv;

out vec4 vertexColor;
out float flowCoord;
out vec2 materialUv;
out vec4 portalProjection;

void main() {
    vec4 clipPosition = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = clipPosition;
    vertexColor = Color;
    flowCoord = Flow;
    materialUv = MaterialUv;
    // 从裁剪空间推导端门投影坐标 (对齐原版 end_portal 的 projection_from_position 语义,
    // 1.21.1 纯程序化端门配色只依赖其连续性)。
    portalProjection = vec4(clipPosition.xyz * 0.5 + vec3(clipPosition.w * 0.25), clipPosition.w);
}
