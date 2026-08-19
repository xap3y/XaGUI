package eu.xap3y.xagui.commands;

import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.XaGuiPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class CmdLib {

    public static boolean processCmd(CommandSender sender, String[] args) {
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
                sender.sendMessage("§7/xagui screens §f- Show opened GUIs");
                sender.sendMessage("§7/xagui close-for <player> §f- Close GUI for player");
                sender.sendMessage("§7/xagui dupe <originPlayer> <targetPlayer> §f- Duplicate menu from origin player to target player");
                sender.sendMessage("§7/xagui disable §f- Disable XaGui");
                sender.sendMessage("§7/xagui enable §f- Enable XaGui");
                break;
            case "ver":
                sender.sendMessage("§aXaGui version: §6" + XaGui.getVERSION());
                break;
            case "screens":
                sendScreens(sender);
                break;
            case "close-for":
                if (!hasPermission(sender, "xagui.command.closefor")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cPlease specify a player! Usage: /xagui close-for <player>");
                    return true;
                }
                Player target = XaGuiPlugin.getXaGui().getPlugin().getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }
                AtomicInteger closedCount = new AtomicInteger();
                XaGuiPlugin.getXaGui().getOpenMenus().forEach((uuid, menu) -> {
                    if (uuid.equals(target.getUniqueId())) {
                        menu.close();
                        closedCount.getAndIncrement();
                    }
                });
                sender.sendMessage("§aClosed " + closedCount.get() + " menu(s) for player §e" + target.getName());
                break;
            case "dupe":
                if (!hasPermission(sender, "xagui.command.dupe")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cPlease specify a player! Usage: /xagui dupe <originPlayer> <targetPlayer>");
                    return true;
                }

                Player origin = XaGuiPlugin.getXaGui().getPlugin().getServer().getPlayer(args[1]);
                Player targetPlayer = XaGuiPlugin.getXaGui().getPlugin().getServer().getPlayer(args[2]);
                if (origin == null || targetPlayer == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                } else if (origin.getUniqueId().equals(targetPlayer.getUniqueId())) {
                    sender.sendMessage("§cYou cannot duplicate a menu to the same player!");
                    return true;
                } else if (!XaGuiPlugin.getXaGui().getOpenMenus().containsKey(origin.getUniqueId())) {
                    sender.sendMessage("§cThe origin player doesn't have an open menu!");
                    return true;
                }

                XaGuiPlugin.getXaGui().getOpenMenus().get(origin.getUniqueId()).open(targetPlayer);
                sender.sendMessage("§aMenu duplicated from §e" + origin.getName() + " §ato §e" + targetPlayer.getName());
                break;
            case "disable":
                if (!hasPermission(sender, "xagui.command.disable")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                if (!XaGui.isEnabled()) {
                    sender.sendMessage("§cXaGui is already disabled!");
                    return true;
                } else if (args.length < 2) {
                    sender.sendMessage("§cPlease verify this action using: §e/xagui disable confirm");
                    return true;
                } else if (!args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage("§cPlease verify this action using: §e/xagui disable confirm");
                    return true;
                }
                XaGui.setEnabled(false);
                sender.sendMessage("§aXaGui has been disabled!");
                break;
            case "enable":
                if (!hasPermission(sender, "xagui.command.enable")) {
                    sendNoPermissionMessage(sender);
                    return true;
                }
                if (XaGui.isEnabled()) {
                    sender.sendMessage("§cXaGui is already enabled!");
                    return true;
                }
                XaGui.setEnabled(true);
                sender.sendMessage("§aXaGui has been enabled!");
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

    private static void sendScreens(CommandSender sender) {
        if (!hasPermission(sender, "xagui.command.screens")) {
            sendNoPermissionMessage(sender);
            return;
        }

        if (XaGuiPlugin.getXaGui().getOpenMenus().isEmpty()) {
            sender.sendMessage("§cThere are no open menus!");
            return;
        }

        sender.sendMessage("§6Open menus:");
        XaGuiPlugin.getXaGui().getOpenMenus().forEach((uuid, menu) -> {
            Player player = XaGuiPlugin.getXaGui().getPlugin().getServer().getPlayer(uuid);
            sender.sendMessage("§e" + (player != null ? player.getName() : uuid.toString()) + " §7§l-> §r" + menu.getRawName());
        });
    }
}
