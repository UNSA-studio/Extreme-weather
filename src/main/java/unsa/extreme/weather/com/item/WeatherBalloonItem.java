package unsa.extreme.weather.com.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import unsa.extreme.weather.com.blockentity.WeatherStationBlockEntity;

public class WeatherBalloonItem extends Item {
    public WeatherBalloonItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof WeatherStationBlockEntity station) {
            station.deployBalloon();
            context.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return super.useOn(context);
    }
}
