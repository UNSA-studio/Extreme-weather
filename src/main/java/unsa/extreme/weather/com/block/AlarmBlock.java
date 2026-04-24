package unsa.extreme.weather.com.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.blockentity.AlarmBlockEntity;
import unsa.extreme.weather.com.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class AlarmBlock extends Block implements EntityBlock {
    public AlarmBlock(Properties p) { super(p); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlarmBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.ALARM_BLOCK.get()) {
            return (BlockEntityTicker<T>)(BlockEntityTicker<AlarmBlockEntity>)AlarmBlockEntity::tick;
        }
        return null;
    }
}
