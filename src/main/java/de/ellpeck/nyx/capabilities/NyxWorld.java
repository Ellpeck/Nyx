package de.ellpeck.nyx.capabilities;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.network.PacketHandler;
import de.ellpeck.nyx.network.PacketNyxWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
    public float harvestMoonSkyModifier;
   
    private boolean wasDaytime;

    public NyxWorld(World world) {
        this.world = world;
    }

    public void update() {
        if (this.world.provider.getDimensionType() != DimensionType.OVERWORLD)
            return;

        moonPhase = this.world.getCurrentMoonPhaseFactor();

        if (!this.world.isRemote) {
            boolean isDirty = false;

            if (Config.harvestMoon) {
                if (this.shouldHarvestMoonStart()) {
                    this.isHarvestMoon = true;
                    isDirty = true;

                    ITextComponent text = new TextComponentTranslation("info." + Nyx.ID + ".harvest_moon")
                            .setStyle(new Style().setColor(TextFormatting.BLUE).setItalic(true));
                    for (EntityPlayer player : this.world.playerEntities)
                        player.sendMessage(text);
                }
                this.wasDaytime = this.world.isDaytime();
                if (this.wasDaytime && this.isHarvestMoon) {
                    this.isHarvestMoon = false;
                    isDirty = true;
                }
            }

            if (isDirty) {
                for (EntityPlayer player : this.world.playerEntities)
                    PacketHandler.sendTo(player, new PacketNyxWorld(this));
            }
        } else {
            if (this.isHarvestMoon) {
                if (this.harvestMoonSkyModifier < 1)
                    this.harvestMoonSkyModifier += 0.01F;
            } else {
                if (this.harvestMoonSkyModifier > 0)
                    this.harvestMoonSkyModifier -= 0.01F;
            }
        }
    }

    private boolean shouldHarvestMoonStart() {
        if (this.world.getCurrentMoonPhaseFactor() < 1)
            return false;
        // check if it just turned night time
        if (!this.wasDaytime || this.world.isDaytime())
            return false;
        return this.world.rand.nextDouble() <= Config.harvestMoonChance;
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
