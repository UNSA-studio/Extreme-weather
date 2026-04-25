package unsa.extreme.weather.com.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.world.level.ChunkPos;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class PollutionManager {
    private static final Map<String, Double> pollutionMap = new HashMap<>();
    private static final Map<String, Double> dailyIncreaseMap = new HashMap<>();

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(PollutionManager::onLevelTick);
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension() != Level.OVERWORLD) return;
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

    private static double calculateDailyIncrease(ServerLevel level) {
        double increase = 0.0;
        // 收集所有玩家附近的已加载区块位置
        Set<ChunkPos> loadedChunks = new HashSet<>();
        for (Player player : level.players()) {
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            // 添加玩家所在区块及其周围 1 格区块（模拟玩家加载范围）
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    loadedChunks.add(new ChunkPos(playerChunk.x + dx, playerChunk.z + dz));
                }
            }
        }
        // 对每个区块进行扫描
        for (ChunkPos chunkPos : loadedChunks) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                increase += scanChunk(chunk);
            }
        }
        return increase;
    }

    private static double scanChunk(LevelChunk chunk) {
        double chunkPollution = 0.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                    pos.set(x + chunk.getPos().getMinBlockX(), y, z + chunk.getPos().getMinBlockZ());
                    BlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();
                    if (block instanceof CampfireBlock || block instanceof FireBlock) {
                        chunkPollution += 0.1;
                    }
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be != null) {
                        chunkPollution += getModdedPollution(be);
                    }
                }
            }
        }
        return chunkPollution;
    }

    private static double getModdedPollution(BlockEntity be) {
        double pollution = 0.0;
        String className = be.getClass().getName();
        if (className.contains("create")) pollution += 0.5;
        if (className.contains("appeng")) pollution += 0.3;
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
