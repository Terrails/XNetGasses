package terrails.xnetgases.module;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChemicalHelper {

    public static boolean blockSupportsChemicals(@Nullable BlockEntity be, @Nullable Direction direction) {
        if (be instanceof TileEntityMekanism te) {
            return te.canHandleChemicals();
        } else if (be != null) {
            return Capabilities.CHEMICAL.getCapabilityIfLoaded(be.getLevel(), be.getBlockPos(), null, be, direction) != null;
        } else {
            return false;
        }
    }

    public static boolean blockSupportsChemicals(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable Direction direction) {
        return blockSupportsChemicals(level.getBlockEntity(pos), direction);
    }
}
