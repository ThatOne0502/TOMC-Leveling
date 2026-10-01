package com.tomc.leveling.core;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 数据包中单个属性定义的运行期内存模型。
 *
 * @param attribute     属性注册 ID（如 minecraft:max_health）
 * @param translationKey 旧版译名键（可空，作为回退）
 * @param cost          每级消耗属性点
 * @param operation     修饰符运算
 * @param valuePerLevel 使用 value_per_level 时每级提升值（否则为 0）
 * @param values        使用 values 时按级取值（否则为空数组）
 * @param order         面板排序（可空，越小越靠前，null 排最后）
 * @param displayKey    显示名译名键（可空）
 * @param descriptionKey 描述译名键（可空）
 */
public record AttributeDefinition(
        Identifier attribute,
        String translationKey,
        int cost,
        AttributeModifier.Operation operation,
        double valuePerLevel,
        double[] values,
        Integer order,
        String displayKey,
        String descriptionKey
) {
    /** 第 level 级（1 起）的单级提升值；超出 values 长度时用末值。 */
    public double valueForLevel(int level) {
        if (level < 1) {
            return 0.0;
        }
        if (values.length > 0) {
            return values[Math.min(level - 1, values.length - 1)];
        }
        return valuePerLevel;
    }

    /** 前 levels 级的总修饰值（线性累加，非复利）。 */
    public double totalValue(int levels) {
        double sum = 0.0;
        for (int i = 1; i <= levels; i++) {
            sum += valueForLevel(i);
        }
        return sum;
    }

    /** 生效的显示名译名键：display_key → translation_key → attribute.name.&lt;path&gt;。 */
    public String effectiveDisplayKey() {
        return firstNonEmpty(displayKey, translationKey, "attribute.name." + attribute.getPath());
    }

    /** 生效的描述译名键：description_key → displayKey + ".description"。 */
    public String effectiveDescriptionKey() {
        return firstNonEmpty(descriptionKey, effectiveDisplayKey() + ".description");
    }

    private static String firstNonEmpty(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return "";
    }
}
