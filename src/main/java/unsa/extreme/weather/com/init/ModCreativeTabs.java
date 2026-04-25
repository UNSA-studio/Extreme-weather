package unsa.extreme.weather.com.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import unsa.extreme.weather.com.ExtremeWeather;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExtremeWeather.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.extreme_weather"))
                    .icon(() -> new ItemStack(ModItems.WEATHER_BALLOON.get()))
                    .displayItems((params, output) -> {
                        // 添加所有物品
                        output.accept(ModItems.WEATHER_STATION_ITEM.get());
                        output.accept(ModItems.EXTREME_WEATHER_DETECTOR_ITEM.get());
                        output.accept(ModItems.ALARM_BLOCK_ITEM.get());
                        output.accept(ModItems.WEATHER_SUPPRESSOR_ITEM.get());
                        output.accept(ModItems.POLLUTION_FIX_STATION_ITEM.get());
                        output.accept(ModItems.POLLUTION_DETECTOR_ITEM.get());
                        output.accept(ModItems.WEATHER_BALLOON.get());
                        output.accept(ModItems.DATA_CONNECTOR.get());
                        output.accept(ModItems.WEATHER_MISSILE_1.get());
                        output.accept(ModItems.WEATHER_MISSILE_2.get());
                        output.accept(ModItems.WEATHER_MISSILE_3.get());
                        output.accept(ModItems.REPAIR_AGENT.get());
                        output.accept(ModItems.CO2_FIXER.get());
                        output.accept(ModItems.METHANE_CONVERTER.get());
                        output.accept(ModItems.REPAIR_MIX.get());
                        output.accept(ModItems.WATER_ESSENCE.get());
                        // 如果需要，可继续添加
                    })
                    .build()
    );
}
