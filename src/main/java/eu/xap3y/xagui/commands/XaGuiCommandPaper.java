package eu.xap3y.xagui.commands;

import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.XaGuiPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;

public class XaGuiCommandPaper implements BasicCommand {

    @Override
    public void execute(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
        CmdLib.processCmd(commandSourceStack.getSender(), args);
    }

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String @NonNull[] args) {

        if (args.length < 1) {
            return java.util.List.of("help", "closeall", "ver", "screens", "close-for", "dupe", "disable", "enable");
        } else if (args.length == 1) {
            String subcommand = args[0].toLowerCase();
            return switch (subcommand) {
                case "close-for", "dupe" ->
                        commandSourceStack.getSender().getServer().getOnlinePlayers()
                                .stream()
                                .map(player -> subcommand + " " + player.getName())
                                .toList();
                default -> Collections.emptyList();
            };
        } else {
            return Collections.emptyList();
        }
    }
}
