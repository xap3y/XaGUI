package eu.xap3y.xagui.paper;

import eu.xap3y.xagui.XaGuiPlugin;
import eu.xap3y.xagui.commands.CmdLib;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
            return java.util.Arrays.asList("help", "closeall", "ver", "screens", "close-for", "dupe", "disable", "enable");
        } else if (args.length == 1) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "close-for":
                case "dupe":
                    return commandSourceStack.getSender().getServer().getOnlinePlayers()
                            .stream()
                            .map(player -> subcommand + " " + player.getName())
                            .collect(java.util.stream.Collectors.toList());
                default:
                    return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public static void injectPaper() {
        XaGuiPlugin.getXaGui().getPlugin().registerCommand("xagui", "XaGui control plugin", new XaGuiCommandPaper());
    }
}
