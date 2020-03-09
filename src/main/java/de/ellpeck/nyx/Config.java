package de.ellpeck.nyx;

import com.google.common.collect.Sets;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Set;

public final class Config {

    public static Configuration instance;
    public static Set<String> allowedDimensions;
    public static boolean enchantments;
    public static boolean lunarWater;
    public static boolean harvestMoon;
    public static boolean addPotionEffects;
    public static int additionalMobsChance;
    public static boolean lunarEdgeXp;
    public static boolean disallowDayEnchanting;
    public static double harvestMoonChance;
    public static double harvestMoonGrowthChance;
    public static double cometShardGuardianChance;
    public static boolean fallingStars;
    public static double fallingStarRarity;
    public static int nightTicks;
    public static boolean starShowers;
    public static double starShowerRarity;
    public static boolean fullMoon;
    public static double cometShardChance;
    public static boolean bloodMoon;
    public static double bloodMoonChance;
    public static boolean bloodMoonSleeping;
    public static int bloodMoonSpawnMultiplier;

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

        fullMoon = instance.get("fullMoon", "fullMoon", true, "If the vanilla full moon should be considered a proper lunar event").getBoolean();
        addPotionEffects = instance.get("fullMoon", "addPotionEffects", true, "If mobs spawned during a full moon should have random potion effects applied to them (similarly to spiders in the base game)").getBoolean();
        additionalMobsChance = instance.get("fullMoon", "additionalMobsChance", 5, "The chance for an additional mob to be spawned when a mob spawns during a full moon. The higher the number, the less likely. Set to 0 to disable.", 0, 1000).getInt();

        enchantments = instance.get("enchantments", "enchantments", true, "If the enchantments should be enabled").getBoolean();
        lunarEdgeXp = instance.get("enchantments", "lunarEdgeXp", true, "If a weapon enchanted with lunar edge should increase the experience drops of mobs").getBoolean();
        disallowDayEnchanting = instance.get("enchantments", "disallowDayEnchanting", true, "If enchanting should be disallowed during the day").getBoolean();

        harvestMoon = instance.get("harvestMoon", "harvestMoon", true, "If the harvest moon should be enabled").getBoolean();
        harvestMoonChance = instance.get("harvestMoon", "harvestMoonChance", 0.05, "The chance in percent (1 = 100%) of the harvest moon occuring on a full moon night", 0, 1).getDouble();
        harvestMoonGrowthChance = instance.get("harvestMoon", "harvestMoonGrowthChance", 0.8, "The chance in percent (1 = 100%) for any crop to get an extra growth tick each random tick during the harvest moon", 0, 1).getDouble();

        fallingStars = instance.get("fallingStars", "fallingStars", true, "If falling stars should be enabled").getBoolean();
        fallingStarRarity = instance.get("fallingStars", "fallingStarRarity", 0.01F, "The chance in percent (1 = 100%) for a falling star to appear at night for each player each second", 0, 1).getDouble();
        starShowers = instance.get("fallingStars", "starShowers", true, "If star showers should be enabled").getBoolean();
        starShowerRarity = instance.get("fallingStars", "starShowerRarity", 0.05, "The chance in percent (1 = 100%) of a star shower occuring on any given night", 0, 1).getDouble();
        cometShardChance = instance.get("fallingStars", "cometShardChance", 0.05, "The chance in percent (1 = 100%) for a falling star to spawn a comet shard instead of a fallen star item", 0, 1).getDouble();

        bloodMoon = instance.get("bloodMoon", "bloodMoon", true, "If the blood moon should be enabled").getBoolean();
        bloodMoonChance = instance.get("bloodMoon", "bloodMoonChance", 0.05, "The chance in percent (1 = 100%) of the blood moon occuring on a full moon night", 0, 1).getDouble();
        bloodMoonSleeping = instance.get("bloodMoon", "bloodMoonSleeping", false, "If sleeping is allowed during a blood moon").getBoolean();
        bloodMoonSpawnMultiplier = instance.get("bloodMoon", "bloodMoonSpawnMultiplier", 2, "The multiplier with which mobs should spawn during the blood moon (eg 2 means 2 mobs spawn instead of 1)", 1, 1000).getInt();

        if (instance.hasChanged())
            instance.save();
    }
}
