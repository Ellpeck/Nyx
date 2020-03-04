package de.ellpeck.nyx;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class Config {

    public static Configuration instance;
    public static boolean enchantments;
    public static boolean lunarWater;
    public static boolean harvestMoon;
    public static boolean addPotionEffects;
    public static int additionalMobsChance;
    public static boolean lunarEdgeXp;
    public static boolean disallowDayEnchanting;
    public static double harvestMoonChance;
    public static double harvestMoonGrowthChance;

    public static void init(File file) {
        instance = new Configuration(file);
        instance.load();
        load();
    }

    public static void load() {
        enchantments = instance.get(Configuration.CATEGORY_GENERAL, "enchantments", true, "If the enchantments should be enabled").getBoolean();
        lunarWater = instance.get(Configuration.CATEGORY_GENERAL, "lunarWater", true, "If lunar water should be enabled").getBoolean();
        harvestMoon = instance.get(Configuration.CATEGORY_GENERAL, "harvestMoon", true, "If the harvest moon should be enabled").getBoolean();
        addPotionEffects = instance.get(Configuration.CATEGORY_GENERAL, "addPotionEffects", true, "If mobs spawned during a full moon should have random potion effects applied to them (similarly to spiders in the base game)").getBoolean();
        additionalMobsChance = instance.get(Configuration.CATEGORY_GENERAL, "additionalMobsChance", 5, "The chance for an additional mob to be spawned when a mob spawns during a full moon. The higher the number, the less likely. Set to 0 to disable.", 0, 1000).getInt();
        lunarEdgeXp = instance.get(Configuration.CATEGORY_GENERAL, "lunarEdgeXp", true, "If a weapon enchanted with lunar edge should increase the experience drops of mobs").getBoolean();
        disallowDayEnchanting = instance.get(Configuration.CATEGORY_GENERAL, "disallowDayEnchanting", true, "If enchanting should be disallowed during the day").getBoolean();
        harvestMoonChance = instance.get(Configuration.CATEGORY_GENERAL, "harvestMoonChance", 0.05, "The chance in percent (1 = 100%) of the harvest moon occuring on a full moon night", 0, 1).getDouble();
        harvestMoonGrowthChance = instance.get(Configuration.CATEGORY_GENERAL, "harvestMoonGrowthChance", 0.8, "The chance in percent (1 = 100%) for any crop to get an extra growth tick each random tick during the harvest moon", 0, 1).getDouble();

        if (instance.hasChanged())
            instance.save();
    }
}
