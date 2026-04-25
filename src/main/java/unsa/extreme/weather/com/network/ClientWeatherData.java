package unsa.extreme.weather.com.network;

import net.minecraft.core.BlockPos;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import net.minecraft.world.phys.Vec3;

public class ClientWeatherData {
    private static boolean active = false;
    private static ExtremeWeatherType currentType = null;
    private static BlockPos center = null;
    private static int radius = 0;
    private static double moveX = 0, moveZ = 0;
    private static int remainingTicks = 0;

    public static void updateFromPacket(WeatherSyncPacket packet) {
        active = packet.isActive();
        currentType = packet.getWeatherType();
        center = packet.getCenter();
        radius = packet.getRadius();
        moveX = packet.getMoveX();
        moveZ = packet.getMoveZ();
        remainingTicks = packet.getRemainingTicks();
    }

    public static boolean isActive() { return active; }
    public static ExtremeWeatherType getType() { return currentType; }
    public static BlockPos getCenter() { return center; }
    public static int getRadius() { return radius; }
    public static Vec3 getMoveDirection() {
        return new Vec3(moveX, 0, moveZ);
    }
    public static int getRemainingTicks() { return remainingTicks; }
}
