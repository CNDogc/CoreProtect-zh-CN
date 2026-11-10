package net.coreprotect.config;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.coreprotect.CoreProtect;
import net.coreprotect.language.Language;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.ErrorReporter;

public class ConfigFile extends Config {

    public static final String CONFIG = "config.yml";
    public static final String LANGUAGE = "language.yml";
    public static final String LANGUAGE_CACHE = ".language";

    private static final TreeMap<String, String> DEFAULT_VALUES = new TreeMap<>();
    private static final TreeMap<String, String> USER_VALUES = new TreeMap<>();
    private static final String DEFAULT_FILE_HEADER = "# CoreProtect Language File (en)";
    private final HashMap<String, String> lang;

    /** i18n-zh: cached zh-CN template (jar resource), keyed by phrase name. */
    private static Map<String, String> ZH_TEMPLATE = null;

    /**
     * i18n-zh: translations that changed between releases, as {@code PHRASE_NAME + "#" + oldText}.
     * Only entries matching the value actually present in the file are rewritten.
     */
    private static final Map<String, String> LEGACY_TRANSLATIONS = new LinkedHashMap<>();
    static {
        LEGACY_TRANSLATIONS.put("HELP_PURGE_4#指定清除数据所属的世界。", "指定清除数据所属的世界或半径。");
        LEGACY_TRANSLATIONS.put("HELP_PURGE_5#指定清除中要包含的方块。", "指定清除中要包含的方块或实体击杀。");
    }

    public static void init(String fileName) throws IOException {
        for (Phrase phrase : Phrase.values()) {
            DEFAULT_VALUES.put(phrase.name(), phrase.getPhrase());
            USER_VALUES.put(phrase.name(), phrase.getUserPhrase());
        }

        boolean isCache = fileName.startsWith(".");
        loadFiles(fileName, isCache);
    }

    public ConfigFile() {
        this.lang = new LinkedHashMap<>();
    }

    public void load(final InputStream in, String fileName, boolean isCache) throws IOException {
        // if we fail reading, we will not corrupt our current config.
        final Map<String, String> newConfig = new LinkedHashMap<>(this.lang.size());
        ConfigFile.load(in, newConfig, true);

        this.lang.clear();
        this.lang.putAll(newConfig);

        for (final Entry<String, String> entry : this.lang.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (DEFAULT_VALUES.containsKey(key) && value.length() > 0 && (!isCache || DEFAULT_VALUES.get(key).equals(USER_VALUES.get(key)))) {
                Phrase phrase = Phrase.valueOf(key);
                if (!isCache) {
                    Language.setUserPhrase(phrase, value);
                }

                Language.setTranslatedPhrase(phrase, value);
            }
        }
    }

    // this function will close in
    public static void load(final InputStream in, final Map<String, String> config, boolean forceCase) throws IOException {
        try (final InputStream in0 = in) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("#")) {
                    continue;
                }

                final int split = line.indexOf(':');

                if (split == -1) {
                    continue;
                }

                String key = line.substring(0, split).trim();
                String value = line.substring(split + 1).trim();

                // Strip out single and double quotes from the start/end of the value
                if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
                    value = value.replaceAll("^'|'$", "");
                    value = value.replace("''", "'");
                    value = value.replace("\\'", "'");
                    value = value.replace("\\\\", "\\");
                }
                else if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.replaceAll("^\"|\"$", "");
                    value = value.replace("\\\"", "\"");
                    value = value.replace("\\\\", "\\");
                }

                if (forceCase) {
                    key = key.toUpperCase(Locale.ROOT);
                }
                config.put(key, value);
            }

            reader.close();
        }
    }

    private static Map<String, byte[]> loadFiles(String fileName, boolean isCache) throws IOException {
        final CoreProtect plugin = CoreProtect.getInstance();
        final File configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        final Map<String, byte[]> map = new HashMap<>();
        final File globalFile = new File(configFolder, fileName);

        if (globalFile.exists()) {
            // we always add options to the global config
            final byte[] data = Files.readAllBytes(globalFile.toPath());
            map.put("config", data);

            final ConfigFile temp = new ConfigFile();
            temp.load(new ByteArrayInputStream(data), fileName, isCache);
            temp.addMissingOptions(globalFile);
        }
        else {
            final ConfigFile temp = new ConfigFile();
            if (!isCache && LANGUAGE.equals(fileName)) {
                // i18n-zh: seed the default language file with the bundled complete zh-CN translation
                try (InputStream template = ConfigFile.class.getResourceAsStream("/lang/language-zh.yml")) {
                    if (template != null) {
                        Files.copy(template, globalFile.toPath());
                    }
                }
                catch (Exception e) {
                    // fall back to English defaults below
                }
            }
            temp.addMissingOptions(globalFile);
            if (!isCache && LANGUAGE.equals(fileName) && globalFile.exists()) {
                // apply the seeded translation immediately on first run
                temp.load(new ByteArrayInputStream(Files.readAllBytes(globalFile.toPath())), fileName, isCache);
            }
        }

        return map;
    }

    @Override
    public void addMissingOptions(final File file) throws IOException {
        if (file.getName().startsWith(".")) {
            return;
        }

        final boolean isLanguage = LANGUAGE.equals(file.getName()) && file.exists();
        if (isLanguage) {
            upgradeLanguageFile(file);
        }

        final boolean writeHeader = !file.exists() || file.length() == 0;
        try (final FileOutputStream fout = new FileOutputStream(file, true)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            if (writeHeader) {
                out.append(DEFAULT_FILE_HEADER);
                out.append(Config.LINE_SEPARATOR);
            }

            for (final Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
                final String key = entry.getKey();

                final String configuredValue = this.lang.get(key);
                if (configuredValue != null) {
                    continue;
                }

                String defaultValue = entry.getValue();
                if (isLanguage) {
                    // i18n-zh: a language file created by an older build gets new keys in Chinese,
                    // not in the English defaults, so the admin never sees a half-translated UI.
                    String translated = zhTemplate().get(key);
                    if (translated != null) {
                        defaultValue = translated;
                    }
                }
                defaultValue = defaultValue.replaceAll("\"", "\\\\\"");

                out.append(Config.LINE_SEPARATOR);
                out.append(key);
                out.append(": ");
                out.append("\"" + defaultValue + "\"");
            }

            out.close();
        }
    }

    /**
     * i18n-zh: updates the bundled zh-CN text of an existing language.yml in place.
     *
     * CoreProtect only seeds a language file when it does not exist yet; afterwards new keys are
     * appended with the English defaults and edited phrases keep their old text. For the zh-CN
     * branch that means an admin upgrading from an older build keeps seeing English until they
     * delete the file. Two safe rewrites are applied here:
     *
     * 1. a value that still equals the English default was written by CoreProtect itself, so it is
     *    replaced with the current Chinese text;
     * 2. a value matching a known older translation is replaced with the current one.
     *
     * Anything else is treated as a deliberate edit by the admin and left untouched.
     */
    private void upgradeLanguageFile(final File file) throws IOException {
        final Map<String, String> template = zhTemplate();
        if (template.isEmpty()) {
            return;
        }

        final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        final List<String> updated = upgradeLanguageLines(lines, template, DEFAULT_VALUES, LEGACY_TRANSLATIONS);
        if (!updated.equals(lines)) {
            Files.write(file.toPath(), updated, StandardCharsets.UTF_8);
        }
    }

    public static List<String> upgradeLanguageLines(final List<String> lines, final Map<String, String> template, final Map<String, String> englishDefaults, final Map<String, String> legacy) {
        final List<String> result = new ArrayList<>(lines);
        boolean changed = false;

        for (int i = 0; i < result.size(); i++) {
            final String line = result.get(i);
            final String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            final int split = trimmed.indexOf(':');
            if (split <= 0) {
                continue;
            }

            final String key = trimmed.substring(0, split).trim();
            final String current = unquote(trimmed.substring(split + 1).trim());
            final String target = template.get(key);
            if (target == null) {
                continue;
            }

            final String english = englishDefaults.get(key);
            String replacement = null;
            if (english != null && english.equals(current)) {
                replacement = target;
            }
            else {
                replacement = legacy.get(key + "#" + current);
            }

            if (replacement != null && !replacement.equals(current)) {
                result.set(i, key + ": \"" + replacement.replace("\"", "\\\"") + "\"");
                changed = true;
            }
        }

        return changed ? result : lines;
    }

    private static String unquote(final String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"");
        }
        return value;
    }

    /** Bundled zh-CN template, keyed by phrase name. Loaded lazily from the jar. */
    private static Map<String, String> zhTemplate() {
        if (ZH_TEMPLATE != null) {
            return ZH_TEMPLATE;
        }

        final Map<String, String> map = new LinkedHashMap<>();
        try (InputStream in = ConfigFile.class.getResourceAsStream("/lang/language-zh.yml")) {
            if (in != null) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    final int split = line.indexOf(':');
                    if (split <= 0) {
                        continue;
                    }
                    map.put(line.substring(0, split).trim(), unquote(line.substring(split + 1).trim()));
                }
                reader.close();
            }
        }
        catch (Exception e) {
            // fall back to English defaults when the template cannot be read
        }

        ZH_TEMPLATE = map;
        return map;
    }

    public static void modifyLine(String fileName, String oldLine, String newLine) {
        try {
            Path path = Paths.get(ConfigHandler.path + fileName);
            List<String> lines = Files.readAllLines(path);

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equalsIgnoreCase(oldLine)) {
                    if (newLine != null && newLine.length() > 0) {
                        lines.set(i, newLine);
                    }
                    else {
                        lines.remove(i);
                    }

                    break;
                }
            }

            if (lines.size() > 0) {
                String lastLine = lines.get(lines.size() - 1); // append the final line to prevent a line separator from being added
                Files.write(path, (lines.remove(lines.size() - 1).isEmpty() ? lines : lines), StandardCharsets.UTF_8);
                Files.write(path, lastLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                lines.clear();
            }
        }
        catch (Exception e) {
            ErrorReporter.report(e);
        }
    }

    public static void sortFile(String fileName) {
        try {
            Path path = Paths.get(ConfigHandler.path + fileName);
            List<String> lines = Files.readAllLines(path);
            List<String> sort = lines.subList(2, lines.size());
            Collections.sort(sort);
            lines = lines.subList(0, 2);
            lines.addAll(sort);

            if (lines.size() > 0) {
                String lastLine = lines.get(lines.size() - 1); // append the final line to prevent a line separator from being added
                Files.write(path, (lines.remove(lines.size() - 1).isEmpty() ? lines : lines), StandardCharsets.UTF_8);
                Files.write(path, lastLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                lines.clear();
            }
        }
        catch (Exception e) {
            ErrorReporter.report(e);
        }
    }

    public static void resetCache(String cacheName, String fileName) throws IOException {
        File file = new File(CoreProtect.getInstance().getDataFolder(), cacheName);
        if (file.length() > 0) {
            new FileOutputStream(file).close();
            init(fileName);
        }
    }
}
