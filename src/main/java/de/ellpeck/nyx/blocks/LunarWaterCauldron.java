package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.items.LunarWaterBottle;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class LunarWaterCauldron extends BlockCauldron {
    public LunarWaterCauldron() {
        this.setRegistryName(new ResourceLocation(Nyx.ID, "lunar_water_cauldron"));
        this.setTranslationKey(Nyx.ID + "." + this.getRegistryName().getPath());
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (stack.isEmpty())
            return false;
        Item item = stack.getItem();
        int level = state.getValue(LEVEL);

        if (item == Items.BUCKET) {
            if (level == 3 && !worldIn.isRemote) {
                if (!playerIn.capabilities.isCreativeMode) {
                    stack.shrink(1);

                    ItemStack bucket = FluidUtil.getFilledBucket(new FluidStack(Registry.lunarWaterFluid, 1000));
                    if (stack.isEmpty()) {
                        playerIn.setHeldItem(hand, bucket);
                    } else if (!playerIn.inventory.addItemStackToInventory(bucket)) {
                        playerIn.dropItem(bucket, false);
                    }
                }

                worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                this.setWaterLevel(worldIn, pos, state, 0);
            }
            return true;
        } else if (item == Items.GLASS_BOTTLE) {
            if (level > 0 && !worldIn.isRemote) {
                if (!playerIn.capabilities.isCreativeMode) {
                    playerIn.addStat(StatList.CAULDRON_USED);
                    stack.shrink(1);

                    ItemStack bottle = new ItemStack(Registry.lunarWaterBottle);
                    if (stack.isEmpty()) {
                        playerIn.setHeldItem(hand, bottle);
                    } else if (!playerIn.inventory.addItemStackToInventory(bottle)) {
                        playerIn.dropItem(bottle, false);
                    }
                }

                worldIn.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                this.setWaterLevel(worldIn, pos, state, level - 1);
            }

            return true;
        }
        return false;
    }

    @Override
    public void setWaterLevel(World worldIn, BlockPos pos, IBlockState state, int level) {
        if (level <= 0) {
            worldIn.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
        } else {
            super.setWaterLevel(worldIn, pos, state, level);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        int i = state.getValue(LEVEL);
        float f = (float) pos.getY() + (6.0F + (float) (3 * i)) / 16.0F;

        if (!worldIn.isRemote && i > 0 && entityIn.getEntityBoundingBox().minY <= (double) f) {
            boolean did = false;
            if (entityIn.isBurning()) {
                entityIn.extinguish();
                did = true;
            }
            if (entityIn instanceof EntityLivingBase) {
                if (LunarWaterBottle.applyLunarWater((EntityLivingBase) entityIn))
                    did = true;
            }
            if (did)
                this.setWaterLevel(worldIn, pos, state, i - 1);
        }
    }

    @Override
    public void fillWithRain(World worldIn, BlockPos pos) {

    }
}
