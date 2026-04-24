package unsa.extreme.weather.com.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import unsa.extreme.weather.com.ExtremeWeather;
import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ExtremeWeather.MODID);
    public static final Supplier<SoundEvent> ALARM = SOUNDS.register("alarm",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ExtremeWeather.MODID, "alarm")));
}
