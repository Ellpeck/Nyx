package de.ellpeck.nyx.capabilities;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.lunarevents.*;
import de.ellpeck.nyx.network.PacketHandler;
import de.ellpeck.nyx.network.PacketNyxWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NyxWorld implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    public static float moonPhase;

    public final World world;
    public float eventSkyModifier;
    public int currentSkyColor;
    public LunarEvent currentEvent;

    private final List<LunarEvent> lunarEvents = new ArrayList<>();
    private boolean wasDaytime;

    public NyxWorld(World world) {
        this.world = world;
        this.lunarEvents.add(new HarvestMoon(this));
        this.lunarEvents.add(new StarShower(this));
        this.lunarEvents.add(new BloodMoon(this));
        // this needs to stay at the end to prioritize random events
        this.lunarEvents.add(new FullMoon(this));
    }

    public void update() {
        String dimension = this.world.provider.getDimensionType().getName();
        if (!Config.allowedDimensions.contains(dimension))
            return;

        moonPhase = this.world.getCurrentMoonPhaseFactor();

        for (LunarEvent event : this.lunarEvents)
            event.update(this.wasDaytime);

        if (!this.world.isRemote) {
            boolean isDirty = false;

            if (this.currentEvent == null) {
                for (LunarEvent event : this.lunarEvents) {
                    if (!event.shouldStart(this.wasDaytime))
                        continue;

                    this.currentEvent = event;
                    isDirty = true;

                    ITextComponent text = event.getStartMessage();
                    for (EntityPlayer player : this.world.playerEntities)
                        player.sendMessage(text);

                    break;
                }
            }

            if (this.currentEvent != null && this.currentEvent.shouldStop(this.wasDaytime)) {
                this.currentEvent = null;
                isDirty = true;
            }

            if (isDirty) {
                for (EntityPlayer player : this.world.playerEntities)
                    PacketHandler.sendTo(player, new PacketNyxWorld(this));
            }

            this.wasDaytime = this.world.isDaytime();
        } else {
            if (this.currentEvent != null && this.currentSkyColor != 0) {
                if (this.eventSkyModifier < 1)
                    this.eventSkyModifier += 0.01F;
            } else {
                if (this.eventSkyModifier > 0)
                    this.eventSkyModifier -= 0.01F;
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        if (this.currentEvent != null)
            compound.setString("event", this.currentEvent.name);
        compound.setBoolean("was_daytime", this.wasDaytime);
        for (LunarEvent event : this.lunarEvents)
            compound.setTag(event.name, event.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        String name = compound.getString("event");
        this.currentEvent = this.lunarEvents.stream().filter(e -> e.name.equals(name)).findFirst().orElse(null);
        if (this.currentEvent != null)
            this.currentSkyColor = this.currentEvent.getSkyColor();
        this.wasDaytime = compound.getBoolean("was_daytime");
        for (LunarEvent event : this.lunarEvents)
            event.deserializeNBT(compound.getCompoundTag(event.name));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == Registry.worldCapability;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Registry.worldCapability ? (T) this : null;
    }

    public static NyxWorld get(World world) {
        if (world.hasCapability(Registry.worldCapability, null))
            return world.getCapability(Registry.worldCapability, null);
        return null;
    }
}
