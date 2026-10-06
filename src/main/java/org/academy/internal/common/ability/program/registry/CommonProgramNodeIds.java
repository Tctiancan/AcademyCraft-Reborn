package org.academy.internal.common.ability.program.registry;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

/**
 * Stable identifiers for the shared, ability-category-independent node algebra.
 */
public final class CommonProgramNodeIds {
    public static final ResourceLocation SCALAR_CONSTANT = id("value/scalar");
    public static final ResourceLocation BOOLEAN_CONSTANT = id("value/boolean");
    public static final ResourceLocation INTEGER_CONSTANT = id("value/integer");
    public static final ResourceLocation FLOAT_CONSTANT = id("value/float");
    public static final ResourceLocation TEXT_CONSTANT = id("value/text");
    public static final ResourceLocation TEXT_COMPARE = id("logic/text/compare");
    public static final ResourceLocation TEXT_SPLIT = id("logic/text/split");
    public static final ResourceLocation TEXT_TO_VEC3 = id("spatial/text_to_vec3");
    public static final ResourceLocation TAG_CONSTANT = id("value/tag");

    public static final ResourceLocation INTEGER_ADD = id("logic/integer/add");
    public static final ResourceLocation INTEGER_SUBTRACT = id("logic/integer/subtract");
    public static final ResourceLocation INTEGER_MULTIPLY = id("logic/integer/multiply");
    public static final ResourceLocation INTEGER_DIVIDE = id("logic/integer/divide");
    public static final ResourceLocation INTEGER_MODULO = id("logic/integer/modulo");
    public static final ResourceLocation INTEGER_EQUAL = id("logic/integer/equal");
    public static final ResourceLocation INTEGER_LESS = id("logic/integer/less");
    public static final ResourceLocation INTEGER_LESS_EQUAL = id("logic/integer/less_equal");
    public static final ResourceLocation INTEGER_GREATER = id("logic/integer/greater");
    public static final ResourceLocation INTEGER_GREATER_EQUAL = id("logic/integer/greater_equal");

    public static final ResourceLocation BIG_INTEGER_CONSTANT = id("value/big_integer");
    public static final ResourceLocation BIG_INTEGER_ADD = id("logic/big_integer/add");
    public static final ResourceLocation BIG_INTEGER_SUBTRACT = id("logic/big_integer/subtract");
    public static final ResourceLocation BIG_INTEGER_MULTIPLY = id("logic/big_integer/multiply");
    public static final ResourceLocation BIG_INTEGER_DIVIDE = id("logic/big_integer/divide");
    public static final ResourceLocation BIG_INTEGER_MODULO = id("logic/big_integer/modulo");
    public static final ResourceLocation BIG_INTEGER_EQUAL = id("logic/big_integer/equal");
    public static final ResourceLocation BIG_INTEGER_LESS = id("logic/big_integer/less");
    public static final ResourceLocation BIG_INTEGER_LESS_EQUAL = id("logic/big_integer/less_equal");
    public static final ResourceLocation BIG_INTEGER_GREATER = id("logic/big_integer/greater");
    public static final ResourceLocation BIG_INTEGER_GREATER_EQUAL = id("logic/big_integer/greater_equal");

    public static final ResourceLocation FLOAT_ADD = id("logic/float/add");
    public static final ResourceLocation FLOAT_SUBTRACT = id("logic/float/subtract");
    public static final ResourceLocation FLOAT_MULTIPLY = id("logic/float/multiply");
    public static final ResourceLocation FLOAT_DIVIDE = id("logic/float/divide");
    public static final ResourceLocation FLOAT_MODULO = id("logic/float/modulo");
    public static final ResourceLocation FLOAT_EQUAL = id("logic/float/equal");
    public static final ResourceLocation FLOAT_LESS = id("logic/float/less");
    public static final ResourceLocation FLOAT_LESS_EQUAL = id("logic/float/less_equal");
    public static final ResourceLocation FLOAT_GREATER = id("logic/float/greater");
    public static final ResourceLocation FLOAT_GREATER_EQUAL = id("logic/float/greater_equal");
    public static final ResourceLocation NUMERIC_ARITHMETIC = id("logic/numeric/arithmetic");
    public static final ResourceLocation NUMERIC_CONVERT = id("logic/numeric/convert");
    public static final ResourceLocation NUMERIC_COMPARE = id("logic/numeric/compare");
    public static final ResourceLocation RANDOM_NUMBER = id("value/random_number");

    public static final ResourceLocation BOOLEAN_NOT = id("logic/boolean/not");
    public static final ResourceLocation BOOLEAN_AND = id("logic/boolean/and");
    public static final ResourceLocation BOOLEAN_OR = id("logic/boolean/or");
    public static final ResourceLocation BOOLEAN_XOR = id("logic/boolean/xor");
    public static final ResourceLocation SELECT_VALUE = id("logic/select_value");
    public static final ResourceLocation BRANCH = id("flow/branch");
    public static final ResourceLocation BREAK_LOOP = id("flow/break_loop");
    public static final ResourceLocation CONTINUE_LOOP = id("flow/continue_loop");
    public static final ResourceLocation WAIT = id("flow/wait");
    public static final ResourceLocation STOP = id("flow/stop");
    public static final ResourceLocation TRIGGER_HURT = id("flow/trigger/hurt");
    public static final ResourceLocation TRIGGER_LOOP = id("flow/trigger/loop");
    public static final ResourceLocation TRIGGER_MELEE = id("flow/trigger/melee");
    public static final ResourceLocation TRIGGER_MOVEMENT = id("flow/trigger/movement");
    public static final ResourceLocation TRIGGER_HEALTH_THRESHOLD =
            id("flow/trigger/health_threshold");
    public static final ResourceLocation TRIGGER_CHAT = id("flow/trigger/chat");
    public static final ResourceLocation VARIABLE_GET = id("state/variable_get");
    public static final ResourceLocation VARIABLE_SET = id("state/variable_set");
    public static final ResourceLocation SHARED_VARIABLE_GET = id("state/shared_variable_get");
    public static final ResourceLocation SHARED_VARIABLE_SET = id("state/shared_variable_set");
    public static final ResourceLocation SHARED_VARIABLE_CLEAR = id("state/shared_variable_clear");
    public static final ResourceLocation SHARED_VARIABLE_CLEAR_ALL = id("state/shared_variable_clear_all");
    public static final ResourceLocation CHAT_READ = id("query/chat_read");
    public static final ResourceLocation CHAT_TRIGGER_MESSAGE = id("query/chat_trigger_message");

    public static final ResourceLocation WORLD_POSITION_CONSTANT = id("spatial/world_position");
    public static final ResourceLocation WORLD_POSITION_CONSTRUCT = id("spatial/world_position_construct");
    public static final ResourceLocation WORLD_POSITION_COMPONENTS = id("spatial/world_position_components");
    public static final ResourceLocation WORLD_POSITION_OFFSET = id("spatial/world_position_offset");
    public static final ResourceLocation WORLD_POSITION_DISTANCE = id("spatial/world_position_distance");
    public static final ResourceLocation WORLD_POSITION_SAME_DIMENSION = id("spatial/world_position_same_dimension");
    public static final ResourceLocation BLOCK_POSITION_CONSTANT = id("spatial/block_position");
    public static final ResourceLocation BLOCK_POSITION_CONSTRUCT = id("spatial/block_position_construct");
    public static final ResourceLocation BLOCK_POSITION_COMPONENTS = id("spatial/block_position_components");
    public static final ResourceLocation POSITION_TO_BLOCK = id("spatial/position_to_block");
    public static final ResourceLocation BLOCK_TO_CENTER = id("spatial/block_to_center");
    public static final ResourceLocation DIRECTION_CONSTANT = id("spatial/direction");
    public static final ResourceLocation DIRECTION_CONSTRUCT = id("spatial/direction_construct");
    public static final ResourceLocation DIRECTION_COMPONENTS = id("spatial/direction_components");
    public static final ResourceLocation DIRECTION_BETWEEN = id("spatial/direction_between");
    public static final ResourceLocation DIRECTION_OPPOSITE = id("spatial/direction_opposite");
    public static final ResourceLocation DIRECTION_DOT = id("spatial/direction_dot");
    public static final ResourceLocation VECTOR_CONSTRUCT = id("spatial/vector_construct");
    public static final ResourceLocation VECTOR_COMPONENTS = id("spatial/vector_components");
    public static final ResourceLocation VEC3_OPERATION = id("spatial/vec3_operation");
    public static final ResourceLocation BLOCK_VOLUME = id("query/block_volume");

    public static final ResourceLocation ENTITY_POSITION = id("query/entity_position");
    public static final ResourceLocation ENTITY_LOOK_DIRECTION = id("query/entity_look_direction");
    public static final ResourceLocation ENTITY_MOVEMENT_DIRECTION = id("query/entity_movement_direction");
    public static final ResourceLocation ENTITY_MOTION = id("query/entity_motion");
    public static final ResourceLocation ENTITY_HEIGHT = id("query/entity_height");
    public static final ResourceLocation ENTITY_DATA = id("query/entity_data");
    public static final ResourceLocation CASTER = id("query/caster");
    public static final ResourceLocation DAMAGE_ATTACKER = id("query/damage_attacker");
    public static final ResourceLocation DAMAGE_AMOUNT = id("query/damage_amount");
    public static final ResourceLocation GAME_TIME = id("query/game_time");
    public static final ResourceLocation LOOP_INDEX = id("query/loop_index");
    public static final ResourceLocation MELEE_TARGET = id("query/melee_target");
    public static final ResourceLocation LOOK_TARGET = id("query/look_target");
    public static final ResourceLocation ENTITIES_AROUND = id("query/entities_around");
    public static final ResourceLocation RAYCAST_BLOCK = id("query/raycast_block");
    public static final ResourceLocation RAYCAST_ENTITY = id("query/raycast_entity");
    public static final ResourceLocation BLOCK_NORMAL = id("query/block_normal");

    public static final ResourceLocation FILTER_ENTITY_ALIVE = id("filter/entity/alive");
    public static final ResourceLocation FILTER_ENTITY_DISTANCE = id("filter/entity/distance");
    public static final ResourceLocation FILTER_ENTITY_ALLIED_TO = id("filter/entity/allied_to");
    public static final ResourceLocation FILTER_ENTITY_HOSTILE_TO = id("filter/entity/hostile_to");
    public static final ResourceLocation FILTER_ENTITY_TARGETED_BY = id("filter/entity/targeted_by");
    public static final ResourceLocation FILTER_ENTITY_LAST_DAMAGED_BY =
            id("filter/entity/last_damaged_by");
    public static final ResourceLocation FILTER_ENTITY_TYPE = id("filter/entity/type");
    public static final ResourceLocation FILTER_ENTITY_HEALTH_AT_LEAST =
            id("filter/entity/health_at_least");
    public static final ResourceLocation FILTER_ENTITY_HEALTH_AT_MOST =
            id("filter/entity/health_at_most");
    public static final ResourceLocation FILTER_ENTITY_MAX_HEALTH_AT_LEAST =
            id("filter/entity/max_health_at_least");
    public static final ResourceLocation FILTER_ENTITY_MAX_HEALTH_AT_MOST =
            id("filter/entity/max_health_at_most");
    public static final ResourceLocation FILTER_ENTITY_HAS_TARGET = id("filter/entity/has_target");
    public static final ResourceLocation FILTER_ENTITY_VISIBLE_FROM = id("filter/entity/visible_from");
    public static final ResourceLocation FILTER_ENTITY_EXACT = id("filter/entity/exact");
    public static final ResourceLocation FILTER_BLOCK_EXACT = id("filter/block/exact");

    public static final ResourceLocation ENTITY_EQUAL = id("logic/entity/equal");
    public static final ResourceLocation WORLD_POSITION_EQUAL = id("logic/world_position/equal");
    public static final ResourceLocation BLOCK_POSITION_EQUAL = id("logic/block_position/equal");
    public static final ResourceLocation DIRECTION_EQUAL = id("logic/direction/equal");
    public static final ResourceLocation RANDOM_ENTITY = id("collection/entity/random");
    public static final ResourceLocation NEAREST_ENTITY_TO_POSITION =
            id("collection/entity/nearest_to_position");
    public static final ResourceLocation RANDOM_WORLD_POSITION =
            id("collection/world_position/random");
    public static final ResourceLocation RANDOM_BLOCK_POSITION =
            id("collection/block_position/random");
    public static final ResourceLocation RANDOM_DIRECTION = id("collection/direction/random");
    public static final ResourceLocation SORT_POINTS_BY_DISTANCE = id("collection/point/distance_sort");
    public static final ResourceLocation DEBUG_OUTPUT = id("action/debug_output");

    private CommonProgramNodeIds() {
    }

    public static ResourceLocation collection(String domain, String operation) {
        if (!switch (domain) {
            case "entity", "world_position", "block_position", "direction" -> true;
            default -> false;
        }) {
            throw new IllegalArgumentException("Unknown collection domain " + domain);
        }
        if (!switch (operation) {
            case "empty", "singleton", "union", "intersection", "difference", "contains",
                 "size", "get", "foreach" -> true;
            default -> false;
        }) {
            throw new IllegalArgumentException("Unknown collection operation " + operation);
        }
        return id("collection/" + domain + "/" + operation);
    }

    private static ResourceLocation id(String path) {
        return AcademyCraft.academy("program/core/" + path);
    }
}
