package net.coreprotect.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simplified Chinese display names (i18n-zh branch).
 * Maps English registry names of blocks/items/entities to official zh-CN names.
 * Unmapped names (modded blocks, new vanilla additions) fall back to English.
 * Data source: resources/lang/names-zh-cn.txt, generated from the vanilla
 * zh_cn.json by tools/gen-names-zh.py.
 */
public final class ZhNameMapper {

    private static final Map<String, String> MATERIALS = new HashMap<>();
    private static final Map<String, String> ENTITIES = new HashMap<>();

    static {
        try (InputStream in = ZhNameMapper.class.getResourceAsStream("/lang/names-zh-cn.txt")) {
            if (in != null) {
                Map<String, String> target = MATERIALS;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (line.startsWith("[material]")) {
                            target = MATERIALS;
                            continue;
                        }
                        if (line.startsWith("[entity]")) {
                            target = ENTITIES;
                            continue;
                        }
                        int split = line.indexOf('=');
                        if (split > 0) {
                            target.put(line.substring(0, split).trim().toLowerCase(Locale.ROOT), line.substring(split + 1).trim());
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            // missing or unreadable resource: fall back to English names
        }
    }

    private ZhNameMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static String material(String name) {
        return lookup(MATERIALS, name);
    }

    public static String entity(String name) {
        return lookup(ENTITIES, name);
    }

    private static String lookup(Map<String, String> map, String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return map.getOrDefault(name.toLowerCase(Locale.ROOT), name);
    }
}
