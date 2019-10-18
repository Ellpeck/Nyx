package de.ellpeck.nyx.enchantments;

import de.ellpeck.nyx.events.Events;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;

import static net.minecraft.inventory.EntityEquipmentSlot.MAINHAND;
import static net.minecraft.inventory.EntityEquipmentSlot.OFFHAND;

public class LunarEdge extends NyxEnchantment {
    public LunarEdge() {
        super("lunar_edge", Rarity.UNCOMMON, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{MAINHAND, OFFHAND});

    }

    @Override
    public float calcDamageByCreature(int level, EnumCreatureAttribute creatureType) {
        float baseDamage = 1.25F + (float) Math.max(0, level - 1) * 0.5F;
        return Events.moonPhase * baseDamage;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}
