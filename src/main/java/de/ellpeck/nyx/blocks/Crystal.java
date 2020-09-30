package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.Random;

public class Crystal extends BlockContainer {

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(4 / 16F, 0, 4 / 16F, 12 / 16F, 12 / 16F, 12 / 16F);

    public Crystal() {
        super(Material.ROCK);
        this.setHardness(3);
        this.setLightLevel(0.9375F);
        this.setTickRandomly(true);
        Registry.initBlock(this, "crystal", ItemBlock::new);
        GameRegistry.registerTileEntity(Tile.class, this.getRegistryName());
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState s, Random random) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof Tile))
            return;
        Tile crystal = (Tile) tile;
        if (crystal.durability <= 0)
            return;
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
                        if (worldIn.getBlockState(offset) != state) {
                            crystal.durability--;
                            if (crystal.durability <= 0)
                                return;
                        }
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

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new Tile();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    public static class Tile extends TileEntity implements ITickable {

        public int durability;

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger("durability", this.durability);
            return super.writeToNBT(compound);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            this.durability = compound.getInteger("durability");
            super.readFromNBT(compound);
        }

        @Override
        public void update() {
            if (!this.world.isRemote && this.world.getTotalWorldTime() % 600 == 0) {
                NyxWorld data = NyxWorld.get(this.world);
                if (data != null && data.currentEvent instanceof HarvestMoon && this.world.canSeeSky(this.pos.up()))
                    this.durability = Math.min(Config.crystalDurability, this.durability + Config.crystalDurability / 10);
            }
        }
    }
}
