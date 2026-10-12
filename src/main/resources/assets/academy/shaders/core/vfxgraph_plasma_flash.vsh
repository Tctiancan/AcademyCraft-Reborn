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

void main() {
    vec4 viewPos = ModelViewMat * vec4(InstancePos, 1.0);
    vec2 off = Position.xy * 2.0 - 1.0;
    viewPos.xy += off * InstanceSize;
    gl_Position = ProjMat * viewPos;
    texCoord = Position.xy;
    vertexColor = InstanceColor;
}
