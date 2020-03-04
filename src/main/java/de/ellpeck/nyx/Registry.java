package de.ellpeck.nyx;

import de.ellpeck.nyx.blocks.LunarWater;
import de.ellpeck.nyx.blocks.LunarWaterCauldron;
import de.ellpeck.nyx.blocks.LunarWaterFluid;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.enchantments.LunarEdge;
import de.ellpeck.nyx.enchantments.LunarShield;
import de.ellpeck.nyx.entities.CauldronTracker;
import de.ellpeck.nyx.items.LunarWaterBottle;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import javax.annotation.Nullable;

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

    public static SoundEvent lunarWaterSound;

    @SubscribeEvent
    public static void onEnchantmentRegistry(RegistryEvent.Register<Enchantment> event) {
        if (Config.enchantments) {
            event.getRegistry().registerAll(
                    lunarEdge = new LunarEdge(),
                    lunarShield = new LunarShield()
            );
        }
    }

    @SubscribeEvent
    public static void onBlockRegistry(RegistryEvent.Register<Block> event) {
        if (Config.lunarWater) {
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
        if (Config.lunarWater)
            event.getRegistry().register(lunarWaterBottle = new LunarWaterBottle());
    }

    @SubscribeEvent
    public static void onSoundRegistry(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                lunarWaterSound = new SoundEvent(new ResourceLocation(Nyx.ID, "lunar_water")).setRegistryName("lunar_water")
        );
    }

    public static void init() {
        if (Config.lunarWater)
            EntityRegistry.registerModEntity(new ResourceLocation(Nyx.ID, "cauldron_tracker"), CauldronTracker.class, Nyx.ID + ".cauldron_tracker", 0, Nyx.instance, 64, 20, false);

        CapabilityManager.INSTANCE.register(NyxWorld.class, new Capability.IStorage<NyxWorld>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability capability, NyxWorld instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability capability, NyxWorld instance, EnumFacing side, NBTBase nbt) {

            }
        }, () -> null);

    }

    public static void initItem(Item item, String name) {
        item.setRegistryName(new ResourceLocation(Nyx.ID, name));
        item.setTranslationKey(Nyx.ID + "." + item.getRegistryName().getPath());
        item.setCreativeTab(CreativeTabs.MISC);
    }
}
