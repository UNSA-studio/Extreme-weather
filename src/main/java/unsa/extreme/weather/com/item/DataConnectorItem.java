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
import net.minecraft.world.item.component.CustomData;

public class DataConnectorItem extends Item {
    public DataConnectorItem(Properties props) {
        super(props);
    }

    private static final String STORED_POS_KEY = "StoredPos";

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag(); // Creates a mutable copy

        if (be instanceof WeatherStationBlockEntity) {
            tag.putLong(STORED_POS_KEY, context.getClickedPos().asLong());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return InteractionResult.SUCCESS;
        } else if (be instanceof ExtremeWeatherDetectorBlockEntity detector) {
            if (tag.contains(STORED_POS_KEY)) {
                BlockPos stationPos = BlockPos.of(tag.getLong(STORED_POS_KEY));
                detector.linkToStation(stationPos);
                tag.remove(STORED_POS_KEY);
                if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
                else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return InteractionResult.SUCCESS;
            }
        } else if (be instanceof AlarmBlockEntity alarm) {
            if (tag.contains(STORED_POS_KEY)) {
                BlockPos detectorPos = BlockPos.of(tag.getLong(STORED_POS_KEY));
                alarm.setLinkedDetector(detectorPos);
                tag.remove(STORED_POS_KEY);
                if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
                else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }
}
