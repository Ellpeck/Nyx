package de.ellpeck.nyx.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;

public class ClientProxy extends CommonProxy {

    @Override
    public void sendBreakPacket(BlockPos pos) {
        NetHandlerPlayClient netHandlerPlayClient = Minecraft.getMinecraft().getConnection();
        assert netHandlerPlayClient != null;
        netHandlerPlayClient.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, Minecraft.getMinecraft().objectMouseOver.sideHit));
    }
}
