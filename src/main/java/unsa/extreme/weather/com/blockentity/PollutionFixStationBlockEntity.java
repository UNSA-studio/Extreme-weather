package unsa.extreme.weather.com.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import unsa.extreme.weather.com.init.ModBlockEntities;
import unsa.extreme.weather.com.item.WeatherMissileItem;
import unsa.extreme.weather.com.weather.PollutionManager;

public class PollutionFixStationBlockEntity extends BlockEntity {
    private ItemStack missileSlot = ItemStack.EMPTY;
    private int cooldown = 0;

    public PollutionFixStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLLUTION_FIX_STATION.get(), pos, state);
    }

    public boolean insertMissile(ItemStack missile) {
        if (missile.getItem() instanceof WeatherMissileItem && missileSlot.isEmpty()) {
            missileSlot = missile.copy();
            missileSlot.setCount(1);
            missile.shrink(1);
            setChanged();
            return true;
        }
        return false;
    }

    public void launch(Direction direction) {
        if (!missileSlot.isEmpty() && cooldown <= 0) {
            // 检查方向无阻挡（简化）
            WeatherMissileItem m = (WeatherMissileItem) missileSlot.getItem();
            PollutionManager.reducePollution(level, m.getRepairPercentage());
            missileSlot = ItemStack.EMPTY;
            cooldown = 200; // 10秒冷却
            setChanged();
        }
    }

    public ItemStack getMissile() {
        return missileSlot;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PollutionFixStationBlockEntity be) {
        if (!level.isClientSide && be.cooldown > 0) {
            be.cooldown--;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        missileSlot = ItemStack.of(tag.getCompound("Missile"));
        cooldown = tag.getInt("Cooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Missile", missileSlot.save(new CompoundTag()));
        tag.putInt("Cooldown", cooldown);
    }
}
