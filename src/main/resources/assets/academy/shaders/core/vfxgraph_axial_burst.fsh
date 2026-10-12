#version 150

uniform float ArcEmission;
uniform float ArcBloom;

in vec3 vNormal;
in vec2 texCoord0;
in vec4 vColor;
in vec3 vViewDir;
out vec4 fragColor;

void main() {
    vec3 normal = normalize(vNormal);
    float facing = abs(dot(normal, normalize(vViewDir)));
    // Warm plasma must remain visible at grazing angles when seen along its axis.
    // Keep the blue discharge's sharper edge falloff and the existing side-view brightness.
    float plasma = smoothstep(0.0, 0.35, vColor.r - vColor.b);
    float edge = mix(pow(facing, 1.1), 0.20 + 0.80 * pow(facing, 0.85), plasma);
    float alpha = vColor.a * edge;
    vec3 color = vColor.rgb * ArcEmission;
    // A restrained circumferential gradient separates the translucent cylindrical shells.
    float roundness = 0.82 + 0.18 * max(dot(normal, normalize(vec3(0.35, 0.8, 0.3))), 0.0);
    color *= mix(1.0, roundness, plasma);
    if (ArcBloom > 0.5) color *= alpha;
    fragColor = vec4(color, alpha);
}
