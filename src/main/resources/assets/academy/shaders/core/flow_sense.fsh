#version 150

in vec2 uv;
in vec4 vColor;

out vec4 fragColor;

void main() {
    // 空心棱形：两轴 UV 均在 [-1,1]，|x|+|y|=0.78 形成菱形外轮廓，其余区域透明。
    float edge = abs(uv.x) + abs(uv.y) - 0.78;
    float aa = max(fwidth(edge), 0.001);
    float outline = 1.0 - smoothstep(0.6 * aa, 1.6 * aa, abs(edge));
    fragColor = vec4(vColor.rgb, vColor.a * outline);
}
