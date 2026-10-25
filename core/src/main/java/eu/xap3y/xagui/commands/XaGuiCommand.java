package eu.xap3y.xagui.commands;


import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class XaGuiCommand implements TabExecutor {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return CmdLib.suggest(sender, args);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        return CmdLib.processCmd(sender, args);
    }
}
