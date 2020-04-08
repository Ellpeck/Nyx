package de.ellpeck.nyx.items;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class FallenStar extends Item {

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (entityItem.world.isRemote)
            return false;
        if (entityItem.world.isDaytime()) {
            entityItem.setDead();
            return true;
        }

        String lastOnGround = Nyx.ID + ":last_on_ground";
        if (entityItem.onGround && !entityItem.getEntityData().getBoolean(lastOnGround)) {
            entityItem.getEntityData().setBoolean(lastOnGround, true);
            this.placeStarAir(entityItem);
        }
        return false;
    }

    private void placeStarAir(EntityItem entityItem) {
        BlockPos pos = entityItem.getPosition();
        if (entityItem.world.getBlockState(pos).getBlock().isReplaceable(entityItem.world, pos)) {
            entityItem.world.setBlockState(pos, Registry.starAir.getDefaultState());
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos offset = pos.add(x, y, z);
                    if (entityItem.world.getBlockState(offset).getBlock().isReplaceable(entityItem.world, offset)) {
                        entityItem.world.setBlockState(offset, Registry.starAir.getDefaultState());
                        return;
                    }
                }
            }
        }
    }
}
