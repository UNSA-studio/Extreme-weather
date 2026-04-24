package unsa.extreme.weather.com.client.render;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import unsa.extreme.weather.com.ExtremeWeather;

@EventBusSubscriber(modid = ExtremeWeather.MODID, value = Dist.CLIENT)
public class WeatherRenderer {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // TODO: 沙尘暴/暴风雪视野遮挡效果
    }
}
