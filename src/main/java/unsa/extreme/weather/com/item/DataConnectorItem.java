package unsa.extreme.weather.com.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, new CompoundTag());

        if (be instanceof WeatherStationBlockEntity) {
            tag.putLong("StoredPos", context.getClickedPos().asLong());
            stack.set(DataComponents.CUSTOM_DATA, tag);
            return InteractionResult.SUCCESS;
        } else if (be instanceof ExtremeWeatherDetectorBlockEntity detector) {
            if (tag.contains("StoredPos")) {
                BlockPos stationPos = BlockPos.of(tag.getLong("StoredPos"));
                detector.linkToStation(stationPos);
                tag.remove("StoredPos");
                stack.set(DataComponents.CUSTOM_DATA, tag.isEmpty() ? null : tag);
                return InteractionResult.SUCCESS;
            }
        } else if (be instanceof AlarmBlockEntity alarm) {
            if (tag.contains("StoredPos")) {
                BlockPos detectorPos = BlockPos.of(tag.getLong("StoredPos"));
                alarm.setLinkedDetector(detectorPos);
                tag.remove("StoredPos");
                stack.set(DataComponents.CUSTOM_DATA, tag.isEmpty() ? null : tag);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }
}
