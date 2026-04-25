package unsa.extreme.weather.com.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import unsa.extreme.weather.com.blockentity.WeatherStationBlockEntity;
import unsa.extreme.weather.com.weather.ActiveExtremeWeather;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import java.util.Map;

public class WeatherRadarScreen extends Screen {
    private final WeatherStationBlockEntity station;

    public WeatherRadarScreen(WeatherStationBlockEntity station) {
        super(Component.literal("天气雷达"));
        this.station = station;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int y = 20;

        graphics.drawString(font, "§l=== 极端天气雷达 ===", 10, y, 0xFFD700);
        y += 15;

        if (!station.isBalloonDeployed()) {
            graphics.drawString(font, "§7气象气球未释放", 10, y, 0xAAAAAA);
            y += 12;
            graphics.drawString(font, "§7请使用气象气球右键探测站", 10, y, 0xAAAAAA);
            return;
        }

        // 污染值
        double pollution = station.getPollutionRead();
        String pollutionColor = pollution < 400 ? "§a" : pollution < 600 ? "§e" : "§c";
        graphics.drawString(font, "当前污染值: " + pollutionColor + String.format("%.1f%%", pollution), 10, y, 0xFFFFFF);
        y += 12;

        // 各天气概率
        graphics.drawString(font, "§l各天气概率:", 10, y, 0xFFFFFF);
        y += 12;
        Map<ExtremeWeatherType, Double> probs = station.getWeatherProbabilities();
        if (probs != null) {
            for (Map.Entry<ExtremeWeatherType, Double> entry : probs.entrySet()) {
                String name = switch (entry.getKey()) {
                    case EXTREME_THUNDERSTORM -> "极端雷暴";
                    case SUPER_RAIN -> "超级暴雨";
                    case SUPER_DROUGHT -> "超级干旱";
                    case EXTREME_SANDSTORM -> "极端沙尘暴";
                    case EXTREME_BLIZZARD -> "极端暴风雪";
                    case SUPER_TYPHOON -> "超级台风";
                };
                graphics.drawString(font, "  " + name + ": §e" + String.format("%.1f%%", entry.getValue() * 100), 10, y, 0xAAAAAA);
                y += 12;
            }
        }

        y += 5;
        // 附近天气
        ActiveExtremeWeather weather = station.getNearbyWeather();
        if (weather != null) {
            int dx = Math.abs(weather.getCenter().getX() - station.getBlockPos().getX()) >> 4;
            int dz = Math.abs(weather.getCenter().getZ() - station.getBlockPos().getZ()) >> 4;
            if (dx < 32 && dz < 32) {
                String wtype = switch (weather.type) {
                    case EXTREME_THUNDERSTORM -> "极端雷暴";
                    case SUPER_RAIN -> "超级暴雨";
                    case SUPER_DROUGHT -> "超级干旱";
                    case EXTREME_SANDSTORM -> "极端沙尘暴";
                    case EXTREME_BLIZZARD -> "极端暴风雪";
                    case SUPER_TYPHOON -> "超级台风";
                };
                graphics.drawString(font, "§l附近天气: §c" + wtype, 10, y, 0xFF5555);
                y += 12;
                graphics.drawString(font, "距离: §e" + Math.max(dx, dz) + " 区块", 10, y, 0xCCCCCC);
                y += 12;
                if (weather.getMoveDirection() != null) {
                    String dir = getDirectionName(weather.getMoveDirection().x, weather.getMoveDirection().z);
                    graphics.drawString(font, "移动方向: §e" + dir, 10, y, 0xCCCCCC);
                    y += 12;
                }
                graphics.drawString(font, "剩余时间: §e" + (weather.remainingTicks / 1200) + " 分钟", 10, y, 0xCCCCCC);
                y += 12;
            } else {
                graphics.drawString(font, "§7附近32区块内无极端天气", 10, y, 0xAAAAAA);
            }
        } else {
            graphics.drawString(font, "§7附近32区块内无极端天气", 10, y, 0xAAAAAA);
        }
    }

    private String getDirectionName(double dx, double dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? "东" : "西";
        } else {
            return dz > 0 ? "南" : "北";
        }
    }
}
