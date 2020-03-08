package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public abstract class LunarEvent {

    public final String name;
    protected final NyxWorld nyxWorld;
    protected final World world;

    public LunarEvent(String name, NyxWorld nyxWorld) {
        this.name = name;
        this.nyxWorld = nyxWorld;
        this.world = nyxWorld.world;
    }

    public abstract ITextComponent getStartMessage();

    public abstract boolean shouldStart(boolean lastDaytime);

    public abstract boolean shouldStop(boolean lastDaytime);

    public int getSkyColor() {
        return 0;
    }

    public String getMoonTexture() {
        return null;
    }
}
