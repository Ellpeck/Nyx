package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public abstract class LunarEvent {

    protected final NyxWorld nyxWorld;
    protected final World world;

    public LunarEvent(NyxWorld nyxWorld) {
        this.nyxWorld = nyxWorld;
        this.world = nyxWorld.world;
    }

    public abstract ITextComponent getStartMessage();

    public abstract boolean shouldStart(boolean lastDaytime);

    public abstract boolean shouldStop(boolean lastDaytime);

    public abstract int getSkyColor();

    public String getMoonTexture() {
        return null;
    }
}
