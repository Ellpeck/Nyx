package de.ellpeck.nyx;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@EventBusSubscriber(modid = Nyx.ID)
public final class Events {

    public static float moonPhase;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        moonPhase = event.world.getCurrentMoonPhaseFactor();
    }

    @SubscribeEvent
    public static void onExpDrop(LivingExperienceDropEvent event) {
        EntityPlayer player = event.getAttackingPlayer();
        if (player == null)
            return;
        ItemStack held = player.getHeldItemMainhand();
        int level = EnchantmentHelper.getEnchantmentLevel(Registry.LUNAR_EDGE, held);
        if (level <= 0)
            return;
        float exp = event.getDroppedExperience();
        float mod = 2 * (level / (float) Registry.LUNAR_EDGE.getMaxLevel());
        event.setDroppedExperience(MathHelper.floor(exp * mod));
    }
}
