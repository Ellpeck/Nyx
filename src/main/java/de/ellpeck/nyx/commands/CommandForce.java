package de.ellpeck.nyx.commands;

import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.LunarEvent;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandForce extends CommandBase {
    @Override
    public String getName() {
        return "nyxforce";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.nyx.force.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1)
            throw new WrongUsageException(this.getUsage(sender));
        NyxWorld world = NyxWorld.get(sender.getEntityWorld());
        if (world == null)
            return;
        if ("clear".equals(args[0])) {
            world.forcedEvent = null;
            notifyCommandListener(sender, this, "command.nyx.force.clear");
        } else {
            Optional<LunarEvent> event = world.lunarEvents.stream().filter(e -> e.name.equals(args[0])).findFirst();
            if (!event.isPresent())
                throw new SyntaxErrorException("command.nyx.force.invalid", args[0]);
            world.forcedEvent = event.get();
            notifyCommandListener(sender, this, "command.nyx.force.success", args[0]);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer
                                                  server, ICommandSender sender, String[] args, @Nullable BlockPos
                                                  targetPos) {
        if (args.length != 1)
            return Collections.emptyList();
        NyxWorld world = NyxWorld.get(sender.getEntityWorld());
        if (world == null)
            return Collections.emptyList();
        List<String> ret = world.lunarEvents.stream().map(e -> e.name).collect(Collectors.toList());
        ret.add("clear");
        return getListOfStringsMatchingLastWord(args, ret);
    }
}
