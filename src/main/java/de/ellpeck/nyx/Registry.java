package de.ellpeck.nyx;

import de.ellpeck.nyx.enchantments.LunarEdge;
import de.ellpeck.nyx.enchantments.LunarShield;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Nyx.ID)
public final class Registry {

    public static final Enchantment LUNAR_EDGE = new LunarEdge();
    public static final Enchantment LUNAR_SHIELD = new LunarShield();

    @SubscribeEvent
    public static void onEnchantmentRegistry(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(LUNAR_EDGE, LUNAR_SHIELD);
    }
}
