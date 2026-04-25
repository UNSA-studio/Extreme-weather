package unsa.extreme.weather.com.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PollutionManager {
    // 世界维度 -> 污染值 (0-1000)
    private static final Map<String, Double> pollutionMap = new HashMap<>();
    // 世界维度 -> 每日预估增长量
    private static final Map<String, Double> dailyIncreaseMap = new HashMap<>();

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(PollutionManager::onLevelTick);
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            // 每天一次（24000 ticks）
            if (level.getGameTime() % 24000 == 0) {
                dailyTick(level);
            }
        }
    }

    private static void dailyTick(ServerLevel level) {
        String dim = level.dimension().location().toString();
        double increase = calculateDailyIncrease(level);
        dailyIncreaseMap.put(dim, increase);
        addPollution(dim, increase);
    }

    // 计算当天污染增长量（基于已加载区块）
    private static double calculateDailyIncrease(ServerLevel level) {
        double increase = 0.0;
        // 遍历所有已加载区块
        for (LevelChunk chunk : level.getChunkSource().getChunkMap().getChunks()) {
            increase += scanChunk(chunk);
        }
        return increase;
    }

    // 扫描单个区块内的污染源，返回本区块导致的增长率（百分比/天）
    private static double scanChunk(LevelChunk chunk) {
        double chunkPollution = 0.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                    pos.set(x + chunk.getPos().getMinBlockX(), y, z + chunk.getPos().getMinBlockZ());
                    BlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();

                    // 基础污染：篝火、火焰
                    if (block instanceof CampfireBlock || block instanceof FireBlock) {
                        chunkPollution += 0.1; // 每个篝火/火焰每天增加0.1%
                    }

                    // 对其他模组工业设备的支持
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be != null) {
                        // 这里可以反射检测 create 的应力消耗等
                        chunkPollution += getModdedPollution(be);
                    }
                }
            }
        }
        return chunkPollution;
    }

    // 预留其他模组的污染检测
    private static double getModdedPollution(BlockEntity be) {
        double pollution = 0.0;
        // 使用反射或接口检测，这里展示一种简单方式：检查类名
        String className = be.getClass().getName();
        // 机械动力 Create 设备 (示例)
        if (className.contains("create")) {
            pollution += 0.5; // 每个Create设备基础污染
        }
        // AE2 设备
        if (className.contains("appeng")) {
            pollution += 0.3;
        }
        // 可继续添加其他模组
        return pollution;
    }

    public static double getPollution(Level level) {
        return pollutionMap.getOrDefault(level.dimension().location().toString(), 300.0);
    }

    public static void addPollution(String dim, double amount) {
        double current = pollutionMap.getOrDefault(dim, 300.0);
        pollutionMap.put(dim, Math.min(1000.0, current + amount));
    }

    public static void reducePollution(Level level, double percent) {
        String dim = level.dimension().location().toString();
        double current = getPollution(level);
        pollutionMap.put(dim, Math.max(0.0, current * (1.0 - percent / 100.0)));
    }

    public static double getEstimatedDailyIncrease(Level level) {
        return dailyIncreaseMap.getOrDefault(level.dimension().location().toString(), 0.0);
    }
}
