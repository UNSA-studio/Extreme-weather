package unsa.extreme.weather.com.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfigs {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.DoubleValue initialPollution;
    public static ModConfigSpec.DoubleValue extremeWeatherBaseChance;
    public static ModConfigSpec.BooleanValue typhoonThrowBlocks;

    static {
        BUILDER.push("general");
        initialPollution = BUILDER
                .comment("Initial pollution percentage (0-1000)")
                .defineInRange("initialPollution", 300.0, 0.0, 1000.0);
        extremeWeatherBaseChance = BUILDER
                .comment("Base chance of extreme weather (0.0-1.0)")
                .defineInRange("baseChance", 0.7, 0.0, 1.0);
        typhoonThrowBlocks = BUILDER
                .comment("Whether typhoons can throw blocks (may cause server lag, default off)")
                .define("typhoonThrowBlocks", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
