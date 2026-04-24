package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.weather.PollutionManager;

public class PollutionDetectorBlockEntity extends BlockEntity {
    public PollutionDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLLUTION_DETECTOR.get(), pos, state);
    }

    public double getPollution() {
        if (level == null) return 0;
        return PollutionManager.getPollution(level);
    }
}
