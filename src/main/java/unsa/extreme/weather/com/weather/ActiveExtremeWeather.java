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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class ActiveExtremeWeather {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final ExtremeWeatherType type;
    public int remainingTicks;
    private boolean active = false;
    private boolean omenPhase = true; // 默认为征兆阶段，直到begin被调用

    // 使用 -1 表示仍在征兆中
    public ActiveExtremeWeather(ExtremeWeatherType type, int duration) {
        this.type = type;
        this.remainingTicks = duration > 0 ? duration : 24000; // 征兆默认1天
        this.omenPhase = duration <= 0;
    }

    /** 开始征兆（启动） */
    public void beginOmen(Level level) {
        omenPhase = true;
        remainingTicks = 24000; // 征兆持续1天
        if (!level.isClientSide)
            LOGGER.info("Extreme weather omens started: {}", type.name());
        applyOmenEffects(level);
    }

    /** 正式开始（征兆结束后或强制触发） */
    public void begin(Level level) {
        omenPhase = false;
        active = true;
        if (!level.isClientSide)
            LOGGER.info("Extreme weather started: {}", type.name());
    }

    /** 结束 */
    public void end(Level level) {
        active = false;
        omenPhase = false;
        if (!level.isClientSide)
            LOGGER.info("Extreme weather ended: {}", type.name());
        // 恢复原版天气
        level.setWeatherParameters(0, 0, false, false);
    }

    public void tick(ServerLevel level) {
        remainingTicks--;
        if (omenPhase) {
            if (remainingTicks <= 0) {
                // 征兆结束，正式爆发
                begin(level);
                remainingTicks = getDefaultDuration();
                applyBeginningEffects(level);
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

    /* ---------- 征兆效果 ---------- */
    private void tickOmen(ServerLevel level) {
        switch (type) {
            case EXTREME_THUNDERSTORM:
                // 天空变暗
                level.setWeatherParameters(Math.min(300, 300), 0, false, true);
                break;
            case SUPER_RAIN:
                level.setWeatherParameters(300, 0, true, false);
                break;
            case EXTREME_BLIZZARD:
                // 逐渐下小雪
                int progress = 1 - remainingTicks / 24000;
                level.setWeatherParameters(progress * 500, 0, true, false);
                break;
            case SUPER_DROUGHT:
                // 给予室外玩家炎热效果（不可自动消除）
                for (Player player : level.players()) {
                    if (level.canSeeSky(player.blockPosition())) {
                        player.addEffect(new MobEffectInstance(MobEffects.BURNING, 100, 0, false, true));
                    }
                }
                break;
            case SUPER_TYPHOON:
                // 云几乎消失
                level.setWeatherParameters(0, 0, false, false);
                break;
            default:
                break;
        }
    }

    /* ---------- 天气正式爆发后的效果 ---------- */
    private void applyBeginningEffects(ServerLevel level) {
        switch (type) {
            case EXTREME_THUNDERSTORM -> level.setWeatherParameters(300, 72000, true, true);
            case SUPER_RAIN -> level.setWeatherParameters(300, 72000, true, false);
            case EXTREME_BLIZZARD -> level.setWeatherParameters(300, 72000, true, false); // 雪
            case SUPER_TYPHOON -> level.setWeatherParameters(300, 72000, true, false);
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
                player.addEffect(new MobEffectInstance(MobEffects.BURNING, 200, 0, false, true));
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
                Holder<ArmorMaterial> holder = ai.getMaterial();
                ArmorMaterial mat = holder.value();
                if (mat == ArmorMaterials.IRON.value() || mat == ArmorMaterials.DIAMOND.value() || mat == ArmorMaterials.NETHERITE.value()) {
                    protection++;
                }
            }
        }
        return protection;
    }

    public boolean isExpired() { return remainingTicks <= 0; }
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
