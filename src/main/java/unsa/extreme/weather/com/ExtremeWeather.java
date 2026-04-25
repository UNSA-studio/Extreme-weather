package unsa.extreme.weather.com;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
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

    public ExtremeWeather(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, ModConfigs.SPEC);
        ExtremeWeatherManager.init(modBus);
        PollutionManager.init(modBus);
        ModPacketHandler.register(modBus);

        // 命令注册必须放在游戏事件总线上
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ExtremeWeatherCommand.register(event.getDispatcher());
    }
}
