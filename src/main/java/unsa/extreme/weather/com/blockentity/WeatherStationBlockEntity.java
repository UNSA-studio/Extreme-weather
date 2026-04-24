package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.weather.PollutionManager;
import unsa.extreme.weather.com.weather.ExtremeWeatherManager;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;
import java.util.Map;

public class WeatherStationBlockEntity extends BlockEntity {
    private boolean balloonDeployed = false;
    private long deployTime = -1;

    public WeatherStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEATHER_STATION.get(), pos, state);
    }

    public void deployBalloon() {
        if (!balloonDeployed && level != null) {
            balloonDeployed = true;
            deployTime = level.getGameTime();
            setChanged();
        }
    }

    public boolean isBalloonDeployed() {
        return balloonDeployed;
    }

    public double getPollutionRead() {
        return PollutionManager.getPollution(level);
    }

    public Map<ExtremeWeatherType, Double> getWeatherProbabilities() {
        return ExtremeWeatherManager.getCurrentProbabilities(level);
    }

    public long getDeployTime() {
        return deployTime;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WeatherStationBlockEntity entity) {
        // 可以加入周期检查逻辑
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        balloonDeployed = tag.getBoolean("BalloonDeployed");
        deployTime = tag.getLong("DeployTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("BalloonDeployed", balloonDeployed);
        tag.putLong("DeployTime", deployTime);
    }
}
