package unsa.extreme.weather.com.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

            ServerLevelData data = (ServerLevelData) serverLevel.getLevelData();
            switch (type) {
                case EXTREME_THUNDERSTORM:
                    data.setRaining(true);
                    data.setThundering(true);
                    data.setRainTime(remainingTicks);
                    data.setThunderTime(remainingTicks);
                    break;
                case SUPER_RAIN:
                    data.setRaining(true);
                    data.setThundering(false);
                    data.setRainTime(remainingTicks);
                    break;
                case EXTREME_BLIZZARD:
                    data.setRaining(true);
                    data.setThundering(false);
                    data.setRainTime(remainingTicks);
                    break;
                case SUPER_DROUGHT:
                case EXTREME_SANDSTORM:
                case SUPER_TYPHOON:
                    data.setRaining(false);
                    data.setThundering(false);
                    break;
            }
            LOGGER.info("Extreme weather {} started at {} radius {}", type.name(), center, radius);
        }
    }

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
            case SUPER_DROUGHT:
            case EXTREME_SANDSTORM:
            case SUPER_TYPHOON:
                if (data.isRaining()) data.setRaining(false);
                if (data.isThundering()) data.setThundering(false);
                break;
        }

        if (remainingTicks % 100 == 0) {
            updateDirectionToNearestPlayer(level);
        }
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

    private void tickSuperRain(ServerLevel level) {
        // 每5秒（100 ticks）执行一次
        if (remainingTicks % 100 != 0) return;
        // 获取所有在线玩家
        List<Player> players = level.players();
        if (players.isEmpty()) return;

        // 收集所有玩家渲染视距内的低洼处（空气，下方为实心方块）
        List<BlockPos> validPositions = new ArrayList<>();
        int viewDistance = level.getServer().getPlayerList().getViewDistance(); // 区块半径
        for (Player player : players) {
            BlockPos playerPos = player.blockPosition();
            int minX = playerPos.getX() - (viewDistance * 16);
            int maxX = playerPos.getX() + (viewDistance * 16);
            int minZ = playerPos.getZ() - (viewDistance * 16);
            int maxZ = playerPos.getZ() + (viewDistance * 16);
            // 遍历范围内的所有方块（简化：只检查水平范围，高度取玩家附近y）
            for (int x = minX; x <= maxX; x += 4) { // 步长4以减少计算量
                for (int z = minZ; z <= maxZ; z += 4) {
                    // 获取该列的地表高度
                    BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(x, playerPos.getY(), z);
                    // 向下搜索直到找到地面
                    while (checkPos.getY() > level.getMinBuildHeight() && level.getBlockState(checkPos).isAir()) {
                        checkPos.setY(checkPos.getY() - 1);
                    }
                    // 此时checkPos是地面方块，其上应为空气
                    BlockPos airPos = checkPos.above();
                    BlockState groundState = level.getBlockState(checkPos);
                    if (level.getBlockState(airPos).isAir() && groundState.isCollisionShapeFullBlock(level, checkPos)) {
                        validPositions.add(airPos.immutable());
                    }
                }
            }
        }

        if (validPositions.isEmpty()) return;

        // 随机打乱并取最多10个位置
        Collections.shuffle(validPositions, RANDOM);
        int toFill = Math.min(10, validPositions.size());
        for (int i = 0; i < toFill; i++) {
            BlockPos pos = validPositions.get(i);
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
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
