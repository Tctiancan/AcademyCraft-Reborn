package org.academy.api.client.render;

import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

/**
 * std140 序列化构建器, 移植自 26.2 `Std140Builder`. 纯数据层, 不依赖 GPU 后端.
 *
 * 迁自 Kotlin 源文件 Std140.kt (Java↔Kotlin 编译顺序解耦所需的原生重写).
 */
public final class Std140Builder {
    private final ByteBuffer buffer;
    private final int start;

    private Std140Builder(ByteBuffer buffer) {
        this.buffer = buffer;
        this.start = buffer.position();
    }

    public static Std140Builder intoBuffer(ByteBuffer buffer) {
        return new Std140Builder(buffer);
    }

    public static Std140Builder onStack(MemoryStack stack, int size) {
        return new Std140Builder(stack.malloc(size));
    }

    /** 分配一块指定大小的缓冲, 兼容 26.2 `allocate`. */
    public static Std140Builder allocate(int size) {
        return new Std140Builder(ByteBuffer.allocate(size));
    }

    /** 将当前缓冲置为可读并拷贝剩余字节, 兼容 26.2 `toArray`. */
    public byte[] toArray() {
        buffer.flip();
        byte[] out = new byte[buffer.remaining()];
        buffer.get(out);
        return out;
    }

    public ByteBuffer get() {
        return buffer.flip();
    }

    public Std140Builder align(int alignment) {
        int position = buffer.position();
        buffer.position(start + Std140Util.roundToward(position - start, alignment));
        return this;
    }

    public Std140Builder putFloat(float value) {
        align(4);
        buffer.putFloat(value);
        return this;
    }

    public Std140Builder putInt(int value) {
        align(4);
        buffer.putInt(value);
        return this;
    }

    public Std140Builder putVec2(float x, float y) {
        align(8);
        buffer.putFloat(x);
        buffer.putFloat(y);
        return this;
    }

    public Std140Builder putVec2(Vector2fc vec) {
        align(8);
        vec.get(buffer);
        buffer.position(buffer.position() + 8);
        return this;
    }

    public Std140Builder putIVec2(int x, int y) {
        align(8);
        buffer.putInt(x);
        buffer.putInt(y);
        return this;
    }

    public Std140Builder putIVec2(Vector2ic vec) {
        align(8);
        vec.get(buffer);
        buffer.position(buffer.position() + 8);
        return this;
    }

    public Std140Builder putVec3(float x, float y, float z) {
        align(16);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.position(buffer.position() + 4);
        return this;
    }

    public Std140Builder putVec3(Vector3fc vec) {
        align(16);
        vec.get(buffer);
        buffer.position(buffer.position() + 16);
        return this;
    }

    public Std140Builder putIVec3(int x, int y, int z) {
        align(16);
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(z);
        buffer.position(buffer.position() + 4);
        return this;
    }

    public Std140Builder putIVec3(Vector3ic vec) {
        align(16);
        vec.get(buffer);
        buffer.position(buffer.position() + 16);
        return this;
    }

    public Std140Builder putVec4(float x, float y, float z, float w) {
        align(16);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(w);
        return this;
    }

    public Std140Builder putVec4(Vector4fc vec) {
        align(16);
        vec.get(buffer);
        buffer.position(buffer.position() + 16);
        return this;
    }

    public Std140Builder putIVec4(int x, int y, int z, int w) {
        align(16);
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(z);
        buffer.putInt(w);
        return this;
    }

    public Std140Builder putIVec4(Vector4ic vec) {
        align(16);
        vec.get(buffer);
        buffer.position(buffer.position() + 16);
        return this;
    }

    public Std140Builder putMat4f(Matrix4fc mat) {
        align(16);
        mat.get(buffer);
        buffer.position(buffer.position() + 64);
        return this;
    }
}
