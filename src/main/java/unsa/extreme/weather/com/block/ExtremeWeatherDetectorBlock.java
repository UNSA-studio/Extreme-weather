package unsa.extreme.weather.com.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.client.Minecraft;
import unsa.extreme.weather.com.blockentity.ExtremeWeatherDetectorBlockEntity;
import unsa.extreme.weather.com.client.gui.ExtremeWeatherDetectorScreen;

public class ExtremeWeatherDetectorBlock extends Block {
    public ExtremeWeatherDetectorBlock(Properties p) { super(p); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ExtremeWeatherDetectorBlockEntity detector) {
                Minecraft.getInstance().setScreen(new ExtremeWeatherDetectorScreen(detector));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
