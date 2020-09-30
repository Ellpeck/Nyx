package de.ellpeck.nyx.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

// TODO the bonemeal part, 1000 uses and all that
public class Crystal extends Block {

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(4 / 16F, 0, 4 / 16F, 12 / 16F, 12 / 16F, 12 / 16F);

    public Crystal() {
        super(Material.ROCK);
        this.setHardness(3);
        this.setLightLevel(0.9375F);
        this.setTickRandomly(true);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState s, Random random) {
        int range = 5;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos offset = pos.add(x, y, z);
                    IBlockState state = worldIn.getBlockState(offset);
                    Block block = state.getBlock();
                    if (block instanceof IGrowable || block instanceof IPlantable) {
                        for (int i = 0; i < 5; i++)
                            block.randomTick(worldIn, offset, state, random);
                    }
                }
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDS;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }
}
