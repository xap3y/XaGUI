package eu.xap3y.xagui.commands;

import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.XaGuiPlugin;
import eu.xap3y.xagui.tracking.ScreenTracker;
import eu.xap3y.xagui.tracking.ScreenTracker.ScreenEntry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        String subcommand = args[0].toLowerCase(Locale.ROOT);

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
                sender.sendMessage("§7/xagui screens-detailed §f- Show GUI classes and open duration");
                sender.sendMessage("§7/xagui screens-history [number] [player] §f- Show recent GUI openings");
                sender.sendMessage("§7/xagui screen-toggle <class> §f- Toggle opening a GUI class");
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
            case "screens-detailed":
                sendDetailedScreens(sender);
                break;
            case "screens-history":
                sendHistory(sender, args);
                break;
            case "screen-toggle":
                toggleScreen(sender, args);
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

    private static void sendDetailedScreens(CommandSender sender) {
        if (!hasPermission(sender, "xagui.command.screens-detailed")) {
            sendNoPermissionMessage(sender);
            return;
        }
        List<ScreenEntry> entries = ScreenTracker.getInstance().getActive();
        if (entries.isEmpty()) {
            sender.sendMessage("§cThere are no open menus!");
            return;
        }
        sender.sendMessage("§6Open menus (" + entries.size() + "):");
        for (ScreenEntry entry : entries) {
            sender.sendMessage("§e" + entry.getPlayerName() + " §7-> §r" + entry.getTitle()
                    + " §7| §f" + entry.getClassName()
                    + " §7| §e" + entry.getDurationSeconds() + "s");
        }
    }

    private static void sendHistory(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "xagui.command.screens-history")) {
            sendNoPermissionMessage(sender);
            return;
        }
        if (args.length > 3) {
            sender.sendMessage("§cUsage: /xagui screens-history [number] [player]");
            return;
        }
        int count = 10;
        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage("§cNumber must be a positive integer (maximum "
                        + ScreenTracker.HISTORY_LIMIT + ").");
                return;
            }
            if (count < 1 || count > ScreenTracker.HISTORY_LIMIT) {
                sender.sendMessage("§cNumber must be between 1 and " + ScreenTracker.HISTORY_LIMIT + ".");
                return;
            }
        }
        String player = args.length == 3 ? args[2] : null;
        List<ScreenEntry> entries = ScreenTracker.getInstance().getHistory(count, player);
        if (entries.isEmpty()) {
            sender.sendMessage("§cNo GUI history found" + (player != null ? " for " + player : "") + ".");
            return;
        }
        sender.sendMessage("§6Recent GUI openings (" + entries.size() + ")"
                + (player != null ? " for " + player : "") + ":");
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (ScreenEntry entry : entries) {
            sender.sendMessage("§7" + timestamp.format(new Date(entry.getOpenedAt()))
                    + " §e" + entry.getPlayerName() + " §7-> §r" + entry.getTitle()
                    + " §7| §f" + entry.getClassName()
                    + " §7| §e" + entry.getDurationSeconds() + "s"
                    + (entry.isOpen() ? " §a(open)" : " §7(closed)"));
        }
    }

    private static void toggleScreen(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "xagui.command.screen-toggle")) {
            sendNoPermissionMessage(sender);
            return;
        }
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /xagui screen-toggle <package@MenuClass.java>");
            return;
        }
        ScreenTracker tracker = ScreenTracker.getInstance();
        if (!tracker.getKnownClasses().contains(args[1])) {
            sender.sendMessage("§cUnknown GUI class. Use screens-detailed, screens-history or tab completion.");
            return;
        }
        boolean enabled = tracker.toggle(args[1]);
        sender.sendMessage((enabled ? "§aEnabled" : "§cDisabled")
                + " opening §f" + args[1] + "§7. Existing viewers are unaffected.");
    }

    public static List<String> suggest(CommandSender sender, String[] args) {
        List<String> candidates = new ArrayList<>();
        if (args.length <= 1) {
            for (String command : Arrays.asList(
                    "help", "closeall", "ver", "screens", "screens-detailed",
                    "screens-history", "screen-toggle", "close-for", "dupe", "disable", "enable"
            )) {
                String permission = command.equals("close-for") ? "closefor" : command;
                if (command.equals("ver") || hasPermission(sender, "xagui.command." + permission)) {
                    candidates.add(command);
                }
            }
        } else {
            String command = args[0].toLowerCase(Locale.ROOT);
            String permission = command.equals("close-for") ? "closefor" : command;
            if (!hasPermission(sender, "xagui.command." + permission)) {
                return candidates;
            }
            if (command.equals("screen-toggle") && args.length == 2) {
                candidates.addAll(ScreenTracker.getInstance().getKnownClasses());
            } else if (command.equals("screens-history") && args.length == 2) {
                candidates.addAll(Arrays.asList("10", "25", "50", "100"));
            } else if ((command.equals("close-for") && args.length == 2)
                    || (command.equals("dupe") && (args.length == 2 || args.length == 3))
                    || (command.equals("screens-history") && args.length == 3)) {
                for (Player player : sender.getServer().getOnlinePlayers()) {
                    candidates.add(player.getName());
                }
                if (command.equals("screens-history")) {
                    for (ScreenEntry entry : ScreenTracker.getInstance().getHistory(ScreenTracker.HISTORY_LIMIT, null)) {
                        if (!candidates.contains(entry.getPlayerName())) {
                            candidates.add(entry.getPlayerName());
                        }
                    }
                }
            } else if (command.equals("disable") && args.length == 2) {
                candidates.add("confirm");
            }
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        candidates.removeIf(value -> !value.toLowerCase(Locale.ROOT).startsWith(prefix));
        return candidates;
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
