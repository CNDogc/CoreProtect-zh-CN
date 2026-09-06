package net.coreprotect.config;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.World;

import net.coreprotect.CoreProtect;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.language.Language;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.VersionUtils;

public class Config extends Language {

    private static final Map<String, String[]> HEADERS = new HashMap<>();
    private static final Map<String, String> DEFAULT_VALUES = new LinkedHashMap<>();
    private static final Map<String, Config> CONFIG_BY_WORLD_NAME = new HashMap<>();
    private static final String DEFAULT_FILE_HEADER = "# CoreProtect Config";
    public static final String LINE_SEPARATOR = "\n";

    private static final Config GLOBAL = new Config();
    private final HashMap<String, String> config;
    private Config defaults;

    public String DONATION_KEY;
    public String DATABASE_TYPE;
    public String CLICKHOUSE_HOST;
    public String CLICKHOUSE_DATABASE;
    public String CLICKHOUSE_USERNAME;
    public String CLICKHOUSE_PASSWORD;
    public String DUCKDB_MEMORY_LIMIT;
    public String DUCKDB_MAX_TEMP_DIRECTORY_SIZE;
    public String PREFIX;
    public String MYSQL_HOST;
    public String MYSQL_DATABASE;
    public String MYSQL_USERNAME;
    public String MYSQL_PASSWORD;
    public String LANGUAGE;
    public String AUTO_PURGE;
    public String AUTO_PURGE_TIME;
    public boolean ENABLE_SSL;
    public boolean CLICKHOUSE_TLS;
    public boolean DISABLE_WAL;
    public boolean HOVER_EVENTS;
    public boolean DATABASE_LOCK;
    public boolean LOG_CANCELLED_CHAT;
    public boolean HOPPER_FILTER_META;
    public boolean DUPLICATE_SUPPRESSION;
    public boolean EXCLUDE_TNT;
    public boolean NETWORK_DEBUG;
    public boolean MYSQL;
    public boolean CHECK_UPDATES;
    public boolean ERROR_REPORTING;
    public boolean API_ENABLED;
    public boolean VERBOSE;
    public boolean ROLLBACK_ITEMS;
    public boolean ROLLBACK_ENTITIES;
    public boolean SKIP_GENERIC_DATA;
    public boolean BLOCK_PLACE;
    public boolean BLOCK_BREAK;
    public boolean NATURAL_BREAK;
    public boolean BLOCK_MOVEMENT;
    public boolean PISTONS;
    public boolean BLOCK_BURN;
    public boolean BLOCK_IGNITE;
    public boolean FIRE_EXTINGUISH;
    public boolean EXPLOSIONS;
    public boolean ENTITY_CHANGE;
    public boolean ENTITY_KILLS;
    public boolean ENTITY_SPAWNS;
    public boolean SIGN_TEXT;
    public boolean BUCKETS;
    public boolean LEAF_DECAY;
    public boolean TREE_GROWTH;
    public boolean MUSHROOM_GROWTH;
    public boolean VINE_GROWTH;
    public boolean SCULK_SPREAD;
    public boolean PORTALS;
    public boolean WATER_FLOW;
    public boolean LAVA_FLOW;
    public boolean LIQUID_TRACKING;
    public boolean ITEM_TRANSACTIONS;
    public boolean ITEM_DROPS;
    public boolean ITEM_PICKUPS;
    public boolean HOPPER_TRANSACTIONS;
    public boolean PLAYER_INTERACTIONS;
    public boolean PLAYER_MESSAGES;
    public boolean PLAYER_COMMANDS;
    public boolean PLAYER_SESSIONS;
    public boolean UNKNOWN_LOGGING;
    public boolean USERNAME_CHANGES;
    public boolean WORLDEDIT;
    public int MAXIMUM_POOL_SIZE;
    public int CLICKHOUSE_PORT;
    public int CLICKHOUSE_CONSUMER_DELAY;
    public int MYSQL_PORT;
    public int DEFAULT_RADIUS;
    public int DUCKDB_THREADS;
    public int MAX_RADIUS;

    static {
        DEFAULT_VALUES.put("donation-key", "");
        DEFAULT_VALUES.put("database-type", "duckdb");
        DEFAULT_VALUES.put("table-prefix", "co_");
        DEFAULT_VALUES.put("mysql-host", "127.0.0.1");
        DEFAULT_VALUES.put("mysql-port", "3306");
        DEFAULT_VALUES.put("mysql-database", "database");
        DEFAULT_VALUES.put("mysql-username", "root");
        DEFAULT_VALUES.put("mysql-password", "");
        DEFAULT_VALUES.put("clickhouse-host", "127.0.0.1");
        DEFAULT_VALUES.put("clickhouse-port", "8123");
        DEFAULT_VALUES.put("clickhouse-database", "default");
        DEFAULT_VALUES.put("clickhouse-username", "default");
        DEFAULT_VALUES.put("clickhouse-password", "");
        DEFAULT_VALUES.put("clickhouse-tls", "false");
        DEFAULT_VALUES.put("duckdb-memory-limit", "512MB");
        DEFAULT_VALUES.put("duckdb-threads", "3");
        DEFAULT_VALUES.put("duckdb-max-temp-directory-size", "10GB");
        DEFAULT_VALUES.put("language", "en");
        DEFAULT_VALUES.put("auto-purge", "false");
        DEFAULT_VALUES.put("check-updates", "true");
        DEFAULT_VALUES.put("error-reporting", "true");
        DEFAULT_VALUES.put("api-enabled", "true");
        DEFAULT_VALUES.put("verbose", "true");
        DEFAULT_VALUES.put("default-radius", "10");
        DEFAULT_VALUES.put("max-radius", "100");
        DEFAULT_VALUES.put("rollback-items", "true");
        DEFAULT_VALUES.put("rollback-entities", "true");
        DEFAULT_VALUES.put("skip-generic-data", "true");
        DEFAULT_VALUES.put("block-place", "true");
        DEFAULT_VALUES.put("block-break", "true");
        DEFAULT_VALUES.put("natural-break", "true");
        DEFAULT_VALUES.put("block-movement", "true");
        DEFAULT_VALUES.put("pistons", "true");
        DEFAULT_VALUES.put("block-burn", "true");
        DEFAULT_VALUES.put("block-ignite", "true");
        DEFAULT_VALUES.put("fire-extinguish", "false");
        DEFAULT_VALUES.put("explosions", "true");
        DEFAULT_VALUES.put("entity-change", "true");
        DEFAULT_VALUES.put("entity-kills", "true");
        DEFAULT_VALUES.put("entity-spawns", "true");
        DEFAULT_VALUES.put("sign-text", "true");
        DEFAULT_VALUES.put("buckets", "true");
        DEFAULT_VALUES.put("leaf-decay", "true");
        DEFAULT_VALUES.put("tree-growth", "true");
        DEFAULT_VALUES.put("mushroom-growth", "true");
        DEFAULT_VALUES.put("vine-growth", "true");
        DEFAULT_VALUES.put("sculk-spread", "true");
        DEFAULT_VALUES.put("portals", "true");
        DEFAULT_VALUES.put("water-flow", "true");
        DEFAULT_VALUES.put("lava-flow", "true");
        DEFAULT_VALUES.put("liquid-tracking", "true");
        DEFAULT_VALUES.put("item-transactions", "true");
        DEFAULT_VALUES.put("item-drops", "true");
        DEFAULT_VALUES.put("item-pickups", "true");
        DEFAULT_VALUES.put("hopper-transactions", "true");
        DEFAULT_VALUES.put("player-interactions", "true");
        DEFAULT_VALUES.put("player-messages", "true");
        DEFAULT_VALUES.put("player-commands", "true");
        DEFAULT_VALUES.put("player-sessions", "true");
        DEFAULT_VALUES.put("username-changes", "true");
        DEFAULT_VALUES.put("worldedit", "true");

        HEADERS.put("donation-key", new String[] { "# CoreProtect 是捐赠制软件。捐赠密钥可从 coreprotect.net/donate/ 获取。" });
        HEADERS.put("database-type", new String[] { "# CoreProtect 使用的数据库引擎，可选值：duckdb、clickhouse、sqlite、mysql。", "# 更改数据库引擎或连接目标后，请执行 /co reload 或重启服务器。" });
        HEADERS.put("mysql-host", new String[] { "# MySQL 连接设置。" });
        HEADERS.put("clickhouse-host", new String[] { "# ClickHouse 连接设置（要求 ClickHouse 25.6 或更高版本）。", "# 所配置的数据库必须已存在；CoreProtect 会创建其带前缀的数据表和视图。", "# 多写入端需满足：禁用 database-lock、版本与前缀一致、数据目录各自独立、直连同一物理服务器。", "# 各写入端时钟需保持同步；共享前缀即世界与玩家的同一逻辑命名空间。", "# 不要重新分配用户名；更名后需先记录带 UUID 的登录，再记录无 UUID 的活动。", "# 迁移或清除数据前先停止所有写入端；清除数据时剩余服务器需开启 database-lock。", "# 不支持副本、分布式或负载均衡等相互独立的 ClickHouse 节点。" });
        HEADERS.put("duckdb-memory-limit", new String[] { "# 内嵌 DuckDB 数据库的资源限制。", "# memory-limit 控制 DuckDB 的缓冲管理器；temporary 限制溢写数据上限，不会预先占用。" });
        HEADERS.put("language", new String[] { "# 若修改此项，将自动尝试在线翻译各词条。", "# 语言代码列表：https://coreprotect.net/languages/ ｜ 本分支已默认内置完整简中（language.yml），无需修改。" });
        HEADERS.put("auto-purge", new String[] { "# 自动清除早于所配置时间的数据。", "# 示例：30d、12w、6mo。设为 false 可禁用。" });
        HEADERS.put("check-updates", new String[] { "# 若启用，CoreProtect 将在服务器启动时检查更新。", "# 有新版本时将通过服务器控制台通知。", });
        HEADERS.put("error-reporting", new String[] { "# 自动将错误信息发送给插件作者。" });
        HEADERS.put("api-enabled", new String[] { "# 若启用，其他插件将可以使用 CoreProtect API。", });
        HEADERS.put("verbose", new String[] { "# 若启用，回滚和恢复时将显示额外数据。", "# 在回滚命令后附加 \"#verbose\" 可手动触发。" });
        HEADERS.put("default-radius", new String[] { "# 回滚或恢复未指定半径时，将使用此值作为半径。", "# 设为 \"0\" 可禁用自动添加半径。" });
        HEADERS.put("max-radius", new String[] { "# 命令可使用的最大半径。设为 \"0\" 可禁用限制。", "# 如需不限半径执行回滚或恢复，可使用 \"r:#global\"。" });
        HEADERS.put("rollback-items", new String[] { "# 若启用，回滚将包含玩家从容器等处取走的物品。" });
        HEADERS.put("rollback-entities", new String[] { "# 若启用，回滚将包含实体击杀及可归因于玩家的实体生成。" });
        HEADERS.put("skip-generic-data", new String[] { "# 若启用，将不记录一般性数据，例如僵尸在日光下燃烧。" });
        HEADERS.put("block-place", new String[] { "# 记录玩家放置的方块。" });
        HEADERS.put("block-break", new String[] { "# 记录玩家破坏的方块。" });
        HEADERS.put("natural-break", new String[] { "# 记录从其他方块上脱落的方块，例如玩家破坏泥土后随之掉落的告示牌或火把。", "# 床/门的正常回滚依赖此选项。" });
        HEADERS.put("block-movement", new String[] { "# 正确追踪方块移动，例如沙子或沙砾下落。" });
        HEADERS.put("pistons", new String[] { "# 正确追踪被活塞移动的方块。" });
        HEADERS.put("block-burn", new String[] { "# 记录在火中烧毁的方块。" });
        HEADERS.put("block-ignite", new String[] { "# 记录方块被自然点燃的情况，例如火势蔓延。" });
        HEADERS.put("fire-extinguish", new String[] { "# 记录火焰自然熄灭的情况。" });
        HEADERS.put("explosions", new String[] { "# 记录爆炸，例如 TNT 和苦力怕。" });
        HEADERS.put("entity-change", new String[] { "# 追踪实体改变方块的行为，例如末影人搬走方块。" });
        HEADERS.put("entity-kills", new String[] { "# 记录被击杀的实体，例如被杀死的牛和末影人。" });
        HEADERS.put("entity-spawns", new String[] { "# 记录玩家放置或生成的实体，例如船和刷怪蛋生成的生物。" });
        HEADERS.put("sign-text", new String[] { "# 记录告示牌上的文字。禁用后，告示牌回滚时将为空白。" });
        HEADERS.put("buckets", new String[] { "# 记录玩家用桶放置/移除的岩浆源与水源。" });
        HEADERS.put("leaf-decay", new String[] { "# 记录树木树叶的自然凋落。" });
        HEADERS.put("tree-growth", new String[] { "# 记录树木生长。树木会关联到种植树苗的玩家。" });
        HEADERS.put("mushroom-growth", new String[] { "# 记录蘑菇的生长。" });
        HEADERS.put("vine-growth", new String[] { "# 记录藤蔓的自然生长。" });
        HEADERS.put("sculk-spread", new String[] { "# 记录幽匿方块从幽匿催发体的蔓延。" });
        HEADERS.put("portals", new String[] { "# 记录下界传送门等传送门的自然生成。" });
        HEADERS.put("water-flow", new String[] { "# 记录水的流动。若水冲毁了其他方块（例如火把），", "# 可借此将其正确回滚。" });
        HEADERS.put("lava-flow", new String[] { "# 记录岩浆的流动。若岩浆摧毁了其他方块（例如火把），", "# 可借此将其正确回滚。" });
        HEADERS.put("liquid-tracking", new String[] { "# 允许正确追踪液体并将其关联到玩家。", "# 例如，玩家放置的水流走并冲毁火把后，", "# 只需回滚该玩家即可将其全部恢复。" });
        HEADERS.put("item-transactions", new String[] { "# 追踪物品交易，例如玩家从箱子、熔炉或发射器中取出物品。" });
        HEADERS.put("item-drops", new String[] { "# 记录玩家丢弃的物品。" });
        HEADERS.put("item-pickups", new String[] { "# 记录玩家拾取的物品。" });
        HEADERS.put("hopper-transactions", new String[] { "# 追踪所有漏斗交易，例如漏斗从箱子、熔炉或发射器中取出物品。" });
        HEADERS.put("player-interactions", new String[] { "# 追踪玩家交互，例如开门、按按钮、打开箱子。玩家交互无法回滚。" });
        HEADERS.put("player-messages", new String[] { "# 记录玩家在聊天中发送的消息。" });
        HEADERS.put("player-commands", new String[] { "# 记录玩家使用的所有命令。" });
        HEADERS.put("player-sessions", new String[] { "# 记录玩家的登入与登出。" });
        HEADERS.put("username-changes", new String[] { "# 记录玩家更改 Minecraft 用户名的情况。" });
        HEADERS.put("worldedit", new String[] { "# 记录通过插件 \"WorldEdit\" 所做的更改（若服务器装有该插件）。" });
    }

    private void readValues() {
        this.ENABLE_SSL = this.getBoolean("enable-ssl", false);
        this.DISABLE_WAL = this.getBoolean("disable-wal", false);
        this.HOVER_EVENTS = this.getBoolean("hover-events", true);
        this.DATABASE_LOCK = this.getBoolean("database-lock", true);
        this.LOG_CANCELLED_CHAT = this.getBoolean("log-cancelled-chat", true);
        this.HOPPER_FILTER_META = this.getBoolean("hopper-filter-meta", false);
        this.DUPLICATE_SUPPRESSION = this.getBoolean("duplicate-suppression", true);
        this.EXCLUDE_TNT = this.getBoolean("exclude-tnt", false);
        this.NETWORK_DEBUG = this.getBoolean("network-debug", false);
        this.UNKNOWN_LOGGING = this.getBoolean("unknown-logging", false);
        this.MAXIMUM_POOL_SIZE = this.getInt("maximum-pool-size", 10);
        this.DONATION_KEY = this.getString("donation-key");
        this.DATABASE_TYPE = this.getString("database-type");
        this.CLICKHOUSE_HOST = this.getString("clickhouse-host");
        this.CLICKHOUSE_PORT = this.getInt("clickhouse-port");
        this.CLICKHOUSE_CONSUMER_DELAY = Math.max(500,
                this.getInt("clickhouse-consumer-delay", 2500));
        this.CLICKHOUSE_DATABASE = this.getString("clickhouse-database");
        this.CLICKHOUSE_USERNAME = this.getString("clickhouse-username");
        this.CLICKHOUSE_PASSWORD = this.getString("clickhouse-password");
        this.CLICKHOUSE_TLS = this.getBoolean("clickhouse-tls");
        this.MYSQL = this.getBoolean("use-mysql");
        this.PREFIX = this.getString("table-prefix");
        this.MYSQL_HOST = this.getString("mysql-host");
        this.MYSQL_PORT = this.getInt("mysql-port");
        this.MYSQL_DATABASE = this.getString("mysql-database");
        this.MYSQL_USERNAME = this.getString("mysql-username");
        this.MYSQL_PASSWORD = this.getString("mysql-password");
        this.DUCKDB_MEMORY_LIMIT = this.getString("duckdb-memory-limit");
        this.DUCKDB_THREADS = this.getInt("duckdb-threads", 3);
        this.DUCKDB_MAX_TEMP_DIRECTORY_SIZE = this.getString("duckdb-max-temp-directory-size");
        this.LANGUAGE = this.getString("language");
        this.AUTO_PURGE = this.getString("auto-purge");
        this.AUTO_PURGE_TIME = this.getString("auto-purge-time");
        this.CHECK_UPDATES = this.getBoolean("check-updates");
        this.ERROR_REPORTING = this.getBoolean("error-reporting");
        this.API_ENABLED = this.getBoolean("api-enabled");
        this.VERBOSE = this.getBoolean("verbose");
        this.DEFAULT_RADIUS = this.getInt("default-radius");
        this.MAX_RADIUS = this.getInt("max-radius");
        this.ROLLBACK_ITEMS = this.getBoolean("rollback-items");
        this.ROLLBACK_ENTITIES = this.getBoolean("rollback-entities");
        this.SKIP_GENERIC_DATA = this.getBoolean("skip-generic-data");
        this.BLOCK_PLACE = this.getBoolean("block-place");
        this.BLOCK_BREAK = this.getBoolean("block-break");
        this.NATURAL_BREAK = this.getBoolean("natural-break");
        this.BLOCK_MOVEMENT = this.getBoolean("block-movement");
        this.PISTONS = this.getBoolean("pistons");
        this.BLOCK_BURN = this.getBoolean("block-burn");
        this.BLOCK_IGNITE = this.getBoolean("block-ignite");
        this.FIRE_EXTINGUISH = this.getBoolean("fire-extinguish");
        this.EXPLOSIONS = this.getBoolean("explosions");
        this.ENTITY_CHANGE = this.getBoolean("entity-change");
        this.ENTITY_KILLS = this.getBoolean("entity-kills");
        this.ENTITY_SPAWNS = this.getBoolean("entity-spawns");
        this.SIGN_TEXT = this.getBoolean("sign-text");
        this.BUCKETS = this.getBoolean("buckets");
        this.LEAF_DECAY = this.getBoolean("leaf-decay");
        this.TREE_GROWTH = this.getBoolean("tree-growth");
        this.MUSHROOM_GROWTH = this.getBoolean("mushroom-growth");
        this.VINE_GROWTH = this.getBoolean("vine-growth");
        this.SCULK_SPREAD = this.getBoolean("sculk-spread");
        this.PORTALS = this.getBoolean("portals");
        this.WATER_FLOW = this.getBoolean("water-flow");
        this.LAVA_FLOW = this.getBoolean("lava-flow");
        this.LIQUID_TRACKING = this.getBoolean("liquid-tracking");
        this.ITEM_TRANSACTIONS = this.getBoolean("item-transactions");
        this.ITEM_DROPS = this.getBoolean("item-drops");
        this.ITEM_PICKUPS = this.getBoolean("item-pickups");
        this.HOPPER_TRANSACTIONS = this.getBoolean("hopper-transactions");
        this.PLAYER_INTERACTIONS = this.getBoolean("player-interactions");
        this.PLAYER_MESSAGES = this.getBoolean("player-messages");
        this.PLAYER_COMMANDS = this.getBoolean("player-commands");
        this.PLAYER_SESSIONS = this.getBoolean("player-sessions");
        this.USERNAME_CHANGES = this.getBoolean("username-changes");
        this.WORLDEDIT = this.getBoolean("worldedit");
    }

    public static void init() throws IOException {
        parseConfig(loadFiles(ConfigFile.CONFIG));
        // pass variables to ConfigFile.parseConfig(ConfigFile.loadFiles());
    }

    public static Config getGlobal() {
        return GLOBAL;
    }

    // returns a world specific config if it exists, otherwise the global config
    public static Config getConfig(final World world) {
        return getConfig(world.getName());
    }

    public static Config getConfig(final String worldName) {
        Config ret = CONFIG_BY_WORLD_NAME.get(worldName);
        if (ret == null) {
            ret = CONFIG_BY_WORLD_NAME.getOrDefault(worldName, GLOBAL);
            CONFIG_BY_WORLD_NAME.put(worldName, ret);
        }
        return ret;
    }

    public Config() {
        this.config = new LinkedHashMap<>();
    }

    public void setDefaults(final Config defaults) {
        this.defaults = defaults;
    }

    private String get(final String key, final String dfl) {
        String configured = this.config.get(key);
        if (configured == null) {
            if (dfl != null) {
                return dfl;
            }
            if (this.defaults == null) {
                configured = DEFAULT_VALUES.get(key);
            }
            else {
                configured = this.defaults.config.getOrDefault(key, DEFAULT_VALUES.get(key));
            }
        }
        return configured;
    }

    private boolean getBoolean(final String key) {
        final String configured = this.get(key, null);
        return configured != null && configured.startsWith("t");
    }

    private boolean getBoolean(final String key, final boolean dfl) {
        final String configured = this.get(key, null);
        return configured == null ? dfl : configured.startsWith("t");
    }

    private int getInt(final String key) {
        return this.getInt(key, 0);
    }

    private int getInt(final String key, final int dfl) {
        String configured = this.get(key, null);

        if (configured == null) {
            return dfl;
        }

        configured = configured.replaceAll("[^0-9]", "");

        return configured.isEmpty() ? dfl : Integer.parseInt(configured);
    }

    private String getString(final String key) {
        final String configured = this.get(key, null);
        return configured == null ? "" : configured;
    }

    public void clearConfig() {
        this.config.clear();
    }

    public boolean hasOption(String key) {
        return this.config.containsKey(key);
    }

    public void loadDefaults() {
        this.clearConfig();
        this.readValues();
    }

    public void load(final InputStream in) throws IOException {
        // if we fail reading, we will not corrupt our current config.
        final Map<String, String> newConfig = new LinkedHashMap<>(this.config.size());
        ConfigFile.load(in, newConfig, false);

        this.clearConfig();
        this.config.putAll(newConfig);

        this.readValues();
    }

    private static Map<String, byte[]> loadFiles(String fileName) throws IOException {
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

            // can't modify GLOBAL, we're likely off-main here
            final Config temp = new Config();
            temp.load(new ByteArrayInputStream(data));
            temp.addMissingOptions(globalFile);
        }
        else {
            final Config temp = new Config();
            temp.loadDefaults();
            temp.addMissingOptions(globalFile);
            map.put("config", Files.readAllBytes(globalFile.toPath()));
        }

        for (final File worldConfigFile : configFolder.listFiles((File file) -> file.getName().endsWith(".yml"))) {
            final String name = worldConfigFile.getName();
            if (name.equals(ConfigFile.CONFIG) || name.equals(ConfigFile.LANGUAGE)) {
                continue;
            }

            map.put(name.substring(0, name.length() - ".yml".length()), Files.readAllBytes(worldConfigFile.toPath()));
        }

        return map;
    }

    // this should only be called on the main thread
    private static void parseConfig(final Map<String, byte[]> data) {
        if (!Bukkit.isPrimaryThread()) {
            // we call reloads asynchronously
            // for now this solution is good enough to ensure we only modify on the main thread
            final CompletableFuture<Void> complete = new CompletableFuture<>();
            final CompletableFuture<Void> shutdown = Consumer.databaseReloadShutdownSignal();

            Scheduler.runTask(CoreProtect.getInstance(), () -> {
                if (shutdown.isDone()) {
                    return;
                }
                try {
                    parseConfig(data);
                }
                catch (final Throwable thr) {
                    if (thr instanceof ThreadDeath) {
                        throw (ThreadDeath) thr;
                    }
                    complete.completeExceptionally(thr);
                    return;
                }
                complete.complete(null);
            });

            CompletableFuture.anyOf(complete, shutdown).join();
            if (!complete.isDone()) {
                complete.cancel(false);
                throw new CancellationException("Configuration reload cancelled for shutdown");
            }
            complete.join();
            return;
        }

        CONFIG_BY_WORLD_NAME.clear();

        // we need to load global first since it is used for config defaults
        final byte[] defaultData = data.get("config");
        if (defaultData != null) {
            try {
                GLOBAL.load(new ByteArrayInputStream(defaultData));
            }
            catch (final IOException ex) {
                throw new RuntimeException(ex); // shouldn't happen
            }
        }
        else {
            GLOBAL.loadDefaults();
        }

        for (final Map.Entry<String, byte[]> entry : data.entrySet()) {
            final String worldName = entry.getKey();
            if (worldName.equals("config")) {
                continue;
            }

            final byte[] fileData = entry.getValue();
            final Config config = new Config();
            config.setDefaults(GLOBAL);

            try {
                config.load(new ByteArrayInputStream(fileData));
            }
            catch (final IOException ex) {
                throw new RuntimeException(ex); // shouldn't happen
            }

            CONFIG_BY_WORLD_NAME.put(worldName, config);
        }
    }

    public void addMissingOptions(final File file) throws IOException {
        final boolean writeHeader = !file.exists() || file.length() == 0;
        try (final FileOutputStream fout = new FileOutputStream(file, true)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            if (writeHeader) {
                out.append(DEFAULT_FILE_HEADER);
                out.append(LINE_SEPARATOR);
            }

            for (final Map.Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
                final String key = entry.getKey();
                String defaultValue = entry.getValue();

                final String configuredValue = this.config.get(key);

                if (configuredValue != null) {
                    continue;
                }
                if (key.equals("auto-purge") && VersionUtils.isCommunityEdition()) {
                    continue;
                }
                if (key.equals("database-type") && !writeHeader) {
                    defaultValue = this.getBoolean("use-mysql") ? "mysql" : "sqlite";
                }

                final String[] header = HEADERS.get(key);

                if (header != null) {
                    out.append(LINE_SEPARATOR);
                    for (final String headerLine : header) {
                        out.append(headerLine);
                        out.append(LINE_SEPARATOR);
                    }
                }

                out.append(key);
                out.append(": ");
                out.append(defaultValue);
                out.append(LINE_SEPARATOR);
            }

            out.close();
        }
    }
}
