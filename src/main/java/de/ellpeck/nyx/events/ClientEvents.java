package de.ellpeck.nyx.events;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.enchantments.NyxEnchantment;
import de.ellpeck.nyx.entities.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Nyx.ID, value = Side.CLIENT)
public final class ClientEvents {

    private static String lastMoonTextures;

    @SubscribeEvent
    public static void onDebug(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo)
            return;
        event.getLeft().add("");
        NyxWorld world = NyxWorld.get(mc.world);
        String pre = TextFormatting.GREEN + "[" + Nyx.NAME + "]" + TextFormatting.RESET;
        String name = world.currentEvent == null ? "None" : world.currentEvent.name;
        event.getLeft().add(pre + " CurrEvent: " + name);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        World world = Minecraft.getMinecraft().world;
        if (world == null)
            return;
        NyxWorld nyx = NyxWorld.get(world);
        if (nyx == null)
            return;
        nyx.update();

        String moonTex = nyx.currentEvent != null ? nyx.currentEvent.getMoonTexture() : null;
        if (!Objects.equals(moonTex, lastMoonTextures)) {
            lastMoonTextures = moonTex;

            ResourceLocation res = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, null, "field_110927_h");
            ObfuscationReflectionHelper.setPrivateValue(ResourceLocation.class, res,
                    moonTex == null ? "minecraft" : Nyx.ID,
                    "field_110626_a");
            ObfuscationReflectionHelper.setPrivateValue(ResourceLocation.class, res,
                    moonTex == null ? "textures/environment/moon_phases.png" : "textures/moon/" + moonTex + ".png",
                    "field_110625_b");
        }
    }

    @SubscribeEvent
    public static void onFogRender(EntityViewRenderEvent.FogColors event) {
        if (!Config.moonEventTint)
            return;
        NyxWorld world = NyxWorld.get(Minecraft.getMinecraft().world);
        if (world == null || world.currentSkyColor == 0)
            return;
        event.setRed(lerp(event.getRed(), (world.currentSkyColor >> 16 & 255) / 255F, world.eventSkyModifier));
        event.setGreen(lerp(event.getGreen(), (world.currentSkyColor >> 8 & 255) / 255F, world.eventSkyModifier));
        event.setBlue(lerp(event.getBlue(), (world.currentSkyColor & 255) / 255F, world.eventSkyModifier));
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (Config.lunarWater) {
            registerFluidRenderer(Registry.lunarWaterFluid);
            RenderingRegistry.registerEntityRenderingHandler(CauldronTracker.class, EmptyRenderer::new);
        }
        if (Config.fallingStars)
            RenderingRegistry.registerEntityRenderingHandler(FallingStar.class, EmptyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FallingMeteor.class, MeteorRenderer::new);

        for (Item item : Registry.MOD_ITEMS)
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void onColorRegistry(ColorHandlerEvent.Item event) {
        if (Config.meteors)
            event.getItemColors().registerItemColorHandler((stack, tintIndex) -> 0xd44a13, Registry.meteorFinder);
    }

    // Just stole this fluid stuff from Actually Additions lol
    private static void registerFluidRenderer(Fluid fluid) {
        Block block = fluid.getBlock();
        Item item = Item.getItemFromBlock(block);
        FluidStateMapper mapper = new FluidStateMapper(fluid);
        ModelBakery.registerItemVariants(item);
        ModelLoader.setCustomMeshDefinition(item, mapper);
        ModelLoader.setCustomStateMapper(block, mapper);
    }

    private static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    private static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {

        private final ModelResourceLocation location;

        public FluidStateMapper(Fluid fluid) {
            this.location = new ModelResourceLocation(new ResourceLocation(Nyx.ID, "fluids"), fluid.getName());
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return this.location;
        }

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack) {
            return this.location;
        }
    }
}
