package de.ellpeck.nyx.enchantments;

import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

import static net.minecraft.inventory.EntityEquipmentSlot.MAINHAND;
import static net.minecraft.inventory.EntityEquipmentSlot.OFFHAND;

public class LunarEdge extends NyxEnchantment {
    public LunarEdge() {
        super("lunar_edge", Rarity.UNCOMMON, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{MAINHAND, OFFHAND});

    }

    @Override
    public float calcDamageByCreature(int level, EnumCreatureAttribute creatureType) {
        float baseDamage = 1.25F + (float) Math.max(0, level - 1) * 0.5F;
        return NyxWorld.moonPhase * baseDamage;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected boolean canApplyTogether(Enchantment ench) {
        return super.canApplyTogether(ench) && !(ench instanceof EnchantmentDamage);
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return stack.getItem() instanceof ItemAxe || super.canApply(stack);
    }
}
