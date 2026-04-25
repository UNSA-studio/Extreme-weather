package unsa.extreme.weather.com.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExtremeWeatherManager {
    // 每个维度的冷却剩余tick（距离下次允许触发还有多久）
    private static final Map<String, Integer> cooldown = new ConcurrentHashMap<>();
    // 活跃天气
    private static final Map<String, ActiveExtremeWeather> activeWeathers = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ExtremeWeatherManager::onLevelTick);
    }

    private static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            tick(level);
        }
    }

    private static void tick(ServerLevel level) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather current = activeWeathers.get(dim);
        if (current != null) {
            current.tick(level);
            if (current.isExpired()) {
                current.end(level);
                activeWeathers.remove(dim);
                // 天气结束后进入5天冷却
                cooldown.put(dim, 120000); // 5天 = 120000 ticks
            }
            return; // 有天气时不触发新天气
        }

        // 处理冷却
        int cd = cooldown.getOrDefault(dim, 0);
        if (cd > 0) {
            cooldown.put(dim, --cd);
            return; // 冷却中不触发
        }

        // 冷却结束，尝试触发新天气
        double chance = getCurrentChance(level);
        if (random.nextDouble() < chance) {
            // 进入征兆阶段（持续1天 = 24000 ticks）
            ExtremeWeatherType type = chooseRandomType();
            ActiveExtremeWeather weather = new ActiveExtremeWeather(type, -1); // -1 表示还在征兆
            weather.beginOmen(level);
            activeWeathers.put(dim, weather);
        }
    }

    /** 根据污染值计算触发概率：污染300%时概率70%，线性映射 */
    public static double getCurrentChance(Level level) {
        double pollution = PollutionManager.getPollution(level);
        return Math.min(0.7, pollution * 0.7 / 300.0);
    }

    public static Map<ExtremeWeatherType, Double> getCurrentProbabilities(Level level) {
        Map<ExtremeWeatherType, Double> map = new EnumMap<>(ExtremeWeatherType.class);
        double base = getCurrentChance(level) / 6.0; // 平均分给6种天气
        for (ExtremeWeatherType type : ExtremeWeatherType.values()) {
            map.put(type, base);
        }
        return map;
    }

    public static boolean isWeatherActive(Level level) {
        String dim = level.dimension().location().toString();
        return activeWeathers.containsKey(dim);
    }

    /** 未来若干tick内是否会爆发极端天气（用于报警器） */
    public static boolean isExtremeWeatherImminent(Level level, int ticksInFuture) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather current = activeWeathers.get(dim);
        if (current != null) {
            if (current.isOmenPhase()) return true; // 征兆阶段也算即将爆发
            return current.remainingTicks <= ticksInFuture;
        }
        // 冷却中则不算
        if (cooldown.getOrDefault(dim, 0) > 0) return false;
        // 冷却结束但有概率触发
        return random.nextDouble() < getCurrentChance(level) * (ticksInFuture / 1200.0);
    }

    /** 指令强制开启天气（跳过冷却和征兆） */
    public static void forceStartWeather(Level level, ExtremeWeatherType type) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather existing = activeWeathers.get(dim);
        if (existing != null) existing.end(level);
        ActiveExtremeWeather weather = new ActiveExtremeWeather(type, getDefaultDuration(type));
        weather.begin(level);
        activeWeathers.put(dim, weather);
        cooldown.remove(dim); // 强制清除冷却
    }

    /** 指令结束天气 */
    public static void forceEndWeather(Level level) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather current = activeWeathers.remove(dim);
        if (current != null) {
            current.end(level);
            cooldown.put(dim, 120000); // 仍需要冷却
        }
    }

    public static void addSafeZone(Level level, BlockPos center, int radius) {
        // 未实现
    }

    public static void removeSafeZone(Level level, BlockPos center) {
        // 未实现
    }

    private static ExtremeWeatherType chooseRandomType() {
        ExtremeWeatherType[] values = ExtremeWeatherType.values();
        return values[random.nextInt(values.length)];
    }

    private static int getDefaultDuration(ExtremeWeatherType type) {
        return switch (type) {
            case EXTREME_THUNDERSTORM, SUPER_RAIN -> 24000 * (5 + random.nextInt(2));
            case SUPER_DROUGHT -> 24000 * (3 + random.nextInt(3));
            case EXTREME_SANDSTORM, EXTREME_BLIZZARD -> 24000 * (5 + random.nextInt(6));
            case SUPER_TYPHOON -> 24000 * (10 + random.nextInt(4));
        };
    }
}
