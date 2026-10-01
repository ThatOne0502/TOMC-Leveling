package com.tomc.leveling.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tomc.leveling.Leveling;
import com.tomc.leveling.core.AttributeDefinition;
import com.tomc.leveling.core.AttributeDefinitionRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 数据包属性定义加载器。遍历所有 data/&lt;namespace&gt;/leveling/*.json。
 * 挂到 SERVER_DATA 重载监听上：世界加载时与 /reload 时触发。
 */
public final class DefinitionDataLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Leveling.MOD_ID, "attribute_definitions");

    private final AttributeDefinitionRegistry registry;

    public DefinitionDataLoader(AttributeDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        registry.clear();
        Map<Identifier, Resource> resources = manager.listResources("leveling", path -> path.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier path = entry.getKey();
            try (InputStream in = entry.getValue().open()) {
                JsonObject object = GsonHelper.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
                AttributeDefinition definition = parse(path, object);
                if (definition != null) {
                    registry.put(definition.attribute(), definition, path.toString());
                }
            } catch (Exception e) {
                Leveling.LOGGER.warn("[leveling] Failed to load attribute definition {}: {}", path, e.getMessage());
                registry.addError(path.toString());
            }
        }
        Leveling.LOGGER.info("[leveling] Loaded {} attribute definition(s), {} error(s)",
                registry.all().size(), registry.getLoadErrors().size());
    }

    private AttributeDefinition parse(Identifier path, JsonObject object) {
        String attrStr = GsonHelper.getAsString(object, "attribute", null);
        if (attrStr == null || attrStr.isEmpty()) {
            return fail(path, "missing 'attribute'");
        }
        Identifier attribute = Identifier.tryParse(attrStr);
        if (attribute == null) {
            return fail(path, "invalid 'attribute': " + attrStr);
        }

        // translation_key 现为可选（display_key 成为新的主键，translation_key 作为回退）。
        String translationKey = GsonHelper.getAsString(object, "translation_key", null);
        String displayKey = GsonHelper.getAsString(object, "display_key", null);
        String descriptionKey = GsonHelper.getAsString(object, "description_key", null);
        Integer order = object.has("order") ? GsonHelper.getAsInt(object, "order", 0) : null;

        int cost = GsonHelper.getAsInt(object, "cost", 1);
        if (cost < 0) {
            return fail(path, "'cost' must be non-negative");
        }

        String opStr = GsonHelper.getAsString(object, "operation", null);
        AttributeModifier.Operation operation = switch (opStr == null ? "" : opStr) {
            case "add_value" -> AttributeModifier.Operation.ADD_VALUE;
            case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> null;
        };
        if (operation == null) {
            return fail(path, "invalid 'operation': " + opStr);
        }

        boolean hasValuePerLevel = object.has("value_per_level");
        boolean hasValues = object.has("values");
        if (hasValuePerLevel == hasValues) {
            return fail(path, "exactly one of 'value_per_level' or 'values' is required");
        }

        double valuePerLevel = 0.0;
        double[] values = new double[0];
        if (hasValuePerLevel) {
            valuePerLevel = GsonHelper.getAsFloat(object, "value_per_level", 0.0F);
        } else {
            JsonElement element = object.get("values");
            if (!element.isJsonArray()) {
                return fail(path, "'values' must be an array");
            }
            JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) {
                return fail(path, "'values' must not be empty");
            }
            values = new double[array.size()];
            for (int i = 0; i < array.size(); i++) {
                JsonElement value = array.get(i);
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                    return fail(path, "'values' must contain only numbers");
                }
                values[i] = value.getAsDouble();
            }
        }

        return new AttributeDefinition(attribute, translationKey, cost, operation, valuePerLevel, values, order, displayKey, descriptionKey);
    }

    private AttributeDefinition fail(Identifier path, String reason) {
        String message = path.toString() + " : " + reason;
        Leveling.LOGGER.warn("[leveling] Skipping attribute definition: {}", message);
        registry.addError(message);
        return null;
    }
}
