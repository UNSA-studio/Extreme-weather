package unsa.extreme.weather.com.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import unsa.extreme.weather.com.ExtremeWeather;
import unsa.extreme.weather.com.item.*;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExtremeWeather.MODID);

    public static final DeferredItem<Item> WEATHER_BALLOON = ITEMS.register("weather_balloon",
            () -> new WeatherBalloonItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> DATA_CONNECTOR = ITEMS.register("data_connector",
            () -> new DataConnectorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> WEATHER_MISSILE_1 = ITEMS.register("weather_missile_1",
            () -> new WeatherMissileItem(new Item.Properties().stacksTo(4), 1, 10));
    public static final DeferredItem<Item> WEATHER_MISSILE_2 = ITEMS.register("weather_missile_2",
            () -> new WeatherMissileItem(new Item.Properties().stacksTo(4), 2, 20));
    public static final DeferredItem<Item> WEATHER_MISSILE_3 = ITEMS.register("weather_missile_3",
            () -> new WeatherMissileItem(new Item.Properties().stacksTo(4), 3, 30));
    public static final DeferredItem<Item> REPAIR_AGENT = ITEMS.register("repair_agent",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CO2_FIXER = ITEMS.register("co2_fixer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> METHANE_CONVERTER = ITEMS.register("methane_converter",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> REPAIR_MIX = ITEMS.register("repair_mix",
            () -> new RepairMixItem(new Item.Properties()));
    public static final DeferredItem<Item> WATER_ESSENCE = ITEMS.register("water_essence",
            () -> new Item(new Item.Properties()));

    // Block items
    public static final DeferredItem<BlockItem> WEATHER_STATION_ITEM = ITEMS.register("weather_station",
            () -> new BlockItem(ModBlocks.WEATHER_STATION.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> EXTREME_WEATHER_DETECTOR_ITEM = ITEMS.register("extreme_weather_detector",
            () -> new BlockItem(ModBlocks.EXTREME_WEATHER_DETECTOR.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ALARM_BLOCK_ITEM = ITEMS.register("alarm_block",
            () -> new BlockItem(ModBlocks.ALARM_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> WEATHER_SUPPRESSOR_ITEM = ITEMS.register("weather_suppressor",
            () -> new BlockItem(ModBlocks.WEATHER_SUPPRESSOR.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> POLLUTION_FIX_STATION_ITEM = ITEMS.register("pollution_fix_station",
            () -> new BlockItem(ModBlocks.POLLUTION_FIX_STATION.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> POLLUTION_DETECTOR_ITEM = ITEMS.register("pollution_detector",
            () -> new BlockItem(ModBlocks.POLLUTION_DETECTOR.get(), new Item.Properties()));
}
