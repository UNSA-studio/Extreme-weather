package unsa.extreme.weather.com.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class ActiveExtremeWeather {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final ExtremeWeatherType type;
    public int remainingTicks;
    private boolean active;

    public ActiveExtremeWeather(ExtremeWeatherType type, int duration) {
        this.type = type;
        this.remainingTicks = duration;
    }

    public void begin(Level level) {
        active = true;
        if (!level.isClientSide) LOGGER.info("Extreme weather started: {}", type.name());
    }

    public void end(Level level) {
        active = false;
        if (!level.isClientSide) LOGGER.info("Extreme weather ended: {}", type.name());
    }

    public void tick(ServerLevel level) {
        if (!active) return;
        remainingTicks--;
        switch (type) {
            case EXTREME_THUNDERSTORM -> tickThunderstorm(level);
            case SUPER_RAIN -> tickSuperRain(level);
            case SUPER_DROUGHT -> tickSuperDrought(level);
            case EXTREME_SANDSTORM -> tickSandstorm(level);
            case EXTREME_BLIZZARD -> tickBlizzard(level);
            case SUPER_TYPHOON -> tickTyphoon(level);
        }
    }

    private void tickThunderstorm(ServerLevel level) {
        if (level.random.nextFloat() < 0.02f) {
            for (Player player : level.players()) {
                BlockPos pos = player.blockPosition().offset(
                    level.random.nextInt(20) - 10, 0, level.random.nextInt(20) - 10);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                bolt.setPos(pos.getX(), pos.getY(), pos.getZ());
                level.addFreshEntity(bolt);
            }
        }
    }

    private void tickSuperRain(ServerLevel level) {
        if (level.random.nextFloat() < 0.01f) {
            for (Player player : level.players()) {
                BlockPos pos = player.blockPosition().below();
                if (level.getBlockState(pos).isAir() &&
                    level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }

    private void tickSuperDrought(ServerLevel level) {
        for (Player player : level.players()) {
            if (level.canSeeSky(player.blockPosition())) {
                int protection = getArmorProtection(player);
                int interval = protection >= 2 ? 600 : 200;
                if (player.tickCount % interval == 0) player.setRemainingFireTicks(100);
            }
        }
    }

    private void tickSandstorm(ServerLevel level) {}
    private void tickBlizzard(ServerLevel level) {}
    private void tickTyphoon(ServerLevel level) {}

    private int getArmorProtection(Player player) {
        int protection = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof ArmorItem ai) {
                ArmorMaterials mat = ai.getMaterial();
                if (mat == ArmorMaterials.IRON || mat == ArmorMaterials.DIAMOND || mat == ArmorMaterials.NETHERITE) {
                    protection++;
                }
            }
        }
        return protection;
    }

    public boolean isExpired() { return remainingTicks <= 0; }
}
