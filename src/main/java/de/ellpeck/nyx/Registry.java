package de.ellpeck.nyx;

import de.ellpeck.nyx.blocks.LunarWater;
import de.ellpeck.nyx.blocks.LunarWaterFluid;
import de.ellpeck.nyx.enchantments.LunarEdge;
import de.ellpeck.nyx.enchantments.LunarShield;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Nyx.ID)
public final class Registry {

    public static Enchantment lunarEdge;
    public static Enchantment lunarShield;

    public static Fluid lunarWaterFluid;
    public static Block lunarWater;

    @SubscribeEvent
    public static void onEnchantmentRegistry(RegistryEvent.Register<Enchantment> event) {
        if (Nyx.enchantments) {
            event.getRegistry().registerAll(
                    lunarEdge = new LunarEdge(),
                    lunarShield = new LunarShield()
            );
        }
    }

    @SubscribeEvent
    public static void onBlockRegistry(RegistryEvent.Register<Block> event) {
        if (Nyx.lunarWater) {
            Fluid fluid = new LunarWaterFluid();
            FluidRegistry.registerFluid(fluid);
            FluidRegistry.addBucketForFluid(fluid);
            lunarWaterFluid = FluidRegistry.getFluid(fluid.getName());

            event.getRegistry().registerAll(
                    lunarWater = new LunarWater(lunarWaterFluid)
            );
        }
    }
}
