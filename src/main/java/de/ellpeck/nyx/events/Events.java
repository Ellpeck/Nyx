package de.ellpeck.nyx.events;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@EventBusSubscriber(modid = Nyx.ID)
public final class Events {

    public static float moonPhase;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.provider.getDimensionType() == DimensionType.OVERWORLD)
            moonPhase = event.world.getCurrentMoonPhaseFactor();
    }

    @SubscribeEvent
    public static void onExpDrop(LivingExperienceDropEvent event) {
        if (Nyx.enchantments && Nyx.lunarEdgeXp) {
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
    public static void onSpawn(LivingSpawnEvent.SpecialSpawn event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof IMob))
            return;
        if (entity.world.getCurrentMoonPhaseFactor() < 1)
            return;

        // Set random effect
        if (Nyx.addPotionEffects) {
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
        if (Nyx.additionalMobsChance > 0 && entity.world.rand.nextInt(Nyx.additionalMobsChance) == 0) {
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
            Nyx.loadConfig();
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock event) {
        if (Nyx.disallowDayEnchanting) {
            World world = event.getWorld();
            long time = world.getWorldTime() % 24000;
            if (time > 13000 && time < 23000)
                return;
            BlockPos pos = event.getPos();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (!(block instanceof BlockEnchantmentTable))
                return;
            event.setUseBlock(Event.Result.DENY);
            event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("info." + Nyx.ID + ".day_enchanting"), true);
        }
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
