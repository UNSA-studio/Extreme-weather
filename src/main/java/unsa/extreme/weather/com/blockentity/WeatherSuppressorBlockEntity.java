package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.weather.ExtremeWeatherManager;

public class WeatherSuppressorBlockEntity extends BlockEntity {
    private boolean active = false;
    private int powerTicks = 0; // 剩余供电ticks

    public WeatherSuppressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEATHER_SUPPRESSOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WeatherSuppressorBlockEntity be) {
        if (level.isClientSide) return;
        // 简化供电检测：需红石块提供能量，每10分钟消耗一个红石块
        boolean hasPower = level.hasNeighborSignal(pos);
        if (hasPower) {
            be.powerTicks = Math.max(be.powerTicks, 12000); // 10分钟 = 12000 ticks
        }
        if (be.powerTicks > 0) {
            be.powerTicks--;
            if (!be.active) {
                be.active = true;
                ExtremeWeatherManager.addSafeZone(level, pos, 32);
            }
        } else {
            if (be.active) {
                be.active = false;
                ExtremeWeatherManager.removeSafeZone(level, pos);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        active = tag.getBoolean("Active");
        powerTicks = tag.getInt("PowerTicks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Active", active);
        tag.putInt("PowerTicks", powerTicks);
    }
}
