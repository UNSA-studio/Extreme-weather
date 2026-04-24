package unsa.extreme.weather.com.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.blockentity.WeatherSuppressorBlockEntity;
import unsa.extreme.weather.com.init.ModBlockEntities;

import javax.annotation.Nullable;

public class WeatherSuppressorBlock extends Block implements EntityBlock {
    public WeatherSuppressorBlock(Properties p) { super(p); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeatherSuppressorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.WEATHER_SUPPRESSOR.get() ? WeatherSuppressorBlockEntity::tick : null;
    }
}
