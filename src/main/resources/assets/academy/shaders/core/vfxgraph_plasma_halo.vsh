#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec3 InstancePos;
in vec3 InstanceVel;
in float InstanceSize;
in vec4 InstanceColor;
in float InstanceRot;
in float InstanceSeed;
in float InstanceAge;

out vec2 texCoord;
out vec4 vertexColor;
out float plasmaTime;

void main() {
    vec4 viewPos = ModelViewMat * vec4(InstancePos, 1.0);
    vec2 off = Position.xy * 2.0 - 1.0;
    plasmaTime = InstanceAge * 120.0;
    float breath = 1.0 + sin(plasmaTime * 0.68) * 0.018;
    viewPos.xy += off * InstanceSize * breath;
    gl_Position = ProjMat * viewPos;
    texCoord = Position.xy;
    vertexColor = InstanceColor;
}
