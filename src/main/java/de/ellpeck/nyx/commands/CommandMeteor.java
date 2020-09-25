package de.ellpeck.nyx.commands;

import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.entities.FallingMeteor;
import de.ellpeck.nyx.lunarevents.LunarEvent;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

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
        if (args.length != 0 && args.length != 2 && args.length != 3)
            throw new WrongUsageException(this.getUsage(sender));

        double x = sender.getPosition().getX();
        double z = sender.getPosition().getZ();
        Integer size = null;
        if (args.length >= 2) {
            x = parseDouble(x, args[0], false);
            z = parseDouble(z, args[1], false);
            if (args.length == 3)
                size = parseInt(args[2], 1);
        }

        BlockPos pos = new BlockPos(x, 0, z);
        FallingMeteor meteor = FallingMeteor.spawn(sender.getEntityWorld(), pos);
        if (size != null)
            meteor.size = size;
        pos = meteor.getPosition();
        notifyCommandListener(sender, this, "command.nyx.meteor.success", pos.getX(), pos.getY(), pos.getZ());
    }
}
