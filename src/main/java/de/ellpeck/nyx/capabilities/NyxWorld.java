package de.ellpeck.nyx.capabilities;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.LunarEvent;
import de.ellpeck.nyx.lunarevents.StarShower;
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

public class NyxWorld implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    public static float moonPhase;

    public final BiMap<String, LunarEvent> lunarEvents = HashBiMap.create();
    public final World world;
    public float eventSkyModifier;
    public int currentSkyColor;
    public LunarEvent currentEvent;

    private boolean wasDaytime;

    public NyxWorld(World world) {
        this.world = world;
        this.lunarEvents.put("harvest_moon", new HarvestMoon(this));
        this.lunarEvents.put("star_shower", new StarShower(this));
    }

    public void update() {
        String dimension = this.world.provider.getDimensionType().getName();
        if (!Config.allowedDimensions.contains(dimension))
            return;

        moonPhase = this.world.getCurrentMoonPhaseFactor();
        if (this.currentEvent != null)
            this.currentEvent.update();

        if (!this.world.isRemote) {
            boolean isDirty = false;

            if (this.currentEvent == null) {
                for (LunarEvent event : this.lunarEvents.values()) {
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
            compound.setString("event", this.lunarEvents.inverse().get(this.currentEvent));
        compound.setBoolean("was_daytime", this.wasDaytime);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.currentEvent = this.lunarEvents.get(compound.getString("event"));
        if (this.currentEvent != null)
            this.currentSkyColor = this.currentEvent.getSkyColor();
        this.wasDaytime = compound.getBoolean("was_daytime");
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
