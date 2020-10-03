package de.ellpeck.nyx;

import de.ellpeck.nyx.blocks.*;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.enchantments.LunarEdge;
import de.ellpeck.nyx.enchantments.LunarShield;
import de.ellpeck.nyx.entities.CauldronTracker;
import de.ellpeck.nyx.entities.FallingMeteor;
import de.ellpeck.nyx.entities.FallingStar;
import de.ellpeck.nyx.items.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Nyx.ID)
public final class Registry {

    public static final Set<Item> MOD_ITEMS = new HashSet<>();

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(Nyx.ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registry.fallenStar);
        }
    };

    @CapabilityInject(NyxWorld.class)
    public static Capability<NyxWorld> worldCapability;

    public static Enchantment lunarEdge;
    public static Enchantment lunarShield;

    public static Block lunarWater;
    public static Block lunarWaterCauldron;
    public static Block starAir;
    public static Block starBlock;
    public static Block crackedStarBlock;
    public static Block chiseledStarBlock;
    public static Block starStairs;
    public static Block starSlab;
    public static Block meteorRock;
    public static Block gleaningMeteorRock;
    public static Block crystal;

    public static Fluid lunarWaterFluid;

    public static Item lunarWaterBottle;
    public static Item cometShard;
    public static Item fallenStar;
    public static Item meteorDust;
    public static Item meteorFinder;
    public static Item unrefinedCrystal;
    public static Item scythe;
    public static Item meteorIngot;

    public static SoundEvent lunarWaterSound;
    public static SoundEvent fallingStarSound;
    public static SoundEvent fallingStarImpactSound;
    public static SoundEvent fallingMeteorSound;
    public static SoundEvent fallingMeteorImpactSound;

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
        IForgeRegistry<Block> reg = event.getRegistry();
        if (Config.lunarWater) {
            Fluid fluid = new LunarWaterFluid();
            FluidRegistry.registerFluid(fluid);
            FluidRegistry.addBucketForFluid(fluid);
            lunarWaterFluid = FluidRegistry.getFluid(fluid.getName());

            reg.registerAll(
                    lunarWater = new LunarWater(lunarWaterFluid),
                    lunarWaterCauldron = new LunarWaterCauldron()
            );
        }
        if (Config.fallingStars) {
            reg.registerAll(
                    starAir = new StarAir(),
                    starBlock = initBlock(new Block(Material.ROCK).setHardness(2), "star_block", ItemBlock::new),
                    crackedStarBlock = initBlock(new Block(Material.ROCK).setHardness(2), "cracked_star_block", ItemBlock::new),
                    chiseledStarBlock = initBlock(new Block(Material.ROCK).setHardness(2), "chiseled_star_block", ItemBlock::new),
                    starStairs = initBlock(new NyxStairs(starBlock.getDefaultState()), "star_stairs", ItemBlock::new)
            );
            NyxSlab[] slabs = NyxSlab.makeSlab("star_slab", Material.ROCK, SoundType.STONE, 2);
            reg.registerAll(slabs);
            starSlab = slabs[0];
        }
        if (Config.meteors) {
            reg.registerAll(
                    meteorRock = initBlock(new MeteorRock(() -> cometShard), "meteor_rock", ItemBlock::new),
                    gleaningMeteorRock = initBlock(new MeteorRock(() -> unrefinedCrystal), "gleaning_meteor_rock", ItemBlock::new),
                    crystal = new Crystal()
            );
        }
    }

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        if (Config.lunarWater)
            lunarWaterBottle = new LunarWaterBottle();
        cometShard = initItem(new Item(), "comet_shard");
        if (Config.meteors) {
            meteorDust = initItem(new Item(), "meteor_dust");
            meteorFinder = initItem(new MeteorFinder(), "meteor_finder");
            unrefinedCrystal = initItem(new Item(), "unrefined_crystal");
            scythe = initItem(new Scythe(), "scythe");
            meteorIngot = initItem(new Item(), "meteor_ingot");
        }
        if (Config.fallingStars)
            fallenStar = initItem(new FallenStar(), "fallen_star");
        MOD_ITEMS.forEach(event.getRegistry()::register);
    }

    @SubscribeEvent
    public static void onSoundRegistry(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                lunarWaterSound = new SoundEvent(new ResourceLocation(Nyx.ID, "lunar_water")).setRegistryName("lunar_water"),
                fallingStarSound = new SoundEvent(new ResourceLocation(Nyx.ID, "falling_star")).setRegistryName("falling_star"),
                fallingStarImpactSound = new SoundEvent(new ResourceLocation(Nyx.ID, "falling_star_impact")).setRegistryName("falling_star_impact"),
                fallingMeteorSound = new SoundEvent(new ResourceLocation(Nyx.ID, "falling_meteor")).setRegistryName("falling_meteor"),
                fallingMeteorImpactSound = new SoundEvent(new ResourceLocation(Nyx.ID, "falling_meteor_impact")).setRegistryName("falling_meteor_impact")
        );
    }

    public static void preInit() {
        if (Config.lunarWater)
            EntityRegistry.registerModEntity(new ResourceLocation(Nyx.ID, "cauldron_tracker"), CauldronTracker.class, Nyx.ID + ".cauldron_tracker", 0, Nyx.instance, 64, 20, false);
        if (Config.fallingStars)
            EntityRegistry.registerModEntity(new ResourceLocation(Nyx.ID, "falling_star"), FallingStar.class, Nyx.ID + ".falling_star", 1, Nyx.instance, 128, 1, true);
        if (Config.meteors)
            EntityRegistry.registerModEntity(new ResourceLocation(Nyx.ID, "falling_meteor"), FallingMeteor.class, Nyx.ID + ".falling_meteor", 2, Nyx.instance, 256, 1, true);

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

    public static void init() {
        if (Config.fallingStars)
            GameRegistry.addSmelting(new ItemStack(starBlock), new ItemStack(crackedStarBlock), 0.1F);
        if (Config.meteors) {
            GameRegistry.addSmelting(new ItemStack(unrefinedCrystal), new ItemStack(crystal), 0.25F);
            GameRegistry.addSmelting(new ItemStack(cometShard), new ItemStack(meteorIngot), 0.15F);
        }
    }

    public static Item initItem(Item item, String name) {
        item.setRegistryName(new ResourceLocation(Nyx.ID, name));
        item.setTranslationKey(Nyx.ID + "." + item.getRegistryName().getPath());
        item.setCreativeTab(CREATIVE_TAB);
        MOD_ITEMS.add(item);
        return item;
    }

    public static Block initBlock(Block block, String name, Function<Block, ItemBlock> item) {
        block.setRegistryName(new ResourceLocation(Nyx.ID, name));
        block.setTranslationKey(Nyx.ID + "." + block.getRegistryName().getPath());
        block.setCreativeTab(CREATIVE_TAB);
        if (item != null)
            initItem(item.apply(block), name);
        return block;
    }
}
