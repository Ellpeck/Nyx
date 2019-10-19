package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Nyx;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class LunarWater extends BlockFluidClassic {
    public LunarWater(Fluid fluid) {
        super(fluid, Material.WATER);
        this.setRegistryName(new ResourceLocation(Nyx.ID, "lunar_water"));
        this.setTranslationKey(Nyx.ID + "." + this.getRegistryName().getPath());

        this.displacements.put(this, false);
    }

    @Override
    public boolean canDisplace(IBlockAccess world, BlockPos pos) {
        return !world.getBlockState(pos).getMaterial().isLiquid() && super.canDisplace(world, pos);
    }

    @Override
    public boolean displaceIfPossible(World world, BlockPos pos) {
        return !world.getBlockState(pos).getMaterial().isLiquid() && super.displaceIfPossible(world, pos);
    }
}
