package eu.xap3y.xagui.commands;

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
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {

        if (args.length < 1) {
            return java.util.List.of("help", "closeall", "ver");
        } else {
            return Collections.emptyList();
        }
    }
}
