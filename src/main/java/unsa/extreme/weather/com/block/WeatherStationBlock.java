package unsa.extreme.weather.com.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.blockentity.WeatherStationBlockEntity;
import unsa.extreme.weather.com.init.ModBlockEntities;

import javax.annotation.Nullable;

public class WeatherStationBlock extends Block implements EntityBlock {
    public WeatherStationBlock(Properties p) { super(p); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeatherStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.WEATHER_STATION.get() ? WeatherStationBlockEntity::tick : null;
    }
}
