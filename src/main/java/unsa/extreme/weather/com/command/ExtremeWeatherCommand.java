package unsa.extreme.weather.com.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import unsa.extreme.weather.com.weather.ExtremeWeatherManager;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import unsa.extreme.weather.com.weather.PollutionManager;

public class ExtremeWeatherCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("extremeweather")
                .requires(src -> src.hasPermission(2)) // 需要OP权限
                .then(Commands.literal("numericvalue")
                    .executes(ExtremeWeatherCommand::showNumericValues)
                )
                .then(Commands.literal("weather")
                    .then(Commands.literal("generate")
                        .then(Commands.argument("type", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                for (ExtremeWeatherType type : ExtremeWeatherType.values()) {
                                    builder.suggest(type.name().toLowerCase());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ExtremeWeatherCommand::generateWeather)
                        )
                    )
                    .then(Commands.literal("end")
                        .executes(ExtremeWeatherCommand::endWeather)
                    )
                )
        );
    }

    private static int showNumericValues(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        double pollution = PollutionManager.getPollution(level);
        double dailyIncrease = PollutionManager.getEstimatedDailyIncrease(level);
        double chance = ExtremeWeatherManager.getCurrentChance(level);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "§a=== Extreme Weather Numeric Values ===\n" +
            "§fPollution: §c" + String.format("%.2f", pollution) + "%\n" +
            "§fDaily Increase: §e" + String.format("%.2f", dailyIncrease) + "%\n" +
            "§fExtreme Weather Chance: §6" + String.format("%.2f", chance * 100) + "%"
        ), false);
        return 1;
    }

    private static int generateWeather(CommandContext<CommandSourceStack> ctx) {
        String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();
        ExtremeWeatherType type;
        try {
            type = ExtremeWeatherType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("§cInvalid weather type! Available: " +
                    java.util.Arrays.toString(ExtremeWeatherType.values()).toLowerCase()));
            return 0;
        }
        ServerLevel level = ctx.getSource().getLevel();
        ExtremeWeatherManager.forceStartWeather(level, type);
        ctx.getSource().sendSuccess(() -> Component.literal("§aGenerated extreme weather: " + type.name()), true);
        return 1;
    }

    private static int endWeather(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        if (ExtremeWeatherManager.isWeatherActive(level)) {
            ExtremeWeatherManager.forceEndWeather(level);
            ctx.getSource().sendSuccess(() -> Component.literal("§aCurrent extreme weather ended."), true);
        } else {
            ctx.getSource().sendFailure(Component.literal("§cNo extreme weather is currently active."));
        }
        return 1;
    }
}
