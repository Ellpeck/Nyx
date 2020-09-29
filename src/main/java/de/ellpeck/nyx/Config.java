package de.ellpeck.nyx;

import com.google.common.collect.Sets;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.LunarEvent;
import de.ellpeck.nyx.lunarevents.StarShower;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Set;

public final class Config {

    public static Configuration instance;
    public static Set<String> allowedDimensions;
    public static boolean enchantments;
    public static boolean lunarWater;
    public static boolean addPotionEffects;
    public static int additionalMobsChance;
    public static boolean lunarEdgeXp;
    public static boolean disallowDayEnchanting;
    public static double cometShardGuardianChance;
    public static boolean fallingStars;
    public static double fallingStarRarity;
    public static boolean fullMoon;
    public static boolean bloodMoonSleeping;
    public static int bloodMoonSpawnMultiplier;
    public static Set<String> mobDuplicationBlacklist;
    public static boolean isMobDuplicationWhitelist;
    public static boolean bloodMoonVanish;
    public static int bloodMoonSpawnRadius;
    public static boolean harvestMoonOnFull;
    public static boolean bloodMoonOnFull;
    public static boolean moonEventTint;
    public static int harvestMoonGrowAmount;
    public static int harvestMoonGrowInterval;
    public static LunarEventConfig harvestMoon;
    public static LunarEventConfig starShowers;
    public static LunarEventConfig bloodMoon;
    public static int[] lunarWaterTicks;
    public static double meteorChance;
    public static double meteorChanceNight;
    public static String meteorGateDimension;
    public static double meteorChanceAfterGate;
    public static double meteorChanceAfterGateNight;
    public static double meteorChanceStarShower;
    public static double meteorChanceEnd;
    public static int meteorSpawnRadius;
    public static boolean meteors;
    public static int meteorDisallowRadius;
    public static int meteorDisallowTime;
    public static Set<String> enchantingWhitelistDimensions;

    public static void init(File file) {
        instance = new Configuration(file);
        instance.load();
        load();
    }

    public static void load() {
        allowedDimensions = Sets.newHashSet(instance.get("general", "allowedDimensions", new String[]{"overworld"}, "Names of the dimensions that lunar events should occur in").getStringList());
        lunarWater = instance.get("general", "lunarWater", true, "If lunar water should be enabled").getBoolean();
        cometShardGuardianChance = instance.get("general", "cometShardGuardianChance", 0.05, "The chance in percent (1 = 100%) for a comet shard to be dropped from an elder guardian", 0, 1).getDouble();
        mobDuplicationBlacklist = Sets.newHashSet(instance.get("general", "mobDuplicationBlacklist", new String[0], "The registry names of entities that should not be spawned during the full and blood moons. If isMobDuplicationWhitelist is true, this acts as a whitelist instead.").getStringList());
        isMobDuplicationWhitelist = instance.get("general", "isMobDuplicationWhitelist", false, "If the mobDuplicationBlacklist should act as a whitelist instead").getBoolean();
        moonEventTint = instance.get("general", "moonEventTint", true, "If moon events should tint the sky").getBoolean();
        lunarWaterTicks = instance.get("general", "lunarWaterTicks", new int[]{1200, -1, 4800, 4800, 3600, 3600, 2400, 2400, 600, -1}, "The amount of ticks that a cauldron of water must be exposed to the night sky to be ready to turn into lunar water, per moon phase. From first to last, the entries are: Full moon, new moon, waning crescent, waxing crescent, third quarter, first quarter, waning gibbous, waxing gibbous, harvest moon and blood moon. Set any entry to -1 to disable lunar water production for that phase.").getIntList();

        fullMoon = instance.get("fullMoon", "fullMoon", true, "If the vanilla full moon should be considered a proper lunar event").getBoolean();
        addPotionEffects = instance.get("fullMoon", "addPotionEffects", true, "If mobs spawned during a full moon should have random potion effects applied to them (similarly to spiders in the base game)").getBoolean();
        additionalMobsChance = instance.get("fullMoon", "additionalMobsChance", 5, "The chance for an additional mob to be spawned when a mob spawns during a full moon. The higher the number, the less likely. Set to 0 to disable.", 0, 1000).getInt();

        enchantments = instance.get("enchantments", "enchantments", true, "If the enchantments should be enabled").getBoolean();
        lunarEdgeXp = instance.get("enchantments", "lunarEdgeXp", true, "If a weapon enchanted with lunar edge should increase the experience drops of mobs").getBoolean();
        disallowDayEnchanting = instance.get("enchantments", "disallowDayEnchanting", true, "If enchanting should be disallowed during the day").getBoolean();
        enchantingWhitelistDimensions = Sets.newHashSet(instance.get("enchantments", "enchantingWhitelistDimensions", new String[]{"the_nether", "the_end"}, "A list of names of dimensions where enchanting is always allowed, and not just at night").getStringList());

        harvestMoon = new LunarEventConfig("harvestMoon", "harvestMoon", "Harvest Moon", 0.05);
        harvestMoonOnFull = instance.get("harvestMoon", "harvestMoonOnFull", true, "If the harvest moon should only occur on full moon nights").getBoolean();
        harvestMoonGrowAmount = instance.get("harvestMoon", "harvestMoonGrowAmount", 15, "The amount of plants that should be grown per chunk during the harvest moon", 0, 100).getInt();
        harvestMoonGrowInterval = instance.get("harvestMoon", "harvestMoonGrowInterval", 10, "The amount of ticks that should pass before plants are grown again during the harvest moon", 1, 100).getInt();

        starShowers = new LunarEventConfig("fallingStars", "starShowers", "Star Showers", 0.05);
        fallingStars = instance.get("fallingStars", "fallingStars", true, "If falling stars should be enabled").getBoolean();
        fallingStarRarity = instance.get("fallingStars", "fallingStarRarity", 0.01F, "The chance in percent (1 = 100%) for a falling star to appear at night for each player each second", 0, 1).getDouble();

        bloodMoon = new LunarEventConfig("bloodMoon", "bloodMoon", "Blood Moon", 0.05);
        bloodMoonSleeping = instance.get("bloodMoon", "bloodMoonSleeping", false, "If sleeping is allowed during a blood moon").getBoolean();
        bloodMoonSpawnMultiplier = instance.get("bloodMoon", "bloodMoonSpawnMultiplier", 2, "The multiplier with which mobs should spawn during the blood moon (eg 2 means 2 mobs spawn instead of 1)", 1, 1000).getInt();
        bloodMoonVanish = instance.get("bloodMoon", "bloodMoonVanish", true, "If mobs spawned by the blood moon should die at sunup").getBoolean();
        bloodMoonSpawnRadius = instance.get("bloodMoon", "bloodMoonSpawnRadius", 20, "The closest distance that mobs can spawn away from a player during the blood moon. Vanilla value is 24.").getInt();
        bloodMoonOnFull = instance.get("bloodMoon", "bloodMoonOnFull", true, "If the blood moon should only occur on full moon nights").getBoolean();

        meteors = instance.get("meteors", "meteors", true, "If meteor content should be enabled").getBoolean();
        meteorChance = instance.get("meteors", "meteorChance", 0.00014, "The chance of a meteor spawning every second, during the day").getDouble();
        meteorChanceNight = instance.get("meteors", "meteorChanceNight", 0.0024, "The chance of a meteor spawning every second, during nighttime").getDouble();
        meteorGateDimension = instance.get("meteors", "meteorGateDimension", "the_nether", "The dimension that needs to be entered to increase the spawning of meteors").getString();
        meteorChanceAfterGate = instance.get("meteors", "meteorChanceAfterGate", 0.0002, "The chance of a meteor spawning every second, during the day, after the gate dimension has been entered once").getDouble();
        meteorChanceAfterGateNight = instance.get("meteors", "meteorChanceAfterGateNight", 0.003, "The chance of a meteor spawning every second, during the day, after the gate dimension has been entered once").getDouble();
        meteorChanceStarShower = instance.get("meteors", "meteorChanceStarShower", 0.0075, "The chance of a meteor spawning every second, during a star shower").getDouble();
        meteorChanceEnd = instance.get("meteors", "meteorChanceEnd", 0.003, "The chance of a meteor spawning every second, in the end dimension").getDouble();
        meteorSpawnRadius = instance.get("meteors", "meteorSpawnRadius", 1000, "The amount of blocks a meteor can spawn away from the nearest player").getInt();
        meteorDisallowRadius = instance.get("meteors", "meteorDisallowRadius", 16, "The radius in chunks that should be marked as invalid for meteor spawning around each player").getInt();
        meteorDisallowTime = instance.get("meteors", "meteorDisallowTime", 12000, "The amount of ticks that need to pass for each player until the chance of a meteor spawning in the area is halved (and then halved again, and so on)").getInt();

        if (instance.hasChanged())
            instance.save();
    }

    public static double getMeteorChance(World world, NyxWorld data) {
        // TODO nether gate to change the chance
        if (world.provider.getDimensionType() == DimensionType.THE_END) {
            return Config.meteorChanceEnd;
        } else if (!NyxWorld.isDaytime(world)) {
            if (data.currentEvent instanceof StarShower) {
                return Config.meteorChanceStarShower;
            } else {
                return Config.meteorChanceNight;
            }
        }
        return Config.meteorChance;
    }

    public static class LunarEventConfig {

        public boolean enabled;
        public double chance;
        public int startNight;
        public int nightInterval;
        public int graceDays;

        public LunarEventConfig(String category, String name, String displayName, double defaultChance) {
            this.enabled = instance.get(category, name, true, "If the " + displayName + " should be enabled").getBoolean();
            this.chance = instance.get(category, name + "Chance", defaultChance, "The chance in percent (1 = 100%) of the " + displayName + " occuring", 0, 1).getDouble();
            this.startNight = instance.get(category, name + "StartNight", 0, "The amount of nights that should pass before the " + displayName + " occurs for the first time", 0, 1000).getInt();
            this.nightInterval = instance.get(category, name + "Interval", 0, "The interval in days at which the " + displayName + " should occur. Overrides chance setting if set to a value greater than 0.", 0, 1000).getInt();
            this.graceDays = instance.get(category, name + "GracePeriod", 0, "The amount of days that should pass until the " + displayName + " happens again", 0, 1000).getInt();
        }
    }
}
