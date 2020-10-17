package de.ellpeck.nyx.blocks;

import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;

import java.util.Random;

public class Glass extends BlockGlass {
    public Glass() {
        super(Material.GLASS, false);
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }
}
