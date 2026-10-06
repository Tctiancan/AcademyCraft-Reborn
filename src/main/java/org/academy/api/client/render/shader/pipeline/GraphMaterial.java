package org.academy.api.client.render.shader.pipeline;

import org.academy.api.client.render.graph.model.GraphParameter;
import org.academy.api.client.render.graph.type.Value;
import org.academy.api.client.render.graph.type.ValueType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行时材质：持有图参数值（默认值初始化，可逐项覆盖），可写入 std140 uniform 块。
 */
public final class GraphMaterial {
    private final UniformLayout layout;
    private final List<GraphParameter> parameters;
    private final Map<String, Value> values = new LinkedHashMap<>();

    public GraphMaterial(UniformLayout layout, List<GraphParameter> parameters) {
        this.layout = layout;
        this.parameters = List.copyOf(parameters);
        for (var p : parameters) {
            values.put(p.id(), p.defaultValue());
        }
    }

    public void set(String parameterId, Value value) {
        if (!values.containsKey(parameterId)) {
            throw new IllegalArgumentException("unknown parameter: " + parameterId);
        }
        values.put(parameterId, value);
    }

    public Value get(String parameterId) {
        var value = values.get(parameterId);
        if (value == null) {
            throw new IllegalArgumentException("unknown parameter: " + parameterId);
        }
        return value;
    }

    public Map<String, Value> values() {
        return Map.copyOf(values);
    }

    public UniformLayout layout() {
        return layout;
    }

    /**
     * 图所需的纹理绑定槽位（来自布局，含 texture.sample 属性与 SAMPLER 参数）。
     */
    public List<SamplerBinding> samplerBindings() {
        return layout.samplers();
    }

    /**
     * C-stun: 26.2 {@code Std140Builder} std140 uniform 序列化在 1.21.1 暂不移植。
     * 保留方法签名，实现为空占位。
     */
    public void write(Object builder, float time) {
        // C-stun: std140 uniform 写入暂不移植。
    }

    private static boolean isUniformType(ValueType type) {
        return switch (type) {
            case FLOAT, INT, VEC2, VEC3, VEC4, COLOR -> true;
            default -> false;
        };
    }
}
