#version 150

uniform sampler2D Sampler1;

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    float streak = exp(-p.x * p.x * 8.0) * (1.0 - smoothstep(0.25, 1.0, abs(p.y)));
    float alpha = vertexColor.a * streak;
    if (alpha < 0.002) discard;
    vec3 color = mix(vertexColor.rgb, vec3(1.0), streak * 0.62);
    fragColor = vec4(color * alpha, alpha);
}
