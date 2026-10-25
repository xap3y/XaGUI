package eu.xap3y.xagui.paper;

import eu.xap3y.xagui.XaGuiPlugin;
import eu.xap3y.xagui.commands.CmdLib;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class XaGuiCommandPaper implements BasicCommand {

    @Override
    public void execute(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
        CmdLib.processCmd(commandSourceStack.getSender(), args);
    }

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String @NonNull[] args) {

        return CmdLib.suggest(commandSourceStack.getSender(), args);
    }

    public static void injectPaper() {
        XaGuiPlugin.getXaGui().getPlugin().registerCommand("xagui", "XaGui control plugin", new XaGuiCommandPaper());
    }
}
