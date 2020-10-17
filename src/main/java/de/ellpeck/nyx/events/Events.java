package de.ellpeck.nyx.events;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;
import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.entities.CauldronTracker;
import de.ellpeck.nyx.entities.FallingMeteor;
import de.ellpeck.nyx.entities.FallingStar;
import de.ellpeck.nyx.entities.WolfAiSpecialMoon;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.FullMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import de.ellpeck.nyx.network.PacketHandler;
import de.ellpeck.nyx.network.PacketNyxWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = Nyx.ID)
public final class Events {

    private static final Method SET_SLIME_SIZE_METHOD = ObfuscationReflectionHelper.findMethod(EntitySlime.class, "func_70799_a", void.class, int.class, boolean.class);
    private static final AttributeModifier METEOR_MOVEMENT_MODIFIER = new AttributeModifier(UUID.fromString("c1f96acc-e117-4dc1-a351-e295a5de6071"), Nyx.ID + ":meteor_movement_speed", -0.15F, 2); // 2 is multiply total

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (Config.meteors) {
            EntityPlayer player = event.player;
            // meteor armor speed reduction
            if (player.world.getTotalWorldTime() % 20 == 0) {
                IAttributeInstance speed = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                String key = Nyx.ID + ":meteor_equipped";
                NBTTagCompound nbt = player.getEntityData();
                // do we have 2 or more pieces equipped?
                boolean equipped = Streams.stream(player.getArmorInventoryList())
                        .filter(s -> s.getItem() instanceof ItemArmor && ((ItemArmor) s.getItem()).getArmorMaterial() == Registry.meteorArmorMaterial)
                        .count() >= 2;
                if (equipped && !nbt.getBoolean(key)) {
                    // we just equipped it
                    nbt.setBoolean(key, true);
                    if (!speed.hasModifier(METEOR_MOVEMENT_MODIFIER))
                        speed.applyModifier(METEOR_MOVEMENT_MODIFIER);
                } else if (!equipped && nbt.getBoolean(key)) {
                    // we just unequipped it
                    nbt.setBoolean(key, false);
                    speed.removeModifier(METEOR_MOVEMENT_MODIFIER);
                }
            }
            // meteor hammer ability executions
            // we check fall distance because we need the player to be done falling when removing the tag
            if (player.onGround && player.fallDistance <= 0 && player.getEntityData().hasKey(Nyx.ID + ":leap_start")) {
                if (!player.world.isRemote) {
                    long leapTime = player.world.getTotalWorldTime() - player.getEntityData().getLong(Nyx.ID + ":leap_start");
                    if (leapTime >= 5) {
                        int r = 3;
                        AxisAlignedBB area = new AxisAlignedBB(player.posX - r, player.posY - r, player.posZ - r, player.posX + r, player.posY + r, player.posZ + r);
                        DamageSource source = DamageSource.causePlayerDamage(player);
                        for (EntityLivingBase entity : player.world.getEntitiesWithinAABB(EntityLivingBase.class, area, EntitySelectors.IS_ALIVE)) {
                            if (entity == player)
                                continue;
                            entity.attackEntityFrom(source, 15);
                            entity.motionY = 1;
                        }
                        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1, 1);
                    }
                }
                player.getEntityData().removeTag(Nyx.ID + ":leap_start");
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        // meteor hammer ability
        if (Config.meteors && event.getEntityLiving().getEntityData().hasKey(Nyx.ID + ":leap_start"))
            event.setDamageMultiplier(0);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        // meteor armor explosion resistance
        if (Config.meteors && event.getSource().isExplosion()) {
            int equipped = (int) Streams.stream(event.getEntityLiving().getArmorInventoryList())
                    .filter(s -> s.getItem() instanceof ItemArmor && ((ItemArmor) s.getItem()).getArmorMaterial() == Registry.meteorArmorMaterial)
                    .count();
            event.setAmount(event.getAmount() * (1 - 0.1F * equipped));
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        // meteor sword stunning
        if (Config.meteors) {
            ItemStack sword = event.getEntityLiving().getHeldItemMainhand();
            if (sword.getItem() != Registry.meteorSword)
                return;
            Entity target = event.getTarget();
            if (target instanceof EntityLivingBase)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 15, 10, false, false));
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        // meteor axe shield damage
        if (Config.meteors) {
            Entity attacker = event.getSource().getTrueSource();
            if (!(attacker instanceof EntityLivingBase))
                return;
            ItemStack weapon = ((EntityLivingBase) attacker).getHeldItemMainhand();
            if (weapon.getItem() != Registry.meteorAxe)
                return;
            EntityLivingBase target = event.getEntityLiving();
            if (!(target instanceof EntityPlayer))
                return;
            EntityPlayer player = (EntityPlayer) target;
            if (!player.isHandActive())
                return;
            ItemStack active = player.getActiveItemStack();
            if (active.getItem() instanceof ItemShield)
                active.damageItem(13, player);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        NyxWorld data = NyxWorld.get(event.world);
        if (data == null)
            return;
        data.update();

        // Falling stars
        if (!event.world.isRemote && Config.fallingStars && !NyxWorld.isDaytime(event.world) && event.world.getTotalWorldTime() % 20 == 0) {
            String dimension = event.world.provider.getDimensionType().getName();
            if (Config.allowedDimensions.contains(dimension)) {
                for (EntityPlayer player : event.world.playerEntities) {
                    float chanceMult = data.currentEvent instanceof StarShower ? 15 : 1;
                    if (event.world.rand.nextFloat() > Config.fallingStarRarity * chanceMult)
                        continue;
                    BlockPos startPos = player.getPosition().add(event.world.rand.nextGaussian() * 20, 0, event.world.rand.nextGaussian() * 20);
                    startPos = event.world.getPrecipitationHeight(startPos).up(MathHelper.getInt(event.world.rand, 32, 64));

                    FallingStar star = new FallingStar(event.world);
                    star.setPosition(startPos.getX(), startPos.getY(), startPos.getZ());
                    event.world.spawnEntity(star);
                }
            }
        }

        // Meteors
        meteors:
        if (!event.world.isRemote && Config.meteors && event.world.getTotalWorldTime() % 20 == 0) {
            if (event.world.playerEntities.size() <= 0)
                break meteors;
            EntityPlayer selectedPlayer = event.world.playerEntities.get(event.world.rand.nextInt(event.world.playerEntities.size()));
            if (selectedPlayer == null)
                break meteors;
            double spawnX = selectedPlayer.posX + MathHelper.nextDouble(event.world.rand, -Config.meteorSpawnRadius, Config.meteorSpawnRadius);
            double spawnZ = selectedPlayer.posZ + MathHelper.nextDouble(event.world.rand, -Config.meteorSpawnRadius, Config.meteorSpawnRadius);
            BlockPos spawnPos = new BlockPos(spawnX, 0, spawnZ);
            double chance = Config.getMeteorChance(event.world, data);
            MutableInt ticksInArea = data.playersPresentTicks.get(new ChunkPos(spawnPos));
            if (ticksInArea != null && ticksInArea.intValue() >= Config.meteorDisallowTime)
                chance /= Math.pow(2, ticksInArea.intValue() / (double) Config.meteorDisallowTime);
            if (!(event.world.rand.nextFloat() <= chance))
                break meteors;
            if (!event.world.isBlockLoaded(spawnPos, false)) {
                // add meteor information to cache
                data.cachedMeteorPositions.add(spawnPos);
                data.sendToClients();
            } else {
                // spawn meteor entity
                FallingMeteor.spawn(data.world, spawnPos);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote)
            return;
        NyxWorld data = NyxWorld.get(world);
        if (data == null)
            return;
        Chunk chunk = event.getChunk();
        ChunkPos cp = chunk.getPos();

        // spawn meteors from the cache
        if (Config.meteors) {
            List<BlockPos> meteors = data.cachedMeteorPositions.stream()
                    .filter(p -> p.getX() >= cp.getXStart() && p.getZ() >= cp.getZStart() && p.getX() <= cp.getXEnd() && p.getZ() <= cp.getZEnd())
                    .collect(Collectors.toList());
            for (BlockPos pos : meteors)
                FallingMeteor.spawn(data.world, pos);
            data.cachedMeteorPositions.removeAll(meteors);
            data.sendToClients();
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        // Delet monsters spawned by blood moon
        if (Config.bloodMoonVanish && !entity.world.isRemote && NyxWorld.isDaytime(entity.world) && entity.getEntityData().getBoolean(Nyx.ID + ":blood_moon_spawn")) {
            ((WorldServer) entity.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, entity.posX, entity.posY, entity.posZ, 10, 0.5, 1, 0.5, 0);
            entity.setDead();
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getEntityWorld();
        if (world.isRemote)
            return;
        NyxWorld nyx = NyxWorld.get(world);
        if (nyx == null)
            return;

        if (entity instanceof EntityPlayerMP) {
            PacketNyxWorld packet = new PacketNyxWorld(nyx);
            PacketHandler.sendTo((EntityPlayerMP) entity, packet);
        } else if (entity instanceof EntityWolf) {
            EntityWolf wolf = (EntityWolf) entity;
            wolf.targetTasks.addTask(3, new WolfAiSpecialMoon(wolf));
        }
    }

    @SubscribeEvent
    public static void onExpDrop(LivingExperienceDropEvent event) {
        if (Config.enchantments && Config.lunarEdgeXp) {
            EntityPlayer player = event.getAttackingPlayer();
            if (player == null)
                return;
            ItemStack held = player.getHeldItemMainhand();
            int level = EnchantmentHelper.getEnchantmentLevel(Registry.lunarEdge, held);
            if (level <= 0)
                return;
            float exp = event.getDroppedExperience();
            float mod = 2 * (level / (float) Registry.lunarEdge.getMaxLevel());
            event.setDroppedExperience(MathHelper.floor(exp * mod));
        }
    }

    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        List<EntityItem> drops = event.getDrops();
        if (entity instanceof EntityElderGuardian && !entity.world.isRemote && entity.world.rand.nextDouble() <= Config.cometShardGuardianChance) {
            ItemStack stack = new ItemStack(Registry.cometShard, (event.getLootingLevel() / 2) + 1);
            drops.add(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, stack));
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof IMob) || entity instanceof EntitySlime)
            return;

        // Don't spawn mobs during harvest moon
        if (event.getSpawner() == null) {
            NyxWorld nyx = NyxWorld.get(entity.world);
            if (nyx != null && nyx.currentEvent instanceof HarvestMoon)
                event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onSpawn(LivingSpawnEvent.SpecialSpawn event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof IMob))
            return;
        NyxWorld nyx = NyxWorld.get(entity.world);
        if (nyx == null)
            return;

        // Bigger slimes
        if (entity instanceof EntitySlime) {
            EntitySlime slime = (EntitySlime) entity;
            int size = slime.getSlimeSize();
            if (nyx.currentEvent instanceof FullMoon) {
                int i = slime.world.rand.nextInt(5);
                if (i <= 1)
                    size += 2;
                if (i <= 2)
                    size += 2;
            } else if (nyx.currentEvent instanceof HarvestMoon) {
                int i = slime.world.rand.nextInt(15);
                if (i < 8)
                    size += i * 2;
            }
            if (size != slime.getSlimeSize()) {
                try {
                    SET_SLIME_SIZE_METHOD.invoke(slime, size, true);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                // Cancelling this event just suppresses onInitialSpawn, doc is wrong
                event.setCanceled(true);
            }
        }

        if (nyx.currentEvent instanceof FullMoon) {
            // Set random effect
            if (Config.addPotionEffects && !(entity instanceof EntityCreeper)) {
                Potion effect = null;
                int i = entity.world.rand.nextInt(20);
                if (i <= 2) {
                    effect = MobEffects.SPEED;
                } else if (i <= 4) {
                    effect = MobEffects.STRENGTH;
                } else if (i <= 6) {
                    effect = MobEffects.REGENERATION;
                } else if (i <= 7) {
                    effect = MobEffects.INVISIBILITY;
                }
                if (effect != null)
                    entity.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE));
            }

            // Spawn a second one
            if (Config.additionalMobsChance > 0 && entity.world.rand.nextInt(Config.additionalMobsChance) == 0)
                doExtraSpawn(entity, "full_moon_spawn");
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (Nyx.ID.equals(event.getModID()))
            Config.load();
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        NyxWorld nyx = NyxWorld.get(world);

        ench:
        if (Config.disallowDayEnchanting) {
            long time = world.getWorldTime() % 24000;
            if (time > 13000 && time < 23000)
                break ench;
            if (!(block instanceof BlockEnchantmentTable))
                break ench;
            if (Config.enchantingWhitelistDimensions.contains(world.provider.getDimensionType().getName()))
                break ench;
            event.setUseBlock(Event.Result.DENY);
            player.sendStatusMessage(new TextComponentTranslation("info." + Nyx.ID + ".day_enchanting"), true);
        }

        lunar:
        if (Config.lunarWater) {
            if (!(block instanceof BlockCauldron))
                break lunar;
            int level = state.getValue(BlockCauldron.LEVEL);
            if (level > 0)
                break lunar;

            ItemStack holding = player.getHeldItem(EnumHand.MAIN_HAND);
            if (holding.isEmpty())
                break lunar;
            FluidStack fluid = FluidUtil.getFluidContained(holding);
            if (fluid == null || fluid.getFluid() != Registry.lunarWaterFluid || fluid.amount < 1000)
                break lunar;
            level = 3;

            if (!world.isRemote) {
                player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                world.setBlockState(pos, Registry.lunarWaterCauldron.getDefaultState().withProperty(BlockCauldron.LEVEL, level));
            }

            player.swingArm(EnumHand.MAIN_HAND);
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }

        if (nyx != null && nyx.currentEvent instanceof BloodMoon && !Config.bloodMoonSleeping && block instanceof BlockBed)
            player.sendStatusMessage(new TextComponentTranslation("info." + Nyx.ID + ".blood_moon_sleeping"), true);
    }

    @SubscribeEvent
    public static void onBlockChanged(BlockEvent.NeighborNotifyEvent event) {
        World world = event.getWorld();
        if (world.isRemote)
            return;

        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (!(block instanceof BlockCauldron))
            return;
        if (!world.getEntitiesWithinAABB(CauldronTracker.class, new AxisAlignedBB(pos)).isEmpty())
            return;

        CauldronTracker tracker = new CauldronTracker(world);
        tracker.setTrackingPos(pos);
        world.spawnEntity(tracker);
    }

    @SubscribeEvent
    public static void onWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(new ResourceLocation(Nyx.ID, "world_cap"), new NyxWorld(event.getObject()));
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        NyxWorld nyx = NyxWorld.get(player.world);
        if (nyx != null && nyx.currentEvent instanceof BloodMoon && !Config.bloodMoonSleeping)
            event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
    }

    private static void doExtraSpawn(Entity original, String key) {
        String addedSpawnKey = Nyx.ID + ":" + key;
        if (!original.getEntityData().getBoolean(addedSpawnKey)) {
            ResourceLocation name = EntityList.getKey(original);
            if (name != null) {
                boolean listed = Config.mobDuplicationBlacklist.contains(name.toString());
                if (Config.isMobDuplicationWhitelist != listed)
                    return;

                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            if (x == 0 && y == 0 && z == 0)
                                continue;
                            BlockPos offset = original.getPosition().add(x, y, z);
                            if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, original.world, offset))
                                continue;
                            Entity entity = EntityList.createEntityByIDFromName(name, original.world);
                            if (!(entity instanceof EntityLiving))
                                return;
                            EntityLiving living = (EntityLiving) entity;
                            entity.setLocationAndAngles(original.posX + x, original.posY + y, original.posZ + z, MathHelper.wrapDegrees(original.world.rand.nextFloat() * 360), 0);
                            living.rotationYawHead = living.rotationYaw;
                            living.renderYawOffset = living.rotationYaw;
                            living.getEntityData().setBoolean(addedSpawnKey, true);
                            if (!ForgeEventFactory.doSpecialSpawn(living, original.world, (float) original.posX + x, (float) original.posY + y, (float) original.posZ + z, null))
                                living.onInitialSpawn(original.world.getDifficultyForLocation(new BlockPos(living)), null);
                            original.world.spawnEntity(entity);
                            return;
                        }
                    }
                }
            }
        }
    }
}
