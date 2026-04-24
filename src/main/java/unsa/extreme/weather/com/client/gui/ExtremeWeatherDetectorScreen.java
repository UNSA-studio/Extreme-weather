package unsa.extreme.weather.com.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import unsa.extreme.weather.com.blockentity.ExtremeWeatherDetectorBlockEntity;
import unsa.extreme.weather.com.blockentity.WeatherStationBlockEntity;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import java.util.Map;

public class ExtremeWeatherDetectorScreen extends Screen {
    private final ExtremeWeatherDetectorBlockEntity detector;

    public ExtremeWeatherDetectorScreen(ExtremeWeatherDetectorBlockEntity detector) {
        super(Component.literal("Extreme Weather Detector"));
        this.detector = detector;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int y = 20;
        if (!detector.isLinked()) {
            graphics.drawString(font, "Not linked to a Weather Station", 10, y, 0xFF5555);
            return;
        }
        WeatherStationBlockEntity station = detector.getLinkedStationEntity();
        if (station == null || !station.isBalloonDeployed()) {
            graphics.drawString(font, "No data - balloon not deployed", 10, y, 0xFFAA00);
            return;
        }
        graphics.drawString(font, "Pollution: " + String.format("%.1f", detector.getPollution()) + "%", 10, y, 0xFFFFFF);
        y += 12;
        Map<ExtremeWeatherType, Double> probs = detector.getProbabilities();
        if (probs != null) {
            for (Map.Entry<ExtremeWeatherType, Double> entry : probs.entrySet()) {
                graphics.drawString(font, entry.getKey() + ": " + String.format("%.1f", entry.getValue() * 100) + "%", 10, y, 0xCCCCCC);
                y += 12;
            }
        }
    }
}
