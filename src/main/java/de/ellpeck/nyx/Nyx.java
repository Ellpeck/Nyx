package de.ellpeck.nyx;

import de.ellpeck.nyx.commands.CommandForce;
import de.ellpeck.nyx.network.PacketHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Nyx.ID, name = Nyx.NAME, version = Nyx.VERSION, guiFactory = "de.ellpeck.nyx.GuiFactory")
public class Nyx {

    public static final String ID = "nyx";
    public static final String NAME = "Nyx";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance
    public static Nyx instance;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        Registry.preInit();
        PacketHandler.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Registry.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandForce());
    }
}
