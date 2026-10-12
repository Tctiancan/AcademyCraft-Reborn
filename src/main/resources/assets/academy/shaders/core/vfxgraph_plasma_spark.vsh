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
    vec2 velocity = (ModelViewMat * vec4(InstanceVel, 0.0)).xy;
    float speed = length(velocity);
    vec2 tangent = speed > 0.001 ? velocity / speed : vec2(0.0, 1.0);
    vec2 normal = vec2(-tangent.y, tangent.x);
    viewPos.xy += (tangent * off.y * (2.0 + min(speed * 0.18, 3.0)) + normal * off.x * 0.55) * InstanceSize;
    gl_Position = ProjMat * viewPos;
    texCoord = Position.xy;
    vertexColor = InstanceColor;
}
