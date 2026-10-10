#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float SkillProgress;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float threshold = texture(Sampler1, texCoord0).r;
    vec4 circleColor = texture(Sampler0, texCoord0);
    float visible = SkillProgress > threshold ? 1.0 : 0.0;
    fragColor = circleColor * vertexColor * visible;
}
