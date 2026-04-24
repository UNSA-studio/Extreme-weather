package unsa.extreme.weather.com.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import unsa.extreme.weather.com.blockentity.PollutionDetectorBlockEntity;

public class PollutionDetectorScreen extends Screen {
    private final PollutionDetectorBlockEntity detector;

    public PollutionDetectorScreen(PollutionDetectorBlockEntity detector) {
        super(Component.literal("Pollution Detector"));
        this.detector = detector;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, "Current Pollution: " + String.format("%.1f", detector.getPollution()) + "%", 10, 20, 0xFFFFFF);
    }
}
