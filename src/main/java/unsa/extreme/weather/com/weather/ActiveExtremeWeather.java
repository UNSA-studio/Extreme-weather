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
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class ActiveExtremeWeather {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final ExtremeWeatherType type;
    public int remainingTicks;
    private boolean active = false;
    private boolean omenPhase = false;

    public ActiveExtremeWeather(ExtremeWeatherType type, int duration) {
        this.type = type;
        this.omenPhase = duration <= 0;
        this.remainingTicks = duration > 0 ? duration : 24000;
    }

    /** 征兆阶段 */
    public void beginOmen(Level level) {
        omenPhase = true;
        active = false;
        remainingTicks = 24000;
        if (!level.isClientSide)
            LOGGER.info("Omen started: {}", type.name());
    }

    /** 正式爆发 */
    public void begin(Level level) {
        omenPhase = false;
        active = true;
        remainingTicks = getDefaultDuration();
        if (!level.isClientSide) {
            LOGGER.info("Extreme weather started: {}", type.name());
            if (level instanceof ServerLevel sl) {
                switch (type) {
                    case EXTREME_THUNDERSTORM -> sl.setWeatherParameters(6000, 72000, true, true);
                    case SUPER_RAIN -> sl.setWeatherParameters(6000, 72000, true, false);
                    case EXTREME_BLIZZARD -> sl.setWeatherParameters(6000, 72000, true, false);
                    case SUPER_TYPHOON -> sl.setWeatherParameters(6000, 72000, true, false);
                }
            }
        }
    }

    public void end(Level level) {
        active = false;
        omenPhase = false;
        if (!level.isClientSide) {
            LOGGER.info("Extreme weather ended: {}", type.name());
            if (level instanceof ServerLevel sl)
                sl.setWeatherParameters(0, 0, false, false);
        }
    }

    public void tick(ServerLevel level) {
        remainingTicks--;
        if (omenPhase) {
            if (remainingTicks <= 0) {
                begin(level);
            } else {
                tickOmen(level);
            }
            return;
        }
        if (!active) return;

        switch (type) {
            case EXTREME_THUNDERSTORM -> tickThunderstorm(level);
            case SUPER_RAIN -> tickSuperRain(level);
            case SUPER_DROUGHT -> tickSuperDrought(level);
            case EXTREME_SANDSTORM -> tickSandstorm(level);
            case EXTREME_BLIZZARD -> tickBlizzard(level);
            case SUPER_TYPHOON -> tickTyphoon(level);
        }
    }

    private void tickOmen(ServerLevel level) {
        switch (type) {
            case EXTREME_THUNDERSTORM:
                level.setWeatherParameters(Math.min(300, 300), 0, false, true);
                break;
            case SUPER_RAIN:
                level.setWeatherParameters(300, 0, true, false);
                break;
            case EXTREME_BLIZZARD:
                int progress = remainingTicks / 24000;
                level.setWeatherParameters(300, 0, true, false);
                break;
            case SUPER_DROUGHT:
                for (Player player : level.players()) {
                    if (level.canSeeSky(player.blockPosition())) {
                        player.setRemainingFireTicks(200);
                    }
                }
                break;
            case SUPER_TYPHOON:
                level.setWeatherParameters(0, 0, false, false);
                break;
            default:
                break;
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
                player.setRemainingFireTicks(300);
            }
        }
    }

    private void tickSandstorm(ServerLevel level) {}
    private void tickBlizzard(ServerLevel level) {}
    private void tickTyphoon(ServerLevel level) {}

    public boolean isExpired() { return remainingTicks <= 0 && !omenPhase; }
    public boolean isOmenPhase() { return omenPhase; }

    private int getDefaultDuration() {
        return 24000 * (switch (type) {
            case EXTREME_THUNDERSTORM, SUPER_RAIN -> 5 + new java.util.Random().nextInt(2);
            case SUPER_DROUGHT -> 3 + new java.util.Random().nextInt(3);
            case EXTREME_SANDSTORM, EXTREME_BLIZZARD -> 5 + new java.util.Random().nextInt(6);
            case SUPER_TYPHOON -> 10 + new java.util.Random().nextInt(4);
        });
    }
}
