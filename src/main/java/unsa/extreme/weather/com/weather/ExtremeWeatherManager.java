package unsa.extreme.weather.com.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import unsa.extreme.weather.com.network.WeatherSyncPacket;
import unsa.extreme.weather.com.config.ModConfigs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExtremeWeatherManager {
    private static final Map<String, ActiveExtremeWeather> activeWeathers = new ConcurrentHashMap<>();
    private static final Map<String, Integer> cooldownTicks = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    private static final double DAILY_CHANCE = 0.70; // 基础每日概率70%
    private static final int COOLDOWN_DURATION = 24000; // 1天冷却

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ExtremeWeatherManager::onLevelTick);
    }

    private static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension() != Level.OVERWORLD) return;
            tick(level);
        }
    }

    private static void tick(ServerLevel level) {
        String dim = level.dimension().location().toString();
        
        // 处理冷却
        Integer cooldown = cooldownTicks.getOrDefault(dim, 0);
        if (cooldown > 0) {
            cooldownTicks.put(dim, cooldown - 1);
        }

        ActiveExtremeWeather current = activeWeathers.get(dim);
        if (current != null) {
            current.tick(level);
            if (current.isExpired()) {
                current.end(level);
                activeWeathers.remove(dim);
                cooldownTicks.put(dim, COOLDOWN_DURATION); // 冷却
                PacketDistributor.sendToAllPlayers(WeatherSyncPacket.createClear());
            }
        } else if (cooldownTicks.getOrDefault(dim, 0) <= 0) {
            // 每 tick 概率 = 每日概率 / 一天总 tick 数
            double tickChance = DAILY_CHANCE / 24000.0;
            if (random.nextDouble() < tickChance * (1 + PollutionManager.getPollution(level) / 300.0)) {
                BlockPos spawn = level.getSharedSpawnPos();
                ExtremeWeatherType type = chooseTypeByBiome(level, spawn);
                if (type == null) return;
                int duration = getDefaultDuration(type);
                ActiveExtremeWeather newWeather = new ActiveExtremeWeather(type, duration);
                newWeather.begin(level);
                activeWeathers.put(dim, newWeather);
                PacketDistributor.sendToAllPlayers(WeatherSyncPacket.fromWeather(newWeather));
            }
        }
    }

    public static double getCurrentChance(Level level) {
        double base = DAILY_CHANCE;
        double pollutionFactor = PollutionManager.getPollution(level) / 300.0;
        return Math.min(base * pollutionFactor, 1.0);
    }

    public static boolean isWeatherActive(Level level) {
        return activeWeathers.containsKey(level.dimension().location().toString());
    }
    public static ActiveExtremeWeather getActiveWeather(Level level) {
        return activeWeathers.get(level.dimension().location().toString());
    }
    public static List<ActiveExtremeWeather> getWeathersInRadius(Level level, BlockPos center, int chunkRadius) {
        List<ActiveExtremeWeather> result = new ArrayList<>();
        ActiveExtremeWeather aw = getActiveWeather(level);
        if (aw != null) {
            BlockPos awCenter = aw.getCenter();
            int dx = Math.abs(awCenter.getX() - center.getX()) >> 4;
            int dz = Math.abs(awCenter.getZ() - center.getZ()) >> 4;
            if (dx < chunkRadius && dz < chunkRadius) result.add(aw);
        }
        return result;
    }
    public static boolean isExtremeWeatherImminent(Level level, int ticksInFuture) {
        ActiveExtremeWeather curr = getActiveWeather(level);
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
        cooldownTicks.put(dim, 0); // 清除冷却，立即开始
        PacketDistributor.sendToAllPlayers(WeatherSyncPacket.fromWeather(weather));
    }
    public static void forceEndWeather(Level level) {
        String dim = level.dimension().location().toString();
        ActiveExtremeWeather current = activeWeathers.remove(dim);
        if (current != null) {
            current.end(level);
            cooldownTicks.put(dim, COOLDOWN_DURATION); // 设置冷却，防止再次生成
            PacketDistributor.sendToAllPlayers(WeatherSyncPacket.createClear());
        }
    }
    public static void addSafeZone(Level level, BlockPos center, int radius) {}
    public static void removeSafeZone(Level level, BlockPos center) {}

    public static Map<ExtremeWeatherType, Double> getCurrentProbabilities(Level level) {
        Map<ExtremeWeatherType, Double> map = new EnumMap<>(ExtremeWeatherType.class);
        double base = getCurrentChance(level);
        for (ExtremeWeatherType type : ExtremeWeatherType.values()) {
            map.put(type, base);
        }
        return map;
    }

    // 生物群系权重等其余方法保持不变，省略以让回复清晰
    // 实际上需要完整的文件，下面补全
    private static Map<ExtremeWeatherType, Double> getBiomeWeights(Biome biome) {
        Map<ExtremeWeatherType, Double> weights = new EnumMap<>(ExtremeWeatherType.class);
        Holder<Biome> holder = Holder.direct(biome);
        if (holder.is(BiomeTags.IS_OCEAN)) {
            weights.put(ExtremeWeatherType.SUPER_TYPHOON, 0.60);
            weights.put(ExtremeWeatherType.SUPER_RAIN, 0.25);
            weights.put(ExtremeWeatherType.EXTREME_THUNDERSTORM, 0.10);
            weights.put(ExtremeWeatherType.EXTREME_BLIZZARD, 0.05);
        } else if (holder.is(BiomeTags.IS_BEACH) || holder.is(BiomeTags.IS_RIVER)) {
            weights.put(ExtremeWeatherType.SUPER_RAIN, 0.30);
            weights.put(ExtremeWeatherType.EXTREME_THUNDERSTORM, 0.30);
            weights.put(ExtremeWeatherType.SUPER_TYPHOON, 0.20);
            weights.put(ExtremeWeatherType.EXTREME_BLIZZARD, 0.10);
        } else if (holder.is(BiomeTags.IS_MOUNTAIN) || holder.is(BiomeTags.IS_HILL)) {
            weights.put(ExtremeWeatherType.EXTREME_BLIZZARD, 0.35);
            weights.put(ExtremeWeatherType.EXTREME_THUNDERSTORM, 0.25);
            weights.put(ExtremeWeatherType.SUPER_RAIN, 0.20);
            weights.put(ExtremeWeatherType.EXTREME_SANDSTORM, 0.15);
            weights.put(ExtremeWeatherType.SUPER_DROUGHT, 0.05);
        } else if (holder.is(BiomeTags.IS_FOREST) || holder.is(BiomeTags.IS_JUNGLE)) {
            weights.put(ExtremeWeatherType.SUPER_RAIN, 0.35);
            weights.put(ExtremeWeatherType.EXTREME_THUNDERSTORM, 0.30);
            weights.put(ExtremeWeatherType.EXTREME_SANDSTORM, 0.15);
            weights.put(ExtremeWeatherType.SUPER_DROUGHT, 0.10);
            weights.put(ExtremeWeatherType.EXTREME_BLIZZARD, 0.10);
        } else if (holder.is(BiomeTags.IS_TAIGA)) {
            weights.put(ExtremeWeatherType.EXTREME_BLIZZARD, 0.50);
            weights.put(ExtremeWeatherType.SUPER_RAIN, 0.15);
            weights.put(ExtremeWeatherType.EXTREME_THUNDERSTORM, 0.15);
            weights.put(ExtremeWeatherType.EXTREME_SANDSTORM, 0.10);
            weights.put(ExtremeWeatherType.SUPER_DROUGHT, 0.10);
        } else if (holder.is(BiomeTags.IS_NETHER) || holder.is(BiomeTags.IS_END)) {
            return weights;
        } else {
            weights.put(ExtremeWeatherType.EXTREME_THUNDERSTORM, 0.30);
            weights.put(ExtremeWeatherType.SUPER_RAIN, 0.25);
            weights.put(ExtremeWeatherType.EXTREME_SANDSTORM, 0.25);
            weights.put(ExtremeWeatherType.SUPER_DROUGHT, 0.15);
            weights.put(ExtremeWeatherType.EXTREME_BLIZZARD, 0.05);
        }
        return weights;
    }

    private static ExtremeWeatherType chooseTypeByBiome(ServerLevel level, BlockPos pos) {
        Holder<Biome> holder = level.getBiome(pos);
        Biome biome = holder.value();
        Map<ExtremeWeatherType, Double> weights = getBiomeWeights(biome);
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return null;
        double r = random.nextDouble() * total;
        double cumulative = 0;
        for (Map.Entry<ExtremeWeatherType, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (r <= cumulative) return entry.getKey();
        }
        return ExtremeWeatherType.SUPER_RAIN;
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
