package de.ellpeck.nyx.events;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.enchantments.NyxEnchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

@Mod.EventBusSubscriber(modid = Nyx.ID, value = Side.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<String> tooltip = event.getToolTip();
        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (!(enchantment instanceof NyxEnchantment))
                continue;
            String info = I18n.format(enchantment.getName() + ".desc");
            for (String split : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(info, 200))
                tooltip.add(TextFormatting.DARK_GRAY + split);
        }
    }
}
