package de.ellpeck.nyx.events;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.enchantments.NyxEnchantment;
import de.ellpeck.nyx.entities.CauldronTracker;
import de.ellpeck.nyx.entities.CauldronTrackerRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Nyx.ID, value = Side.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<String> tooltip = event.getToolTip();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!(enchantment instanceof NyxEnchantment))
                continue;
            String name = enchantment.getTranslatedName(entry.getValue());
            int addIndex = tooltip.indexOf(name) + 1;

            String info = I18n.format(enchantment.getName() + ".desc");
            List<String> split = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(info, 200);
            for (int i = split.size() - 1; i >= 0; i--)
                tooltip.add(addIndex, TextFormatting.DARK_GRAY + split.get(i));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        World world = Minecraft.getMinecraft().world;
        if (world != null && world.hasCapability(Registry.worldCapability, null))
            world.getCapability(Registry.worldCapability, null).update();
    }

    @SubscribeEvent
    public static void onFogRender(EntityViewRenderEvent.FogColors event) {
        World world = Minecraft.getMinecraft().world;
        if (!world.hasCapability(Registry.worldCapability, null))
            return;
        NyxWorld nyxWorld = world.getCapability(Registry.worldCapability, null);
        if (!nyxWorld.isHarvestMoon)
            return;
        float mod = nyxWorld.harvestMoonSkyModifier;
        event.setRed(lerp(event.getRed(), 0.25F, mod));
        event.setGreen(lerp(event.getGreen(), 0.25F, mod));
        event.setBlue(lerp(event.getBlue(), 0.75F, mod));
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (Config.lunarWater) {
            registerFluidRenderer(Registry.lunarWaterFluid);
            RenderingRegistry.registerEntityRenderingHandler(CauldronTracker.class, CauldronTrackerRenderer::new);
        }

        for (Item item : Registry.MOD_ITEMS)
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
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
