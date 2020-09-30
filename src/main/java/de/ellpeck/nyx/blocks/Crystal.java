package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
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
        Registry.initBlock(this, "crystal", Item::new);
        GameRegistry.registerTileEntity(Tile.class, this.getRegistryName());
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

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ItemStack drop = new ItemStack(this, 1);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof Tile)
            drop.setItemDamage(((Tile) tile).durability);
        drops.add(drop);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof Tile)
            ((Tile) tile).durability = stack.getItemDamage();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new Tile();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    public static class Item extends ItemBlock {

        public Item(Block block) {
            super(block);
            this.setMaxDamage(Config.crystalDurability);
        }

        @Override
        public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
            if (!player.isSneaking()) {
                IBlockState state = worldIn.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof IGrowable) {
                    IGrowable growable = (IGrowable) block;
                    if (growable.canGrow(worldIn, pos, state, worldIn.isRemote)) {
                        if (!worldIn.isRemote && growable.canUseBonemeal(worldIn, worldIn.rand, pos, state)) {
                            growable.grow(worldIn, worldIn.rand, pos, state);
                            worldIn.playEvent(2005, pos, 0);
                            player.getHeldItem(hand).damageItem(1, player);
                        }
                    }
                    return EnumActionResult.SUCCESS;
                }
            }
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
    }

    public static class Tile extends TileEntity {

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
    }
}
