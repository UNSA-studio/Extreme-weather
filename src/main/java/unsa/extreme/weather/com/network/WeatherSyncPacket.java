package unsa.extreme.weather.com.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

    public static final StreamCodec<ByteBuf, WeatherSyncPacket> STREAM_CODEC = StreamCodec.of(
            WeatherSyncPacket::encode,
            WeatherSyncPacket::decode
    );

    private static WeatherSyncPacket decode(ByteBuf buf) {
        // 将 ByteBuf 视为 FriendlyByteBuf 以使用 readUtf/writeUtf
        FriendlyByteBuf friendly = new FriendlyByteBuf(buf);
        boolean active = friendly.readBoolean();
        String typeName = friendly.readUtf();
        ExtremeWeatherType wtype = typeName.isEmpty() ? null : ExtremeWeatherType.valueOf(typeName);
        long pos = friendly.readLong();
        BlockPos center = pos == 0 ? null : BlockPos.of(pos);
        int radius = friendly.readInt();
        double mx = friendly.readDouble();
        double mz = friendly.readDouble();
        int ticks = friendly.readInt();
        return new WeatherSyncPacket(active, wtype, center, radius, mx, mz, ticks);
    }

    private static void encode(ByteBuf buf, WeatherSyncPacket packet) {
        FriendlyByteBuf friendly = new FriendlyByteBuf(buf);
        friendly.writeBoolean(packet.active);
        friendly.writeUtf(packet.weatherType == null ? "" : packet.weatherType.name());
        friendly.writeLong(packet.center == null ? 0 : packet.center.asLong());
        friendly.writeInt(packet.radius);
        friendly.writeDouble(packet.moveX);
        friendly.writeDouble(packet.moveZ);
        friendly.writeInt(packet.remainingTicks);
    }

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
