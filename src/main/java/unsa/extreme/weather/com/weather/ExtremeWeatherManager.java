package unsa.extreme.weather.com.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExtremeWeatherManager {
    private static final Map<String, ActiveExtremeWeather> activeWeathers = new ConcurrentHashMap<>();
    private static final Map<String, Set<BlockPos>> safeZones = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public static void init(IEventBus bus) {
        bus.addListener(ExtremeWeatherManager::onLevelTick);
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
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
            }
        } else {
            double chance = getCurrentChance(level);
            if (random.nextDouble() < chance) {
                ExtremeWeatherType type = chooseRandomType();
                int duration = getDefaultDuration(type);
                ActiveExtremeWeather newWeather = new ActiveExtremeWeather(type, duration);
                newWeather.begin(level);
                activeWeathers.put(dim, newWeather);
            }
        }
    }

    public static double getCurrentChance(Level level) {
        double base = 0.01; // 基础每日概率（每tick极低）
        double pollutionMod = PollutionManager.getPollution(level) / 300.0; // 300%时翻倍
        return Math.min(base * pollutionMod, 0.05);
    }

    public static Map<ExtremeWeatherType, Double> getCurrentProbabilities(Level level) {
        Map<ExtremeWeatherType, Double> map = new EnumMap<>(ExtremeWeatherType.class);
        double base = getCurrentChance(level);
        for (ExtremeWeatherType type : ExtremeWeatherType.values()) {
            map.put(type, base);
        }
        return map;
    }

    public static boolean isWeatherActive(Level level) {
        return activeWeathers.containsKey(level.dimension().location().toString());
    }

    public static boolean isExtremeWeatherImminent(Level level, int ticksInFuture) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather curr = activeWeathers.get(dim);
        if (curr != null && curr.remainingTicks <= ticksInFuture) return true;
        double chance = getCurrentChance(level);
        return random.nextDouble() < chance * (ticksInFuture / 1200.0);
    }

    public static void forceStartWeather(Level level, ExtremeWeatherType type) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather existing = activeWeathers.get(dim);
        if (existing != null) existing.end(level);
        ActiveExtremeWeather weather = new ActiveExtremeWeather(type, getDefaultDuration(type));
        weather.begin(level);
        activeWeathers.put(dim, weather);
    }

    public static void forceEndWeather(Level level) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather current = activeWeathers.remove(dim);
        if (current != null) current.end(level);
    }

    public static void addSafeZone(Level level, BlockPos center, int radius) {
        safeZones.computeIfAbsent(level.dimension().location().toString(), k -> new HashSet<>()).add(center);
    }

    public static void removeSafeZone(Level level, BlockPos center) {
        Set<BlockPos> zones = safeZones.get(level.dimension().location().toString());
        if (zones != null) zones.remove(center);
    }

    private static ExtremeWeatherType chooseRandomType() {
        ExtremeWeatherType[] values = ExtremeWeatherType.values();
        return values[random.nextInt(values.length)];
    }

    private static int getDefaultDuration(ExtremeWeatherType type) {
        return switch (type) {
            case EXTREME_THUNDERSTORM, SUPER_RAIN -> 24000 * (5 + random.nextInt(2)); // 5-6天
            case SUPER_DROUGHT -> 24000 * (3 + random.nextInt(3));
            case EXTREME_SANDSTORM, EXTREME_BLIZZARD -> 24000 * (5 + random.nextInt(6));
            case SUPER_TYPHOON -> 24000 * (10 + random.nextInt(4));
        };
    }
}
