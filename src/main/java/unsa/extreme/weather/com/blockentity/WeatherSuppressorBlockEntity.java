package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.weather.ExtremeWeatherManager;

public class WeatherSuppressorBlockEntity extends BlockEntity {
    private boolean active = false;
    private int powerTicks = 0;

    public WeatherSuppressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEATHER_SUPPRESSOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WeatherSuppressorBlockEntity be) {
        if (level.isClientSide) return;
        boolean hasPower = level.hasNeighborSignal(pos);
        if (hasPower) be.powerTicks = Math.max(be.powerTicks, 12000);
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        active = tag.getBoolean("Active");
        powerTicks = tag.getInt("PowerTicks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Active", active);
        tag.putInt("PowerTicks", powerTicks);
    }
}
