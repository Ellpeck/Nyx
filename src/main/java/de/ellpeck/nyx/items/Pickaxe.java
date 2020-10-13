package de.ellpeck.nyx.items;

import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.blocks.MeteorRock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;

public class Pickaxe extends ItemPickaxe {
    public Pickaxe(ToolMaterial material) {
        super(material);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        // meteor pickaxe mines obsidian and meteors twice as fast
        if (this == Registry.meteorPickaxe && (state.getBlock() == Blocks.OBSIDIAN || state.getBlock() instanceof MeteorRock))
            speed *= 2;
        return speed;
    }
}
