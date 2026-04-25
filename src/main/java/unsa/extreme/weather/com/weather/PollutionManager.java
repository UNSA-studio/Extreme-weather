package unsa.extreme.weather.com.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import java.util.HashMap;
import java.util.Map;

public class PollutionManager {
    private static final Map<String, Double> pollutionMap = new HashMap<>();
    private static final Map<String, Double> dailyIncreaseMap = new HashMap<>();

    public static void init(IEventBus modBus) {
        // 在 Forge 事件总线上监听 LevelTickEvent
        NeoForge.EVENT_BUS.addListener(PollutionManager::onLevelTick);
    }

    private static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.getGameTime() % 24000 == 0) {
                dailyTick(level);
            }
        }
    }

    private static void dailyTick(ServerLevel level) {
        String dim = level.dimension().location().toString();
        double increase = calculateDailyIncrease(level);
        dailyIncreaseMap.put(dim, increase);
        addPollution(level, increase);
    }

    private static double calculateDailyIncrease(ServerLevel level) {
        return 0.5 + Math.random() * 0.5;
    }

    public static double getPollution(Level level) {
        String dim = level.dimension().location().toString();
        return pollutionMap.getOrDefault(dim, 300.0);
    }

    public static void addPollution(Level level, double amount) {
        String dim = level.dimension().location().toString();
        double current = getPollution(level);
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
