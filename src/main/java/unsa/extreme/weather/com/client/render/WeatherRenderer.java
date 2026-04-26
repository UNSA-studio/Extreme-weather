package unsa.extreme.weather.com.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import unsa.extreme.weather.com.ExtremeWeather;
import unsa.extreme.weather.com.network.ClientWeatherData;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;

@EventBusSubscriber(modid = ExtremeWeather.MODID, value = Dist.CLIENT)
public class WeatherRenderer {
    private static final RandomSource random = RandomSource.create();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!ClientWeatherData.isActive()) return;

        ExtremeWeatherType type = ClientWeatherData.getType();
        BlockPos center = ClientWeatherData.getCenter();
        int radius = ClientWeatherData.getRadius();
        if (center == null || type == null) return;

        LocalPlayer player = mc.player;
        BlockPos playerPos = player.blockPosition();
        int dx = Math.abs(playerPos.getX() - center.getX());
        int dz = Math.abs(playerPos.getZ() - center.getZ());
        if (dx > radius || dz > radius) return;

        int particleCount = switch (type) {
            case EXTREME_SANDSTORM -> 20;  // 暴增粒子数
            case EXTREME_BLIZZARD -> 15;
            case SUPER_TYPHOON -> 10;
            default -> 0;
        };

        for (int i = 0; i < particleCount; i++) {
            double x = playerPos.getX() + random.nextDouble() * 80 - 40;
            double z = playerPos.getZ() + random.nextDouble() * 80 - 40;
            double y = playerPos.getY() + random.nextDouble() * 25 - 5;

            if (type == ExtremeWeatherType.EXTREME_SANDSTORM) {
                mc.level.addParticle(ParticleTypes.CLOUD, x, y, z,
                        0.2 * (random.nextDouble() - 0.5),
                        0.01,
                        0.2 * (random.nextDouble() - 0.5));
                if (random.nextFloat() < 0.4) {
                    mc.level.addParticle(ParticleTypes.LARGE_SMOKE, x + 2, y + 1, z + 2, 0, 0, 0);
                }
            } else if (type == ExtremeWeatherType.EXTREME_BLIZZARD) {
                mc.level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z,
                        0.3 * (random.nextDouble() - 0.5),
                        -0.15,
                        0.3 * (random.nextDouble() - 0.5));
                if (random.nextFloat() < 0.2) {
                    mc.level.addParticle(ParticleTypes.CLOUD, x, y + 2, z, 0, 0, 0);
                }
            } else if (type == ExtremeWeatherType.SUPER_TYPHOON) {
                mc.level.addParticle(ParticleTypes.CLOUD, x, y, z,
                        0.4 * (random.nextDouble() - 0.5),
                        0.02,
                        0.4 * (random.nextDouble() - 0.5));
            }
        }
    }
}
