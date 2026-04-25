package unsa.extreme.weather.com.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.Random;

public class ActiveExtremeWeather {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    public final ExtremeWeatherType type;
    public int remainingTicks;
    private boolean active;

    private BlockPos center;
    private int radius;
    private Vec3 moveDirection;
    private double speed;

    public ActiveExtremeWeather(ExtremeWeatherType type, int duration) {
        this.type = type;
        this.remainingTicks = duration;
    }

    public void begin(Level level) {
        active = true;
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Player target = findNearestPlayer(serverLevel, serverLevel.getSharedSpawnPos());
            if (target != null) {
                center = target.blockPosition();
            } else if (!serverLevel.players().isEmpty()) {
                center = serverLevel.players().get(RANDOM.nextInt(serverLevel.players().size())).blockPosition();
            } else {
                center = serverLevel.getSharedSpawnPos();
            }
            radius = getDefaultRadius();
            speed = 0.02 + RANDOM.nextDouble() * 0.02;
            updateDirectionToNearestPlayer(serverLevel);
            LOGGER.info("Extreme weather {} started at {} radius {}", type.name(), center, radius);
        }
    }

    public void end(Level level) {
        active = false;
        if (!level.isClientSide) LOGGER.info("Extreme weather ended: {}", type.name());
    }

    public void tick(ServerLevel level) {
        if (!active) return;
        remainingTicks--;

        // 每5秒更新一次方向，以追击最近玩家
        if (remainingTicks % 100 == 0) {
            updateDirectionToNearestPlayer(level);
        }

        // 移动中心
        if (moveDirection != null && center != null) {
            Vec3 newCenter = Vec3.atCenterOf(center).add(moveDirection.scale(speed));
            center = BlockPos.containing(newCenter.x, newCenter.y, newCenter.z);
        }

        switch (type) {
            case EXTREME_THUNDERSTORM -> tickThunderstorm(level);
            case SUPER_RAIN -> tickSuperRain(level);
            case SUPER_DROUGHT -> tickSuperDrought(level);
            case EXTREME_SANDSTORM -> tickSandstorm(level);
            case EXTREME_BLIZZARD -> tickBlizzard(level);
            case SUPER_TYPHOON -> tickTyphoon(level);
        }
    }

    private void updateDirectionToNearestPlayer(ServerLevel level) {
        Player nearest = findNearestPlayer(level, center);
        if (nearest != null) {
            if (RANDOM.nextDouble() < 0.15) {
                // 15% 概率随机移动
                moveDirection = new Vec3(RANDOM.nextDouble() - 0.5, 0, RANDOM.nextDouble() - 0.5).normalize();
            } else {
                // 追踪最近玩家
                Vec3 toPlayer = nearest.position().subtract(Vec3.atCenterOf(center));
                moveDirection = toPlayer.normalize();
            }
        }
    }

    private Player findNearestPlayer(ServerLevel level, BlockPos from) {
        Player closest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : level.players()) {
            double dist = p.blockPosition().distSqr(from);
            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }
        return closest;
    }

    private void tickThunderstorm(ServerLevel level) {
        if (level.random.nextFloat() < 0.7f) {
            for (Player player : level.players()) {
                if (isInRange(player.blockPosition())) {
                    BlockPos pos = player.blockPosition().offset(
                        level.random.nextInt(20) - 10, 0, level.random.nextInt(20) - 10);
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                    bolt.setPos(pos.getX(), pos.getY(), pos.getZ());
                    level.addFreshEntity(bolt);
                }
            }
        }
    }

    private void tickSuperRain(ServerLevel level) {
        if (level.random.nextFloat() < 0.1f) {
            for (Player player : level.players()) {
                if (!isInRange(player.blockPosition())) continue;
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
            if (!isInRange(player.blockPosition())) continue;
            boolean exposed = level.canSeeSky(player.blockPosition());
            if (exposed) {
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
                Holder<ArmorMaterial> holder = ai.getMaterial();
                ArmorMaterial mat = holder.value();
                if (mat == net.minecraft.world.item.ArmorMaterials.IRON.value() ||
                    mat == net.minecraft.world.item.ArmorMaterials.DIAMOND.value() ||
                    mat == net.minecraft.world.item.ArmorMaterials.NETHERITE.value()) {
                    protection++;
                }
            }
        }
        return protection;
    }

    public boolean isExpired() { return remainingTicks <= 0; }

    public boolean isInRange(BlockPos pos) {
        if (center == null) return false;
        int dx = Math.abs(pos.getX() - center.getX());
        int dz = Math.abs(pos.getZ() - center.getZ());
        return dx <= radius && dz <= radius;
    }

    public BlockPos getCenter() { return center; }
    public int getRadius() { return radius; }
    public Vec3 getMoveDirection() { return moveDirection; }

    private int getDefaultRadius() {
        return switch (type) {
            case SUPER_TYPHOON -> 256;
            case EXTREME_THUNDERSTORM, SUPER_RAIN -> 160;
            default -> 200;
        };
    }
}
