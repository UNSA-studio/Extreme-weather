package unsa.extreme.weather.com.network;

import net.minecraft.core.BlockPos;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ClientWeatherData {
    private static ExtremeWeatherType currentType = null;
    private static BlockPos center = null;
    private static int radius = 0;
    private static Vec3 moveDirection = null;
    private static int remainingTicks = 0;
    private static boolean active = false;

    public static void updateFromPacket(WeatherSyncPacket packet) {
        if (packet.active().isPresent() && packet.active().get()) {
            currentType = packet.type().orElse(null);
            center = packet.center().orElse(null);
            radius = packet.radius().orElse(0);
            if (packet.moveX().isPresent() && packet.moveZ().isPresent()) {
                moveDirection = new Vec3(packet.moveX().get(), 0, packet.moveZ().get());
            }
            remainingTicks = packet.remainingTicks().orElse(0);
            active = true;
        } else {
            active = false;
            currentType = null;
            center = null;
            radius = 0;
            moveDirection = null;
            remainingTicks = 0;
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static ExtremeWeatherType getType() {
        return currentType;
    }

    public static BlockPos getCenter() {
        return center;
    }

    public static int getRadius() {
        return radius;
    }

    public static Vec3 getMoveDirection() {
        return moveDirection;
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }
}
