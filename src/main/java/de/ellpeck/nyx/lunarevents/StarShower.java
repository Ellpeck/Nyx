package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class StarShower extends LunarEvent {
    public StarShower(NyxWorld nyxWorld) {
        super(nyxWorld);
    }

    @Override
    public ITextComponent getStartMessage() {
        return new TextComponentTranslation("info." + Nyx.ID + ".star_shower")
                .setStyle(new Style().setColor(TextFormatting.GOLD).setItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (!Config.starShowers)
            return false;
        if (!lastDaytime || this.world.isDaytime())
            return false;
        return this.world.rand.nextDouble() <= Config.starShowerRarity;
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) {
        return this.world.isDaytime();
    }

    @Override
    public int getSkyColor() {
        return 0xdec25f;
    }
}
