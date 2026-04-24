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
import net.neoforged.neoforge.common.extensions.IEntityBlockExtension;
import unsa.extreme.weather.com.blockentity.PollutionDetectorBlockEntity;
import unsa.extreme.weather.com.client.gui.PollutionDetectorScreen;
import org.jetbrains.annotations.Nullable;

public class PollutionDetectorBlock extends Block implements IEntityBlockExtension {
    public PollutionDetectorBlock(Properties p) { super(p); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PollutionDetectorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PollutionDetectorBlockEntity detector) {
                Minecraft.getInstance().setScreen(new PollutionDetectorScreen(detector));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
