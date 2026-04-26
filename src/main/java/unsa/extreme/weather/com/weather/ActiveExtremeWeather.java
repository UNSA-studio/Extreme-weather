package unsa.extreme.weather.com.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import unsa.extreme.weather.com.config.ModConfigs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ActiveExtremeWeather {
    // ... 所有成员变量保持不变，为了简洁只展示修改部分，实际用完整文件覆盖
    // 这里直接给出完整文件，确保清晰

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

            ServerLevelData data = (ServerLevelData) serverLevel.getLevelData();
            // 强制清空所有原版天气状态，防止残留
            data.setRaining(false);
            data.setThundering(false);
            switch (type) {
                case EXTREME_THUNDERSTORM:
                    data.setRaining(true);
                    data.setThundering(true);
                    data.setRainTime(remainingTicks);
                    data.setThunderTime(remainingTicks);
                    break;
                case SUPER_RAIN:
                    data.setRaining(true);
                    data.setRainTime(remainingTicks);
                    break;
                case EXTREME_BLIZZARD:
                    data.setRaining(true); // 暴风雪使用原版下雨状态，但粒子由客户端处理
                    data.setRainTime(remainingTicks);
                    break;
                case SUPER_DROUGHT:
                case EXTREME_SANDSTORM:
                case SUPER_TYPHOON:
                    // 这些天气保持晴天状态，不设置原版下雨
                    break;
            }
            LOGGER.info("Extreme weather {} started at {} radius {}", type.name(), center, radius);
        }
    }

    // end, tick 等方法保持不变
    // 为了完整，把剩下的全部贴出

    public void end(Level level) {
        active = false;
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            ServerLevelData data = (ServerLevelData) serverLevel.getLevelData();
            data.setRaining(false);
            data.setThundering(false);
            data.setRainTime(0);
            data.setThunderTime(0);
        }
        LOGGER.info("Extreme weather ended: {}", type.name());
    }

    public void tick(ServerLevel level) {
        if (!active) return;
        remainingTicks--;

        ServerLevelData data = (ServerLevelData) level.getLevelData();
        switch (type) {
            case EXTREME_THUNDERSTORM:
                if (!data.isRaining()) data.setRaining(true);
                if (!data.isThundering()) data.setThundering(true);
                data.setRainTime(remainingTicks);
                data.setThunderTime(remainingTicks);
                break;
            case SUPER_RAIN:
            case EXTREME_BLIZZARD:
                if (!data.isRaining()) data.setRaining(true);
                data.setRainTime(remainingTicks);
                break;
            default:
                if (data.isRaining()) data.setRaining(false);
                if (data.isThundering()) data.setThundering(false);
                break;
        }

        if (remainingTicks % 100 == 0) updateDirectionToNearestPlayer(level);
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

    private void tickThunderstorm(ServerLevel level) {
        if (remainingTicks % 100 != 0) return;
        if (level.random.nextFloat() >= 0.7f) return;
        for (ServerPlayer player : level.players()) {
            if (!isInRange(player.blockPosition())) continue;
            int x = player.blockPosition().getX() + level.random.nextInt(20) - 10;
            int z = player.blockPosition().getZ() + level.random.nextInt(20) - 10;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            bolt.setPos(x, surfaceY + 1, z);
            level.addFreshEntity(bolt);
        }
    }

    private void tickSuperRain(ServerLevel level) {
        if (remainingTicks % 100 != 0) return;
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        int viewDistance = level.getServer().getPlayerList().getViewDistance();
        int viewDistanceBlocks = viewDistance * 16;
        List<BlockPos> validPositions = new ArrayList<>();
        for (ServerPlayer player : players) {
            BlockPos playerPos = player.blockPosition();
            int minX = playerPos.getX() - viewDistanceBlocks;
            int maxX = playerPos.getX() + viewDistanceBlocks;
            int minZ = playerPos.getZ() - viewDistanceBlocks;
            int maxZ = playerPos.getZ() + viewDistanceBlocks;
            for (int x = minX; x <= maxX; x += 4) {
                for (int z = minZ; z <= maxZ; z += 4) {
                    BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(x, playerPos.getY(), z);
                    while (checkPos.getY() > level.getMinBuildHeight() && level.getBlockState(checkPos).isAir()) {
                        checkPos.setY(checkPos.getY() - 1);
                    }
                    BlockPos airPos = checkPos.above();
                    BlockState groundState = level.getBlockState(checkPos);
                    if (level.getBlockState(airPos).isAir() && groundState.isCollisionShapeFullBlock(level, checkPos)) {
                        validPositions.add(airPos.immutable());
                    }
                }
            }
        }
        if (validPositions.isEmpty()) return;
        Collections.shuffle(validPositions, RANDOM);
        int toFill = Math.min(10, validPositions.size());
        for (int i = 0; i < toFill; i++) {
            level.setBlock(validPositions.get(i), Blocks.WATER.defaultBlockState(), 3);
        }
    }

    private void tickSuperDrought(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
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
    private void tickTyphoon(ServerLevel level) {
        if (ModConfigs.typhoonThrowBlocks.get() && remainingTicks % 200 == 0) {
            for (ServerPlayer player : level.players()) {
                if (!isInRange(player.blockPosition())) continue;
                int x = player.blockPosition().getX() + level.random.nextInt(40) - 20;
                int z = player.blockPosition().getZ() + level.random.nextInt(40) - 20;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                BlockPos surfacePos = new BlockPos(x, surfaceY, z);
                BlockState state = level.getBlockState(surfacePos);
                if (!state.isAir() && state.getDestroySpeed(level, surfacePos) >= 0 && !state.liquid()) {
                    net.minecraft.world.item.ItemStack drop = new net.minecraft.world.item.ItemStack(state.getBlock().asItem());
                    net.minecraft.world.entity.item.ItemEntity itemEntity =
                        new net.minecraft.world.entity.item.ItemEntity(level, x + 0.5, surfaceY + 2, z + 0.5, drop);
                    itemEntity.setDeltaMovement(
                        (level.random.nextDouble() - 0.5) * 2,
                        level.random.nextDouble() * 1.5 + 0.5,
                        (level.random.nextDouble() - 0.5) * 2
                    );
                    level.addFreshEntity(itemEntity);
                    level.removeBlock(surfacePos, false);
                }
            }
        }
    }

    private void updateDirectionToNearestPlayer(ServerLevel level) {
        Player nearest = findNearestPlayer(level, center);
        if (nearest != null) {
            if (RANDOM.nextDouble() < 0.15) {
                moveDirection = new Vec3(RANDOM.nextDouble() - 0.5, 0, RANDOM.nextDouble() - 0.5).normalize();
            } else {
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
