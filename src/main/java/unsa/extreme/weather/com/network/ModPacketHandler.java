package unsa.extreme.weather.com.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import unsa.extreme.weather.com.ExtremeWeather;

public class ModPacketHandler {
    public static void register(IEventBus bus) {
        bus.addListener(ModPacketHandler::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ExtremeWeather.MODID).versioned("1.0.0");
        registrar.playToClient(
                WeatherSyncPacket.TYPE,
                WeatherSyncPacket.STREAM_CODEC,
                WeatherSyncPacket::handle
        );
    }
}
