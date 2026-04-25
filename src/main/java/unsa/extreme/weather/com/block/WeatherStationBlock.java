package unsa.extreme.weather.com.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.client.Minecraft;
import unsa.extreme.weather.com.blockentity.WeatherStationBlockEntity;
import unsa.extreme.weather.com.client.gui.WeatherRadarScreen;
import unsa.extreme.weather.com.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class WeatherStationBlock extends Block implements EntityBlock {
    public WeatherStationBlock(Properties p) { super(p); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeatherStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.WEATHER_STATION.get()) {
            return (BlockEntityTicker<T>)(BlockEntityTicker<WeatherStationBlockEntity>)WeatherStationBlockEntity::tick;
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WeatherStationBlockEntity station) {
                Minecraft.getInstance().setScreen(new WeatherRadarScreen(station));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
