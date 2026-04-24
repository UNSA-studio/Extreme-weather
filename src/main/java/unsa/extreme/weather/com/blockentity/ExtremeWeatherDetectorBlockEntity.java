package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import java.util.Map;

public class ExtremeWeatherDetectorBlockEntity extends BlockEntity {
    private BlockPos linkedStationPos = null;
    private boolean hasData = false;

    public ExtremeWeatherDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTREME_WEATHER_DETECTOR.get(), pos, state);
    }

    public void linkToStation(BlockPos pos) {
        this.linkedStationPos = pos;
        setChanged();
    }

    public boolean isLinked() {
        return linkedStationPos != null;
    }

    public BlockPos getLinkedStationPos() {
        return linkedStationPos;
    }

    public WeatherStationBlockEntity getLinkedStationEntity() {
        if (linkedStationPos != null && level != null) {
            if (level.getBlockEntity(linkedStationPos) instanceof WeatherStationBlockEntity ws) {
                return ws;
            }
        }
        return null;
    }

    // 给GUI提供的数据
    public double getPollution() {
        WeatherStationBlockEntity ws = getLinkedStationEntity();
        return ws != null && ws.isBalloonDeployed() ? ws.getPollutionRead() : -1;
    }

    public Map<ExtremeWeatherType, Double> getProbabilities() {
        WeatherStationBlockEntity ws = getLinkedStationEntity();
        return ws != null && ws.isBalloonDeployed() ? ws.getWeatherProbabilities() : null;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("LinkedStationPos")) {
            linkedStationPos = BlockPos.of(tag.getLong("LinkedStationPos"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (linkedStationPos != null) {
            tag.putLong("LinkedStationPos", linkedStationPos.asLong());
        }
    }
}
