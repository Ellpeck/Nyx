package de.ellpeck.nyx;

import de.ellpeck.nyx.blocks.LunarWater;
import de.ellpeck.nyx.blocks.LunarWaterCauldron;
import de.ellpeck.nyx.blocks.LunarWaterFluid;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.enchantments.LunarEdge;
import de.ellpeck.nyx.enchantments.LunarShield;
import de.ellpeck.nyx.items.LunarWaterBottle;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Nyx.ID)
public final class Registry {

    @CapabilityInject(NyxWorld.class)
    public static Capability<NyxWorld> worldCapability;

    public static Enchantment lunarEdge;
    public static Enchantment lunarShield;

    public static Block lunarWater;
    public static Block lunarWaterCauldron;

    public static Fluid lunarWaterFluid;

    public static Item lunarWaterBottle;

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
                    lunarWater = new LunarWater(lunarWaterFluid),
                    lunarWaterCauldron = new LunarWaterCauldron()
            );
        }
    }

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        if (Nyx.lunarWater)
            event.getRegistry().register(lunarWaterBottle = new LunarWaterBottle());
    }
}
