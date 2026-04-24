package unsa.extreme.weather.com.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import unsa.extreme.weather.com.ExtremeWeather;
import unsa.extreme.weather.com.blockentity.*;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExtremeWeather.MODID);

    public static final Supplier<BlockEntityType<WeatherStationBlockEntity>> WEATHER_STATION =
            BLOCK_ENTITIES.register("weather_station",
                    () -> BlockEntityType.Builder.of(WeatherStationBlockEntity::new, ModBlocks.WEATHER_STATION.get()).build(null));
    public static final Supplier<BlockEntityType<ExtremeWeatherDetectorBlockEntity>> EXTREME_WEATHER_DETECTOR =
            BLOCK_ENTITIES.register("extreme_weather_detector",
                    () -> BlockEntityType.Builder.of(ExtremeWeatherDetectorBlockEntity::new, ModBlocks.EXTREME_WEATHER_DETECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<AlarmBlockEntity>> ALARM_BLOCK =
            BLOCK_ENTITIES.register("alarm_block",
                    () -> BlockEntityType.Builder.of(AlarmBlockEntity::new, ModBlocks.ALARM_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<WeatherSuppressorBlockEntity>> WEATHER_SUPPRESSOR =
            BLOCK_ENTITIES.register("weather_suppressor",
                    () -> BlockEntityType.Builder.of(WeatherSuppressorBlockEntity::new, ModBlocks.WEATHER_SUPPRESSOR.get()).build(null));
    public static final Supplier<BlockEntityType<PollutionFixStationBlockEntity>> POLLUTION_FIX_STATION =
            BLOCK_ENTITIES.register("pollution_fix_station",
                    () -> BlockEntityType.Builder.of(PollutionFixStationBlockEntity::new, ModBlocks.POLLUTION_FIX_STATION.get()).build(null));
    public static final Supplier<BlockEntityType<PollutionDetectorBlockEntity>> POLLUTION_DETECTOR =
            BLOCK_ENTITIES.register("pollution_detector",
                    () -> BlockEntityType.Builder.of(PollutionDetectorBlockEntity::new, ModBlocks.POLLUTION_DETECTOR.get()).build(null));
}
