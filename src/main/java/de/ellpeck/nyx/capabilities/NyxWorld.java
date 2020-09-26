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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.mutable.MutableInt;
import scala.Int;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class NyxWorld implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    public static float moonPhase;

    public final World world;
    public final List<LunarEvent> lunarEvents = new ArrayList<>();
    public final Set<BlockPos> cachedMeteorPositions = new HashSet<>();
    public final Map<ChunkPos, MutableInt> playersPresentTicks = new HashMap<>();
    public final Set<BlockPos> meteorLandingSites = new HashSet<>();
    public float eventSkyModifier;
    public int currentSkyColor;
    public LunarEvent currentEvent;
    public LunarEvent forcedEvent;

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
        // calculate which chunks have players close to them for meteor spawning
        if (!this.world.isRemote) {
            int interval = 100;
            if (Config.meteors && this.world.getTotalWorldTime() % interval == 0) {
                Set<ChunkPos> remaining = new HashSet<>(this.playersPresentTicks.keySet());
                for (EntityPlayer player : this.world.playerEntities) {
                    for (int x = -Config.meteorDisallowRadius; x <= Config.meteorDisallowRadius; x++) {
                        for (int z = -Config.meteorDisallowRadius; z <= Config.meteorDisallowRadius; z++) {
                            ChunkPos pos = new ChunkPos(MathHelper.floor(player.posX / 16) + x, MathHelper.floor(player.posZ / 16) + z);
                            MutableInt time = this.playersPresentTicks.computeIfAbsent(pos, p -> new MutableInt());
                            time.add(interval);
                            remaining.remove(pos);
                        }
                    }
                }
                // all positions that weren't removed are player-free, so reduce them
                if (remaining.size() > 0) {
                    for (ChunkPos pos : remaining) {
                        MutableInt time = this.playersPresentTicks.get(pos);
                        time.subtract(interval);
                        if (time.intValue() <= 0)
                            this.playersPresentTicks.remove(pos);
                    }
                }
            }
        }

        // moon event stuff
        String dimension = this.world.provider.getDimensionType().getName();
        if (Config.allowedDimensions.contains(dimension)) {
            moonPhase = this.world.getCurrentMoonPhaseFactor();

            for (LunarEvent event : this.lunarEvents)
                event.update(this.wasDaytime);

            if (!this.world.isRemote) {
                boolean isDirty = false;

                if (this.currentEvent == null) {
                    if (this.forcedEvent != null && this.forcedEvent.shouldStart(this.wasDaytime)) {
                        this.currentEvent = this.forcedEvent;
                        this.forcedEvent = null;
                    } else {
                        for (LunarEvent event : this.lunarEvents) {
                            if (event.shouldStart(this.wasDaytime)) {
                                this.currentEvent = event;
                                break;
                            }
                        }
                    }
                    if (this.currentEvent != null) {
                        isDirty = true;
                        ITextComponent text = this.currentEvent.getStartMessage();
                        for (EntityPlayer player : this.world.playerEntities)
                            player.sendMessage(text);
                    }
                }

                if (this.currentEvent != null && this.currentEvent.shouldStop(this.wasDaytime)) {
                    this.currentEvent = null;
                    isDirty = true;
                }

                if (isDirty)
                    this.sendToClients();

                this.wasDaytime = isDaytime(this.world);
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
    }

    public void sendToClients() {
        for (EntityPlayer player : this.world.playerEntities)
            PacketHandler.sendTo(player, new PacketNyxWorld(this));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.serializeNBT(false);
    }

    public NBTTagCompound serializeNBT(boolean client) {
        NBTTagCompound compound = new NBTTagCompound();
        if (this.currentEvent != null)
            compound.setString("event", this.currentEvent.name);
        compound.setBoolean("was_daytime", this.wasDaytime);
        for (LunarEvent event : this.lunarEvents)
            compound.setTag(event.name, event.serializeNBT());
        NBTTagList landings = new NBTTagList();
        for (BlockPos pos : this.meteorLandingSites)
            landings.appendTag(new NBTTagLong(pos.toLong()));
        compound.setTag("meteor_landings", landings);
        NBTTagList meteors = new NBTTagList();
        for (BlockPos pos : this.cachedMeteorPositions)
            meteors.appendTag(new NBTTagLong(pos.toLong()));
        compound.setTag("cached_meteors", meteors);
        if (!client) {
            NBTTagList ticks = new NBTTagList();
            for (Map.Entry<ChunkPos, MutableInt> e : this.playersPresentTicks.entrySet()) {
                NBTTagCompound comp = new NBTTagCompound();
                comp.setInteger("x", e.getKey().x);
                comp.setInteger("z", e.getKey().z);
                comp.setInteger("ticks", e.getValue().intValue());
                ticks.appendTag(comp);
            }
            compound.setTag("players_present_ticks", ticks);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.deserializeNBT(compound, false);
    }

    public void deserializeNBT(NBTTagCompound compound, boolean client) {
        String name = compound.getString("event");
        this.currentEvent = this.lunarEvents.stream().filter(e -> e.name.equals(name)).findFirst().orElse(null);
        if (this.currentEvent != null)
            this.currentSkyColor = this.currentEvent.getSkyColor();
        this.wasDaytime = compound.getBoolean("was_daytime");
        for (LunarEvent event : this.lunarEvents)
            event.deserializeNBT(compound.getCompoundTag(event.name));
        this.meteorLandingSites.clear();
        NBTTagList landings = compound.getTagList("meteor_landings", Constants.NBT.TAG_LONG);
        for (int i = 0; i < landings.tagCount(); i++)
            this.meteorLandingSites.add(BlockPos.fromLong(((NBTTagLong) landings.get(i)).getLong()));
        this.cachedMeteorPositions.clear();
        NBTTagList meteors = compound.getTagList("cached_meteors", Constants.NBT.TAG_LONG);
        for (int i = 0; i < meteors.tagCount(); i++)
            this.cachedMeteorPositions.add(BlockPos.fromLong(((NBTTagLong) meteors.get(i)).getLong()));
        if (!client) {
            this.playersPresentTicks.clear();
            NBTTagList ticks = compound.getTagList("players_present_ticks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < ticks.tagCount(); i++) {
                NBTTagCompound comp = ticks.getCompoundTagAt(i);
                this.playersPresentTicks.put(new ChunkPos(comp.getInteger("x"), comp.getInteger("z")), new MutableInt(comp.getInteger("ticks")));
            }
        }
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

    public static boolean isDaytime(World world) {
        // https://minecraft.gamepedia.com/Bed
        // at night (between 12541 and 23458 ticks, when stars appear in the sky)
        long time = world.getWorldTime() % 24000;
        return !(time >= 12541 && time < 23458);
    }

    public static NyxWorld get(World world) {
        if (world.hasCapability(Registry.worldCapability, null))
            return world.getCapability(Registry.worldCapability, null);
        return null;
    }
}
