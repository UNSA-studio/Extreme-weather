package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.init.ModSounds;
import unsa.extreme.weather.com.weather.ExtremeWeatherManager;

public class AlarmBlockEntity extends BlockEntity {
    private BlockPos linkedDetectorPos = null;
    private boolean alarming = false;

    public AlarmBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALARM_BLOCK.get(), pos, state);
    }

    public void setLinkedDetector(BlockPos pos) {
        this.linkedDetectorPos = pos;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlarmBlockEntity be) {
        if (level.isClientSide) return;
        if (be.linkedDetectorPos == null) return;
        if (level.getBlockEntity(be.linkedDetectorPos) instanceof ExtremeWeatherDetectorBlockEntity detector) {
            WeatherStationBlockEntity ws = detector.getLinkedStationEntity();
            if (ws != null && ws.isBalloonDeployed()) {
                boolean imminent = ExtremeWeatherManager.isExtremeWeatherImminent(level, 1200);
                if (imminent && !be.alarming) {
                    be.alarming = true;
                    level.playSound(null, pos, ModSounds.ALARM.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                } else if (!imminent && be.alarming) {
                    be.alarming = false;
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("LinkedDetectorPos")) {
            linkedDetectorPos = BlockPos.of(tag.getLong("LinkedDetectorPos"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (linkedDetectorPos != null) {
            tag.putLong("LinkedDetectorPos", linkedDetectorPos.asLong());
        }
    }
}
