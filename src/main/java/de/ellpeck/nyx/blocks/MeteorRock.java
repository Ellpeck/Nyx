package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import java.util.Random;

public class MeteorRock extends Block {
    public MeteorRock() {
        super(Material.ROCK);
        this.setHarvestLevel("pickaxe", 3);
        this.setHardness(40);
        this.setResistance(3000);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Registry.cometShard;
    }
}
