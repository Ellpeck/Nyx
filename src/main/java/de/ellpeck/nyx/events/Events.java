package de.ellpeck.nyx.events;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.entities.CauldronTracker;
import de.ellpeck.nyx.entities.FallingStar;
import de.ellpeck.nyx.network.PacketHandler;
import de.ellpeck.nyx.network.PacketNyxWorld;
import net.minecraft.block.Block;
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
import net.minecraft.entity.monster.IMob;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@EventBusSubscriber(modid = Nyx.ID)
public final class Events {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!event.world.hasCapability(Registry.worldCapability, null))
            return;
        NyxWorld data = event.world.getCapability(Registry.worldCapability, null);
        data.update();

        // Falling stars
        if (!event.world.isRemote && Config.fallingStars && !event.world.isDaytime() && event.world.getTotalWorldTime() % 20 == 0) {
            for (EntityPlayer player : event.world.playerEntities) {
                if (event.world.rand.nextFloat() > Config.fallingStarRarity)
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
    public static void onPlayerJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP) {
            World world = entity.getEntityWorld();
            if (!world.isRemote && world.hasCapability(Registry.worldCapability, null)) {
                PacketNyxWorld packet = new PacketNyxWorld(world.getCapability(Registry.worldCapability, null));
                PacketHandler.sendTo((EntityPlayerMP) entity, packet);
            }
        }
    }

    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        World world = event.getWorld();
        if (world.isRemote || !world.hasCapability(Registry.worldCapability, null))
            return;
        if (!world.getCapability(Registry.worldCapability, null).isHarvestMoon)
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
        if (!(entity instanceof IMob))
            return;

        // Don't spawn mobs during harvest moon
        if (Config.harvestMoon && entity.world.hasCapability(Registry.worldCapability, null)) {
            NyxWorld world = entity.world.getCapability(Registry.worldCapability, null);
            if (world.isHarvestMoon && event.getSpawner() == null) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onSpawn(LivingSpawnEvent.SpecialSpawn event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof IMob))
            return;
        if (entity.world.getCurrentMoonPhaseFactor() < 1)
            return;

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
        if (Config.additionalMobsChance > 0 && entity.world.rand.nextInt(Config.additionalMobsChance) == 0) {
            String key = Nyx.ID + ":added_spawn";
            if (!entity.getEntityData().getBoolean(key)) {
                ResourceLocation name = EntityList.getKey(entity);
                if (name != null) {
                    Entity spawned = spawnEntity(entity.world, entity.posX, entity.posY, entity.posZ, name);
                    if (spawned != null)
                        spawned.getEntityData().setBoolean(key, true);
                }
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

    private static Entity spawnEntity(World world, double x, double y, double z, ResourceLocation name) {
        Entity entity = EntityList.createEntityByIDFromName(name, world);
        if (!(entity instanceof EntityLiving))
            return null;
        EntityLiving living = (EntityLiving) entity;
        entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360), 0);
        living.rotationYawHead = living.rotationYaw;
        living.renderYawOffset = living.rotationYaw;
        if (ForgeEventFactory.doSpecialSpawn(living, world, (float) x, (float) y, (float) z, null))
            return null;
        living.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(living)), null);
        world.spawnEntity(entity);
        return entity;
    }
}
