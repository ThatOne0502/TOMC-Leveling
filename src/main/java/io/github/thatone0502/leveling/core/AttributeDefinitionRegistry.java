package io.github.thatone0502.leveling.core;

import io.github.thatone0502.leveling.Leveling;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性定义注册表。数据包重载时由 {@link io.github.thatone0502.leveling.data.DefinitionDataLoader} 重建。
 */
public final class AttributeDefinitionRegistry {
    private final Map<Identifier, AttributeDefinition> byId = new LinkedHashMap<>();
    private final List<String> loadErrors = new ArrayList<>();

    public void put(Identifier id, AttributeDefinition definition, String sourcePath) {
        if (byId.containsKey(id)) {
            Leveling.LOGGER.warn("[leveling] Duplicate attribute definition '{}' from {}, overriding previous", id, sourcePath);
        }
        byId.put(id, definition);
    }

    public AttributeDefinition get(Identifier id) {
        return byId.get(id);
    }

    public Collection<AttributeDefinition> all() {
        return byId.values();
    }

    public boolean isEmpty() {
        return byId.isEmpty();
    }

    /**
     * 按 order 升序排序；order 相同按属性 ID 字母序；无 order 的排在最后并按 ID 字母序。
     */
    public List<AttributeDefinition> sorted() {
        List<AttributeDefinition> list = new ArrayList<>(byId.values());
        list.sort((a, b) -> {
            Integer oa = a.order();
            Integer ob = b.order();
            if (oa == null && ob == null) {
                return a.attribute().toString().compareTo(b.attribute().toString());
            }
            if (oa == null) {
                return 1;
            }
            if (ob == null) {
                return -1;
            }
            int cmp = Integer.compare(oa, ob);
            return cmp != 0 ? cmp : a.attribute().toString().compareTo(b.attribute().toString());
        });
        return list;
    }

    public List<String> getLoadErrors() {
        return loadErrors;
    }

    public void addError(String message) {
        loadErrors.add(message);
    }

    public void clear() {
        byId.clear();
        loadErrors.clear();
    }
}
