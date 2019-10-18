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
}
