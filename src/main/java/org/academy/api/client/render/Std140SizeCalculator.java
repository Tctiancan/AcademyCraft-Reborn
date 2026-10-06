package org.academy.api.client.render;

/**
 * std140 尺寸计算器, 移植自 26.2 `Std140SizeCalculator`. 纯数据层, 不依赖 GPU 后端.
 *
 * 迁自 Kotlin 源文件 Std140.kt (Java↔Kotlin 编译顺序解耦所需的原生重写).
 */
public final class Std140SizeCalculator {
    private int size;

    public int get() {
        return size;
    }

    public Std140SizeCalculator align(int alignment) {
        size = Std140Util.roundToward(size, alignment);
        return this;
    }

    public Std140SizeCalculator putFloat() {
        align(4);
        size += 4;
        return this;
    }

    public Std140SizeCalculator putInt() {
        align(4);
        size += 4;
        return this;
    }

    public Std140SizeCalculator putVec2() {
        align(8);
        size += 8;
        return this;
    }

    public Std140SizeCalculator putIVec2() {
        align(8);
        size += 8;
        return this;
    }

    public Std140SizeCalculator putVec3() {
        align(16);
        size += 16;
        return this;
    }

    public Std140SizeCalculator putIVec3() {
        align(16);
        size += 16;
        return this;
    }

    public Std140SizeCalculator putVec4() {
        align(16);
        size += 16;
        return this;
    }

    public Std140SizeCalculator putIVec4() {
        align(16);
        size += 16;
        return this;
    }

    public Std140SizeCalculator putMat4f() {
        align(16);
        size += 64;
        return this;
    }

    /** 构造一个已按 initialSize 预设大小(字节)的尺寸计算器, 兼容 26.2 `prefilled`. */
    public static Std140SizeCalculator prefilled(int initialSize) {
        Std140SizeCalculator c = new Std140SizeCalculator();
        c.size = initialSize;
        return c;
    }
}
