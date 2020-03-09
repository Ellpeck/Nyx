package de.ellpeck.nyx.events;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.entities.CauldronTracker;
import de.ellpeck.nyx.entities.FallingStar;
import de.ellpeck.nyx.entities.WolfAiFullMoon;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@EventBusSubscriber(modid = Nyx.ID)
public final class Events {

    private static final Method SET_SLIME_SIZE_METHOD = ObfuscationReflectionHelper.findMethod(EntitySlime.class, "func_70799_a", void.class, int.class, boolean.class);

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        NyxWorld data = NyxWorld.get(event.world);
        if (data == null)
            return;
        data.update();

        // Falling stars
        if (!event.world.isRemote && Config.fallingStars && !event.world.isDaytime() && event.world.getTotalWorldTime() % 20 == 0) {
            for (EntityPlayer player : event.world.playerEntities) {
                float chanceMult = data.currentEvent instanceof StarShower ? 15 : 1;
                if (event.world.rand.nextFloat() > Config.fallingStarRarity * chanceMult)
                    continue;
                BlockPos startPos = player.getPosition().add(event.world.rand.nextGaussian() * 20, 0, event.world.rand.nextGaussian() * 20);
                startPos = event.world.getHeight(startPos).up(MathHelper.getInt(event.world.rand, 32, 64));

                FallingStar star = new FallingStar(event.world);
                star.setPosition(startPos.getX(), startPos.getY(), startPos.getZ());
                event.world.spawnEntity(star);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        // Delet monsters
        if (!entity.world.isRemote && Config.bloodMoonVanish && entity.world.isDaytime() && entity.getEntityData().getBoolean(Nyx.ID + ":blood_moon_spawn"))
            entity.setDead();
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
            wolf.targetTasks.addTask(3, new WolfAiFullMoon(wolf));
        }
    }

    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        World world = event.getWorld();
        if (world.isRemote)
            return;
        NyxWorld nyx = NyxWorld.get(world);
        if (nyx == null || !(nyx.currentEvent instanceof HarvestMoon))
            return;
        if (world.rand.nextDouble() <= Config.harvestMoonGrowthChance)
            event.setResult(Event.Result.ALLOW);
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
                int i = slime.world.rand.nextInt(12);
                if (i < 6)
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
        } else if (nyx.currentEvent instanceof BloodMoon && event.getSpawner() == null) {
            for (int i = 1; i < Config.bloodMoonSpawnMultiplier; i++) {
                doExtraSpawn(entity, "blood_moon_spawn");
            }
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
