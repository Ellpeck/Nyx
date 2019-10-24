package de.ellpeck.nyx.capabilities;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.network.PacketHandler;
import de.ellpeck.nyx.network.PacketNyxWorld;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NyxWorld implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    public static float moonPhase;

    public final World world;
    public boolean isHarvestMoon;
    private boolean wasDaytime;
    private boolean isDirty;

    public NyxWorld(World world) {
        this.world = world;
    }

    public void update() {
        if (this.world.provider.getDimensionType() != DimensionType.OVERWORLD)
            return;

        moonPhase = this.world.getCurrentMoonPhaseFactor();

        if (this.world.isRemote)
            return;

        if (Nyx.harvestMoon) {
            if (this.shouldHarvestMoonStart()) {
                this.isHarvestMoon = true;
                this.isDirty = true;
            }
            this.wasDaytime = this.world.isDaytime();
            if (this.wasDaytime && this.isHarvestMoon) {
                this.isHarvestMoon = false;
                this.isDirty = true;
            }
        }

        if (this.isDirty) {
            this.isDirty = false;
            for (EntityPlayerMP player : this.world.getPlayers(EntityPlayerMP.class, p -> true))
                PacketHandler.sendTo(player, new PacketNyxWorld(this));
        }
    }

    private boolean shouldHarvestMoonStart() {
        if (this.world.getCurrentMoonPhaseFactor() < 1)
            return false;
        // check if it just turned night time
        if (!this.wasDaytime || this.world.isDaytime())
            return false;
        return this.world.rand.nextDouble() <= Nyx.harvestMoonChance;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("harvest_moon", this.isHarvestMoon);
        compound.setBoolean("was_daytime", this.wasDaytime);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.isHarvestMoon = compound.getBoolean("harvest_moon");
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
}
