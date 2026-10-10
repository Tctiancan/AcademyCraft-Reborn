#version 150

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

in vec3 Position;
in vec2 UV0;

out vec2 texCoord0;
out vec3 viewPosition;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;
    texCoord0 = UV0;
    viewPosition = view.xyz;
}
