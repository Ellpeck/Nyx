package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class HarvestMoon extends LunarEvent {
    public HarvestMoon(NyxWorld nyxWorld) {
        super("harvest_moon", nyxWorld);
    }

    @Override
    public ITextComponent getStartMessage() {
        return new TextComponentTranslation("info." + Nyx.ID + ".harvest_moon")
                .setStyle(new Style().setColor(TextFormatting.BLUE).setItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (!Config.harvestMoon)
            return false;
        if (this.world.getCurrentMoonPhaseFactor() < 1)
            return false;
        // check if it just turned night time
        if (!lastDaytime || this.world.isDaytime())
            return false;
        return this.world.rand.nextDouble() <= Config.harvestMoonChance;
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) {
        return this.world.isDaytime();
    }

    @Override
    public int getSkyColor() {
        return 0x3f3fc0;
    }

    @Override
    public String getMoonTexture() {
        return "harvest_moon";
    }
}
