package unsa.extreme.weather.com.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import unsa.extreme.weather.com.blockentity.ExtremeWeatherDetectorBlockEntity;
import unsa.extreme.weather.com.blockentity.AlarmBlockEntity;
import unsa.extreme.weather.com.blockentity.WeatherStationBlockEntity;

public class DataConnectorItem extends Item {
    public DataConnectorItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof WeatherStationBlockEntity) {
            // 存储到nbt
            CompoundTag tag = context.getItemInHand().getOrCreateTag();
            tag.putLong("StoredPos", context.getClickedPos().asLong());
            return InteractionResult.SUCCESS;
        } else if (be instanceof ExtremeWeatherDetectorBlockEntity detector) {
            CompoundTag tag = context.getItemInHand().getTag();
            if (tag != null && tag.contains("StoredPos")) {
                BlockPos stationPos = BlockPos.of(tag.getLong("StoredPos"));
                detector.linkToStation(stationPos);
                context.getItemInHand().removeTagKey("StoredPos");
                return InteractionResult.SUCCESS;
            }
        } else if (be instanceof AlarmBlockEntity alarm) {
            CompoundTag tag = context.getItemInHand().getTag();
            if (tag != null && tag.contains("StoredPos")) {
                BlockPos detectorPos = BlockPos.of(tag.getLong("StoredPos"));
                alarm.setLinkedDetector(detectorPos);
                context.getItemInHand().removeTagKey("StoredPos");
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }
}
