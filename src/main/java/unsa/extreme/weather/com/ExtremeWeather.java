package unsa.extreme.weather.com;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import unsa.extreme.weather.com.command.ExtremeWeatherCommand;
import unsa.extreme.weather.com.config.ModConfigs;
import unsa.extreme.weather.com.init.ModBlocks;
import unsa.extreme.weather.com.init.ModItems;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.init.ModSounds;
import unsa.extreme.weather.com.weather.ExtremeWeatherManager;
import unsa.extreme.weather.com.weather.PollutionManager;
import unsa.extreme.weather.com.network.ModPacketHandler;

@Mod(ExtremeWeather.MODID)
public class ExtremeWeather {
    public static final String MODID = "extreme_weather";

    public ExtremeWeather(IEventBus bus, ModContainer container) {
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, ModConfigs.SPEC);
        ExtremeWeatherManager.init(bus);
        PollutionManager.init(bus);
        ModPacketHandler.register(bus);
        bus.addListener(this::onRegisterCommands);
    }

    @SubscribeEvent
    private void onRegisterCommands(RegisterCommandsEvent event) {
        ExtremeWeatherCommand.register(event.getDispatcher());
    }
}
