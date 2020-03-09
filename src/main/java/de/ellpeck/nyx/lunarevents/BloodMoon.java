package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class BloodMoon extends LunarEvent {
    public BloodMoon(NyxWorld nyxWorld) {
        super("blood_moon", nyxWorld);
    }

    @Override
    public ITextComponent getStartMessage() {
        return new TextComponentTranslation("info." + Nyx.ID + ".blood_moon")
                .setStyle(new Style().setColor(TextFormatting.DARK_RED).setItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (!Config.bloodMoon)
            return false;
        if (this.world.getCurrentMoonPhaseFactor() < 1)
            return false;
        if (!lastDaytime || this.world.isDaytime())
            return false;
        return this.world.rand.nextDouble() <= Config.bloodMoonChance;
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) {
        return this.world.isDaytime();
    }

    @Override
    public int getSkyColor() {
        return 0x9e2a09;
    }

    @Override
    public String getMoonTexture() {
        return "blood_moon";
    }
}
