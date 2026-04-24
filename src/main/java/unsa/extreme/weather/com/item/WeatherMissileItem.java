package unsa.extreme.weather.com.item;

import net.minecraft.world.item.Item;

public class WeatherMissileItem extends Item {
    private final int tier;
    private final int repairPercent;

    public WeatherMissileItem(Properties props, int tier, int repairPercent) {
        super(props);
        this.tier = tier;
        this.repairPercent = repairPercent;
    }

    public int getTier() {
        return tier;
    }

    public int getRepairPercentage() {
        return repairPercent;
    }
}
