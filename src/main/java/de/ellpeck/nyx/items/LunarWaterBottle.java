package de.ellpeck.nyx.items;

import de.ellpeck.nyx.Nyx;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class LunarWaterBottle extends Item {

    public LunarWaterBottle() {
        this.setRegistryName(new ResourceLocation(Nyx.ID, "lunar_water_bottle"));
        this.setTranslationKey(Nyx.ID + "." + this.getRegistryName().getPath());

        this.setMaxStackSize(1);
    }

    public static boolean applyLunarWater(EntityLivingBase entity) {
        boolean did = false;
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            Potion potion = effect.getPotion();
            if (potion.isBadEffect()) {
                entity.removePotionEffect(potion);
                did = true;
            }
        }
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
        return new ItemStack(Items.GLASS_BOTTLE);
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
}
