package de.ellpeck.nyx.items;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class LunarWaterBottle extends Item {

    public LunarWaterBottle() {
        Registry.initItem(this, "lunar_water_bottle");
        this.setMaxStackSize(1);
    }

    public static boolean applyLunarWater(EntityLivingBase entity) {
        boolean did = false;

        Set<Potion> effectsToRemove = new HashSet<>();
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            Potion potion = effect.getPotion();
            if (potion.isBadEffect()) {
                effectsToRemove.add(potion);
                did = true;
            }
        }
        effectsToRemove.forEach(entity::removePotionEffect);

        if (entity.getActivePotionEffect(MobEffects.REGENERATION) == null) {
            entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5 * 20, 1));
            did = true;
        }
        return did;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote)
            applyLunarWater(entityLiving);
        EntityPlayer player = entityLiving instanceof EntityPlayer ? (EntityPlayer) entityLiving : null;
        if (player == null || !player.capabilities.isCreativeMode) {
            stack.shrink(1);
            if (stack.isEmpty())
                return new ItemStack(Items.GLASS_BOTTLE);
            if (player != null)
                player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new BucketWrapper(stack);
    }

    private static class BucketWrapper extends FluidBucketWrapper {

        private static final int VOLUME = Fluid.BUCKET_VOLUME / 4;

        public BucketWrapper(ItemStack container) {
            super(container);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new FluidTankProperties[]{new FluidTankProperties(this.getFluid(), VOLUME)};
        }

        @Override
        public FluidStack getFluid() {
            if (this.container.getItem() == Registry.lunarWaterBottle)
                return new FluidStack(Registry.lunarWaterFluid, VOLUME);
            return null;
        }

        @Override
        protected void setFluid(@Nullable Fluid fluid) {
            this.setFluid(new FluidStack(fluid, VOLUME));
        }

        @Override
        protected void setFluid(@Nullable FluidStack fluidStack) {
            if (fluidStack == null) {
                this.container = new ItemStack(Items.GLASS_BOTTLE);
            } else {
                this.container = new ItemStack(Registry.lunarWaterBottle);
            }
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (this.container.getCount() != 1 || resource == null || resource.amount < VOLUME || this.getFluid() != null || !this.canFillFluidType(resource)) {
                return 0;
            }

            if (doFill) {
                this.setFluid(resource);
            }

            return Fluid.BUCKET_VOLUME;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (this.container.getCount() != 1 || resource == null || resource.amount < VOLUME) {
                return null;
            }

            FluidStack fluidStack = this.getFluid();
            if (fluidStack != null && fluidStack.isFluidEqual(resource)) {
                if (doDrain) {
                    this.setFluid((FluidStack) null);
                }
                return fluidStack;
            }

            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (this.container.getCount() != 1 || maxDrain < VOLUME) {
                return null;
            }

            FluidStack fluidStack = this.getFluid();
            if (fluidStack != null) {
                if (doDrain) {
                    this.setFluid((FluidStack) null);
                }
                return fluidStack;
            }

            return null;
        }
    }
}
