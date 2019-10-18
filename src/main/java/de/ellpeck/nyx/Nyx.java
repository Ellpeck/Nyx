package de.ellpeck.nyx;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Nyx.ID, name = Nyx.NAME, version = Nyx.VERSION)
public class Nyx {

    public static final String ID = "nyx";
    public static final String NAME = "Nyx";
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Instance
    public static Nyx instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }
}
