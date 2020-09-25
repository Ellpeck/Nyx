package de.ellpeck.nyx.commands;

import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.entities.FallingMeteor;
import de.ellpeck.nyx.lunarevents.LunarEvent;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandMeteor extends CommandBase {
    @Override
    public String getName() {
        return "nyxmeteor";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.nyx.meteor.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0 && args.length != 2)
            throw new WrongUsageException(this.getUsage(sender));

        double x = sender.getPosition().getX();
        double z = sender.getPosition().getZ();
        if (args.length == 2) {
            x = parseDouble(x, args[0], false);
            z = parseDouble(z, args[1], false);
        }

        BlockPos pos = new BlockPos(x, 0, z);
        pos = FallingMeteor.spawn(sender.getEntityWorld(), pos).getPosition();
        notifyCommandListener(sender, this, "command.nyx.meteor.success", pos.getX(), pos.getY(), pos.getZ());
    }
}
