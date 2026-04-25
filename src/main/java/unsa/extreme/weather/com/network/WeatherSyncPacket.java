package unsa.extreme.weather.com.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import unsa.extreme.weather.com.ExtremeWeather;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import unsa.extreme.weather.com.weather.ActiveExtremeWeather;
import net.minecraft.world.phys.Vec3;

public class WeatherSyncPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WeatherSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ExtremeWeather.MODID, "weather_sync"));

    private final boolean active;
    private final ExtremeWeatherType weatherType;
    private final BlockPos center;
    private final int radius;
    private final double moveX;
    private final double moveZ;
    private final int remainingTicks;

    public WeatherSyncPacket(boolean active, ExtremeWeatherType weatherType, BlockPos center,
                             int radius, double moveX, double moveZ, int remainingTicks) {
        this.active = active;
        this.weatherType = weatherType;
        this.center = center;
        this.radius = radius;
        this.moveX = moveX;
        this.moveZ = moveZ;
        this.remainingTicks = remainingTicks;
    }

    public static WeatherSyncPacket fromWeather(ActiveExtremeWeather weather) {
        if (weather == null) return createClear();
        Vec3 dir = weather.getMoveDirection();
        return new WeatherSyncPacket(true, weather.type, weather.getCenter(), weather.getRadius(),
                dir != null ? dir.x : 0, dir != null ? dir.z : 0, weather.remainingTicks);
    }

    public static WeatherSyncPacket createClear() {
        return new WeatherSyncPacket(false, null, null, 0, 0, 0, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, WeatherSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public WeatherSyncPacket decode(ByteBuf buf) {
            boolean active = buf.readBoolean();
            String typeName = buf.readUtf();
            ExtremeWeatherType wtype = typeName.isEmpty() ? null : ExtremeWeatherType.valueOf(typeName);
            long pos = buf.readLong();
            BlockPos center = pos == 0 ? null : BlockPos.of(pos);
            int radius = buf.readInt();
            double mx = buf.readDouble();
            double mz = buf.readDouble();
            int ticks = buf.readInt();
            return new WeatherSyncPacket(active, wtype, center, radius, mx, mz, ticks);
        }

        @Override
        public void encode(ByteBuf buf, WeatherSyncPacket packet) {
            buf.writeBoolean(packet.active);
            buf.writeUtf(packet.weatherType == null ? "" : packet.weatherType.name());
            buf.writeLong(packet.center == null ? 0 : packet.center.asLong());
            buf.writeInt(packet.radius);
            buf.writeDouble(packet.moveX);
            buf.writeDouble(packet.moveZ);
            buf.writeInt(packet.remainingTicks);
        }
    };

    public static void handle(WeatherSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientWeatherData.updateFromPacket(packet));
    }

    public boolean isActive() { return active; }
    public ExtremeWeatherType getWeatherType() { return weatherType; }
    public BlockPos getCenter() { return center; }
    public int getRadius() { return radius; }
    public double getMoveX() { return moveX; }
    public double getMoveZ() { return moveZ; }
    public int getRemainingTicks() { return remainingTicks; }
}
