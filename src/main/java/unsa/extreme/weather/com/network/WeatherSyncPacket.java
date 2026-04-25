package unsa.extreme.weather.com.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import unsa.extreme.weather.com.ExtremeWeather;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import unsa.extreme.weather.com.weather.ActiveExtremeWeather;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 服务端 -> 客户端：同步天气信息
 */
public record WeatherSyncPacket(
        Optional<ExtremeWeatherType> type,
        Optional<BlockPos> center,
        Optional<Integer> radius,
        Optional<Double> moveX,
        Optional<Double> moveZ,
        Optional<Integer> remainingTicks,
        Optional<Boolean> active
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WeatherSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ExtremeWeather.MODID, "weather_sync"));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, WeatherSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(
                    s -> ExtremeWeatherType.valueOf(s),
                    type -> type.name()
            )),
            WeatherSyncPacket::type,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG.map(BlockPos::of, BlockPos::asLong)),
            WeatherSyncPacket::center,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            WeatherSyncPacket::radius,
            ByteBufCodecs.optional(ByteBufCodecs.DOUBLE),
            WeatherSyncPacket::moveX,
            ByteBufCodecs.optional(ByteBufCodecs.DOUBLE),
            WeatherSyncPacket::moveZ,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            WeatherSyncPacket::remainingTicks,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL),
            WeatherSyncPacket::active,
            WeatherSyncPacket::new
    );

    public static void handle(WeatherSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientWeatherData.updateFromPacket(packet);
        });
    }

    // 静态工厂方法
    public static WeatherSyncPacket fromWeather(ActiveExtremeWeather weather) {
        if (weather == null) {
            return new WeatherSyncPacket(Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
        Vec3 dir = weather.getMoveDirection();
        return new WeatherSyncPacket(
                Optional.of(weather.type),
                Optional.of(weather.getCenter()),
                Optional.of(weather.getRadius()),
                Optional.of(dir != null ? dir.x : 0),
                Optional.of(dir != null ? dir.z : 0),
                Optional.of(weather.remainingTicks),
                Optional.of(true)
        );
    }

    public static WeatherSyncPacket clear() {
        return new WeatherSyncPacket(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
