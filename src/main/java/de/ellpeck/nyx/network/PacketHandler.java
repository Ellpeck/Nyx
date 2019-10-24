package de.ellpeck.nyx.network;

import de.ellpeck.nyx.Nyx;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler {

    private static SimpleNetworkWrapper network;

    public static void init() {
        network = new SimpleNetworkWrapper(Nyx.ID);
        network.registerMessage(PacketNyxWorld.Handler.class, PacketNyxWorld.class, 0, Side.CLIENT);
    }

    public static void sendTo(EntityPlayer player, IMessage message) {
        network.sendTo(message, (EntityPlayerMP) player);
    }
}
