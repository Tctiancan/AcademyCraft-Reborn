#version 150

uniform float ArcEmission;
uniform float ArcBloom;

in vec3 vNormal;
in vec2 texCoord0;
in vec4 vColor;
in vec3 vViewDir;
out vec4 fragColor;
void main() {
    float facing = abs(dot(normalize(vNormal), normalize(vViewDir)));
    float edge = pow(facing, 1.8);
    float alpha = vColor.a * edge;
    vec3 color = vColor.rgb * ArcEmission;
    // The bloom target adds RGB directly; the world pass uses straight alpha.
    if (ArcBloom > 0.5) color *= alpha;
    fragColor = vec4(color, alpha);
}
