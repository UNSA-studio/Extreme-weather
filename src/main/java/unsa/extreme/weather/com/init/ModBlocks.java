package unsa.extreme.weather.com.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import unsa.extreme.weather.com.ExtremeWeather;
import unsa.extreme.weather.com.block.*;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExtremeWeather.MODID);

    public static final DeferredBlock<Block> WEATHER_STATION = BLOCKS.register("weather_station",
            () -> new WeatherStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> EXTREME_WEATHER_DETECTOR = BLOCKS.register("extreme_weather_detector",
            () -> new ExtremeWeatherDetectorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> ALARM_BLOCK = BLOCKS.register("alarm_block",
            () -> new AlarmBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> WEATHER_SUPPRESSOR = BLOCKS.register("weather_suppressor",
            () -> new WeatherSuppressorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> POLLUTION_FIX_STATION = BLOCKS.register("pollution_fix_station",
            () -> new PollutionFixStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> POLLUTION_DETECTOR = BLOCKS.register("pollution_detector",
            () -> new PollutionDetectorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL)));
}
