package unsa.extreme.weather.com.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfigs {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.DoubleValue initialPollution;
    public static ModConfigSpec.DoubleValue extremeWeatherBaseChance;

    static {
        BUILDER.push("general");
        initialPollution = BUILDER
                .comment("Initial pollution percentage (0-1000)")
                .defineInRange("initialPollution", 300.0, 0.0, 1000.0);
        extremeWeatherBaseChance = BUILDER
                .comment("Base chance of extreme weather (0.0-1.0)")
                .defineInRange("baseChance", 0.7, 0.0, 1.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
