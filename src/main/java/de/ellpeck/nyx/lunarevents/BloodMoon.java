package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class BloodMoon extends LunarEvent {

    private final ConfigImpl config = new ConfigImpl(() -> Config.bloodMoon);

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
        if (this.world.getCurrentMoonPhaseFactor() < 1)
            return false;
        if (!lastDaytime || this.world.isDaytime())
            return false;
        return this.config.canStart();
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

    @Override
    public void update(boolean lastDaytime) {
        this.config.update(lastDaytime);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.config.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.config.deserializeNBT(nbt);
    }
}
