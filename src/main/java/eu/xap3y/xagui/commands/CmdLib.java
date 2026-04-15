package eu.xap3y.xagui.commands;

import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.XaGuiPlugin;
import org.bukkit.command.CommandSender;

class CmdLib {

    static boolean processCmd(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!hasPermission(sender, "xagui.command.help")) {
                sendNoPermissionMessage(sender);
                return true;
            }
            sender.sendMessage("§cNo subcommand provided! Use /xagui help");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "closeall":
                if (!hasPermission(sender, "xagui.command.closeall")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                XaGuiPlugin.getXaGui().closeAll();
                sender.sendMessage("§aAll open menus have been closed!");
                break;
            case "help":
                if (!hasPermission(sender, "xagui.command.help")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                sender.sendMessage("§6Available commands:");
                sender.sendMessage("§7/xagui closeall §f- Close all open menus");
                sender.sendMessage("§7/xagui ver §f- Show version");
                sender.sendMessage("§7/xagui help §f- Show this help");
                break;
            case "ver":
                sender.sendMessage("§aXaGui version: §6" + XaGui.getVERSION());
                break;
            default:
                if (!hasPermission(sender, "xagui.command.help")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                sender.sendMessage("§cUnknown subcommand! Use §e/xagui help");
                return false;
        }

        return true;
    }

    private static boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.hasPermission("xagui.command.*") || sender.isOp();
    }

    private static void sendNoPermissionMessage(CommandSender sender) {
        sender.sendMessage("§cYou don't have permission to use this command!");
    }
}
