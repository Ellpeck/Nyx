package de.ellpeck.nyx;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Nyx.ID, name = Nyx.NAME, version = Nyx.VERSION, guiFactory = "de.ellpeck.nyx.GuiFactory")
public class Nyx {

    public static final String ID = "nyx";
    public static final String NAME = "Nyx";
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Instance
    public static Nyx instance;

    public static Configuration config;
    public static boolean enchantments;
    public static boolean addPotionEffects;
    public static int additionalMobsChance;
    public static boolean lunarEdgeXp;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        loadConfig();
    }

    public static void loadConfig() {
        enchantments = config.get(Configuration.CATEGORY_GENERAL, "enchantments", true, "If the enchantments should be enabled").getBoolean();
        addPotionEffects = config.get(Configuration.CATEGORY_GENERAL, "addPotionEffects", true, "If mobs spawned during a full moon should have random potion effects applied to them (similarly to spiders in the base game)").getBoolean();
        additionalMobsChance = config.get(Configuration.CATEGORY_GENERAL, "additionalMobsChance", 5, "The chance for an additional mob to be spawned when a mob spawns during a full moon. The higher the number, the less likely. Set to 0 to disable.", 0, 1000).getInt();
        lunarEdgeXp = config.get(Configuration.CATEGORY_GENERAL, "lunarEdgeXp", true, "If a weapon enchanted with lunar edge should increase the experience drops of mobs").getBoolean();

        if (config.hasChanged())
            config.save();
    }
}
