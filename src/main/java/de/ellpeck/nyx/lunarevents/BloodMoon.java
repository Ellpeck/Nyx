package de.ellpeck.nyx.lunarevents;

import com.google.common.collect.Sets;
import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.EntityEntry;

import java.util.Set;

public class BloodMoon extends LunarEvent {

    private static final int MOB_COUNT_DIV = (int) Math.pow(17.0D, 2.0D);
    private final Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();

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
        if (Config.bloodMoonOnFull && this.world.getCurrentMoonPhaseFactor() < 1)
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
        return 0x420d03;
    }

    @Override
    public String getMoonTexture() {
        return "blood_moon";
    }

    @Override
    public void update(boolean lastDaytime) {
        this.config.update(lastDaytime);

        if (this.nyxWorld.currentEvent == this && !this.world.isRemote && Config.bloodMoonSpawnMultiplier > 1) {
            WorldServer world = (WorldServer) this.world;
            this.findChunksForSpawning(world, true, false, world.getTotalWorldTime() % 400L == 0L);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.config.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.config.deserializeNBT(nbt);
    }

    // Copied from WorldEntitySpawner with minor commented adjustments
    // Let's hope this doesn't change in 1.14 haha
    @SuppressWarnings("all")
    private int findChunksForSpawning(WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        } else {
            this.eligibleChunksForSpawning.clear();
            int i = 0;

            for (EntityPlayer entityplayer : worldServerIn.playerEntities) {
                if (!entityplayer.isSpectator()) {
                    int j = MathHelper.floor(entityplayer.posX / 16.0D);
                    int k = MathHelper.floor(entityplayer.posZ / 16.0D);

                    for (int i1 = -8; i1 <= 8; ++i1) {
                        for (int j1 = -8; j1 <= 8; ++j1) {
                            boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                            ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

                            if (!this.eligibleChunksForSpawning.contains(chunkpos)) {
                                ++i;

                                if (!flag && worldServerIn.getWorldBorder().contains(chunkpos)) {
                                    PlayerChunkMapEntry playerchunkmapentry = worldServerIn.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);

                                    if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers()) {
                                        this.eligibleChunksForSpawning.add(chunkpos);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int j4 = 0;
            BlockPos blockpos1 = worldServerIn.getSpawnPoint();

            for (EnumCreatureType enumcreaturetype : EnumCreatureType.values()) {
                if ((!enumcreaturetype.getPeacefulCreature() || spawnPeacefulMobs) && (enumcreaturetype.getPeacefulCreature() || spawnHostileMobs) && (!enumcreaturetype.getAnimal() || spawnOnSetTickRate)) {
                    int k4 = worldServerIn.countEntities(enumcreaturetype, true);
                    // edit: increase spawn cap
                    int l4 = enumcreaturetype.getMaxNumberOfCreature() * Config.bloodMoonSpawnMultiplier * i / MOB_COUNT_DIV;

                    if (k4 <= l4) {
                        java.util.ArrayList<ChunkPos> shuffled = com.google.common.collect.Lists.newArrayList(this.eligibleChunksForSpawning);
                        java.util.Collections.shuffle(shuffled);
                        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                        label134:

                        for (ChunkPos chunkpos1 : shuffled) {
                            BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunkpos1.x, chunkpos1.z);
                            int k1 = blockpos.getX();
                            int l1 = blockpos.getY();
                            int i2 = blockpos.getZ();
                            IBlockState iblockstate = worldServerIn.getBlockState(blockpos);

                            if (!iblockstate.isNormalCube()) {
                                int j2 = 0;

                                for (int k2 = 0; k2 < 3; ++k2) {
                                    int l2 = k1;
                                    int i3 = l1;
                                    int j3 = i2;
                                    Biome.SpawnListEntry entry = null;
                                    IEntityLivingData ientitylivingdata = null;
                                    int l3 = MathHelper.ceil(Math.random() * 4.0D);

                                    for (int i4 = 0; i4 < l3; ++i4) {
                                        l2 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                        i3 += worldServerIn.rand.nextInt(1) - worldServerIn.rand.nextInt(1);
                                        j3 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                        mutable.setPos(l2, i3, j3);
                                        float f = (float) l2 + 0.5F;
                                        float f1 = (float) j3 + 0.5F;

                                        // edit: use config value for distance
                                        if (!worldServerIn.isAnyPlayerWithinRangeAt(f, i3, f1, Config.bloodMoonSpawnRadius) && blockpos1.distanceSq(f, i3, f1) >= 576.0D) {
                                            if (entry == null) {
                                                entry = worldServerIn.getSpawnListEntryForTypeAt(enumcreaturetype, mutable);

                                                if (entry == null) {
                                                    break;
                                                }
                                            }

                                            if (worldServerIn.canCreatureTypeSpawnHere(enumcreaturetype, entry, mutable) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(entry.entityClass), worldServerIn, mutable)) {
                                                EntityLiving entityliving;

                                                try {
                                                    entityliving = entry.newInstance(worldServerIn);
                                                } catch (Exception exception) {
                                                    exception.printStackTrace();
                                                    return j4;
                                                }

                                                // edit: only spawn allowed mobs
                                                ResourceLocation name = EntityList.getKey(entityliving);
                                                boolean listed = Config.mobDuplicationBlacklist.contains(name.toString());
                                                if (Config.isMobDuplicationWhitelist != listed) {
                                                    // it looks like setting entry to null here selects a new random entity to spawn
                                                    entry = null;
                                                    continue;
                                                }

                                                // edit: add data
                                                entityliving.getEntityData().setBoolean(Nyx.ID + ":blood_moon_spawn", true);

                                                entityliving.setLocationAndAngles(f, i3, f1, worldServerIn.rand.nextFloat() * 360.0F, 0.0F);

                                                net.minecraftforge.fml.common.eventhandler.Event.Result canSpawn = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(entityliving, worldServerIn, f, i3, f1, false);
                                                if (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW || (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT && (entityliving.getCanSpawnHere() && entityliving.isNotColliding()))) {
                                                    if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityliving, worldServerIn, f, i3, f1))
                                                        ientitylivingdata = entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);

                                                    if (entityliving.isNotColliding()) {
                                                        ++j2;
                                                        worldServerIn.spawnEntity(entityliving);
                                                    } else {
                                                        entityliving.setDead();
                                                    }

                                                    if (j2 >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(entityliving)) {
                                                        continue label134;
                                                    }
                                                }

                                                j4 += j2;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return j4;
        }
    }

    private static BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
        Chunk chunk = worldIn.getChunk(x, z);
        int i = x * 16 + worldIn.rand.nextInt(16);
        int j = z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }
}
