package de.ellpeck.nyx;

import com.google.common.collect.Sets;
import de.ellpeck.nyx.lunarevents.LunarEvent;
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
    public static double harvestMoonGrowthChance;
    public static double cometShardGuardianChance;
    public static boolean fallingStars;
    public static double fallingStarRarity;
    public static int nightTicks;
    public static boolean fullMoon;
    public static double cometShardChance;
    public static boolean bloodMoonSleeping;
    public static int bloodMoonSpawnMultiplier;
    public static Set<String> mobDuplicationBlacklist;
    public static boolean isMobDuplicationWhitelist;
    public static boolean bloodMoonVanish;
    public static int bloodMoonSpawnRadius;
    public static boolean harvestMoonOnFull;
    public static boolean bloodMoonOnFull;
    public static LunarEventConfig harvestMoon;
    public static LunarEventConfig starShowers;
    public static LunarEventConfig bloodMoon;

    public static void init(File file) {
        instance = new Configuration(file);
        instance.load();
        load();
    }

    public static void load() {
        allowedDimensions = Sets.newHashSet(instance.get("general", "allowedDimensions", new String[]{"overworld"}, "Names of the dimensions that lunar events should occur in").getStringList());
        lunarWater = instance.get("general", "lunarWater", true, "If lunar water should be enabled").getBoolean();
        cometShardGuardianChance = instance.get("general", "cometShardGuardianChance", 0.05, "The chance in percent (1 = 100%) for a comet shard to be dropped from an elder guardian", 0, 1).getDouble();
        nightTicks = instance.get("general", "nightTicks", 10000, "The amount of ticks that an in-game night lasts for").getInt();
        mobDuplicationBlacklist = Sets.newHashSet(instance.get("general", "mobDuplicationBlacklist", new String[0], "The registry names of entities that should not be spawned during the full and blood moons. If isMobDuplicationWhitelist is true, this acts as a whitelist instead.").getStringList());
        isMobDuplicationWhitelist = instance.get("general", "isMobDuplicationWhitelist", false, "If the mobDuplicationBlacklist should act as a whitelist instead").getBoolean();

        fullMoon = instance.get("fullMoon", "fullMoon", true, "If the vanilla full moon should be considered a proper lunar event").getBoolean();
        addPotionEffects = instance.get("fullMoon", "addPotionEffects", true, "If mobs spawned during a full moon should have random potion effects applied to them (similarly to spiders in the base game)").getBoolean();
        additionalMobsChance = instance.get("fullMoon", "additionalMobsChance", 5, "The chance for an additional mob to be spawned when a mob spawns during a full moon. The higher the number, the less likely. Set to 0 to disable.", 0, 1000).getInt();

        enchantments = instance.get("enchantments", "enchantments", true, "If the enchantments should be enabled").getBoolean();
        lunarEdgeXp = instance.get("enchantments", "lunarEdgeXp", true, "If a weapon enchanted with lunar edge should increase the experience drops of mobs").getBoolean();
        disallowDayEnchanting = instance.get("enchantments", "disallowDayEnchanting", true, "If enchanting should be disallowed during the day").getBoolean();

        harvestMoon = new LunarEventConfig("harvestMoon", "harvestMoon", "Harvest Moon", 0.05);
        harvestMoonGrowthChance = instance.get("harvestMoon", "harvestMoonGrowthChance", 0.8, "The chance in percent (1 = 100%) for any crop to get an extra growth tick each random tick during the harvest moon", 0, 1).getDouble();
        harvestMoonOnFull = instance.get("harvestMoon", "harvestMoonOnFull", false, "If the harvest moon should only occur on full moon nights").getBoolean();

        starShowers = new LunarEventConfig("fallingStars", "starShowers", "Star Showers", 0.05);
        fallingStars = instance.get("fallingStars", "fallingStars", true, "If falling stars should be enabled").getBoolean();
        fallingStarRarity = instance.get("fallingStars", "fallingStarRarity", 0.01F, "The chance in percent (1 = 100%) for a falling star to appear at night for each player each second", 0, 1).getDouble();
        cometShardChance = instance.get("fallingStars", "cometShardChance", 0.05, "The chance in percent (1 = 100%) for a falling star to spawn a comet shard instead of a fallen star item", 0, 1).getDouble();

        bloodMoon = new LunarEventConfig("bloodMoon", "bloodMoon", "Blood Moon", 0.05);
        bloodMoonSleeping = instance.get("bloodMoon", "bloodMoonSleeping", false, "If sleeping is allowed during a blood moon").getBoolean();
        bloodMoonSpawnMultiplier = instance.get("bloodMoon", "bloodMoonSpawnMultiplier", 2, "The multiplier with which mobs should spawn during the blood moon (eg 2 means 2 mobs spawn instead of 1)", 1, 1000).getInt();
        bloodMoonVanish = instance.get("bloodMoon", "bloodMoonVanish", true, "If mobs spawned by the blood moon should die at sunup").getBoolean();
        bloodMoonSpawnRadius = instance.get("bloodMoon", "bloodMoonSpawnRadius", 20, "The closest distance that mobs can spawn away from a player during the blood moon. Vanilla value is 24.").getInt();
        bloodMoonOnFull = instance.get("bloodMoon", "bloodMoonOnFull", false, "If the blood moon should only occur on full moon nights").getBoolean();

        if (instance.hasChanged())
            instance.save();
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
