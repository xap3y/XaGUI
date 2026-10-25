package eu.xap3y.xagui.commands;

import eu.xap3y.xagui.tracking.ScreenTracker;
import org.bukkit.command.CommandSender;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class CmdLibTest {

    private CommandSender sender(List<String> messages, boolean permitted) {
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("hasPermission")) {
                        return permitted;
                    }
                    if (method.getName().equals("isOp")) {
                        return false;
                    }
                    if (method.getName().equals("sendMessage")) {
                        messages.add((String) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    @Test
    public void newCommandsRequirePermission() {
        List<String> messages = new ArrayList<>();
        CommandSender sender = sender(messages, false);
        for (String command : new String[]{"screens-detailed", "screens-history", "screen-toggle"}) {
            CmdLib.processCmd(sender, new String[]{command});
        }
        assertEquals(3, messages.size());
        for (String message : messages) {
            assertTrue(message.contains("permission"));
        }
        assertFalse(CmdLib.suggest(sender, new String[]{"screens-"}).contains("screens-history"));
    }

    @Test
    public void rejectsInvalidHistoryCountsAndArguments() {
        List<String> messages = new ArrayList<>();
        CommandSender sender = sender(messages, true);
        for (String count : new String[]{"0", "-1", "1001", "99999999999999", "abc"}) {
            CmdLib.processCmd(sender, new String[]{"screens-history", count});
            assertTrue(messages.get(messages.size() - 1).contains("Number must"));
        }
        CmdLib.processCmd(sender, new String[]{"screens-history", "10", "Alice", "extra"});
        assertTrue(messages.get(messages.size() - 1).contains("Usage:"));
    }

    @Test
    public void defaultHistoryShowsTenAndPlayerFilterSupportsOfflineNames() {
        List<String> messages = new ArrayList<>();
        CommandSender sender = sender(messages, true);
        ScreenTracker tracker = ScreenTracker.getInstance();
        UUID player = UUID.randomUUID();
        Object menu = new Object();
        String name = "Offline" + player.toString().substring(0, 5);
        tracker.register(menu, CmdLibTest.class);
        for (int index = 0; index < 12; index++) {
            tracker.opened(player, name, menu, "Title " + index);
            tracker.closed(player, menu);
        }
        CmdLib.processCmd(sender, new String[]{"screens-history"});
        assertEquals(11, messages.size());
        assertTrue(messages.get(1).contains("Title 11"));
        messages.clear();
        CmdLib.processCmd(sender, new String[]{"screens-history", "2", name.toLowerCase()});
        assertEquals(3, messages.size());
        assertTrue(messages.get(2).contains("Title 10"));
    }

    @Test
    public void classCompletionAndToggleUseExactDisplayedIdentity() {
        List<String> messages = new ArrayList<>();
        CommandSender sender = sender(messages, true);
        ScreenTracker tracker = ScreenTracker.getInstance();
        Object menu = new Object();
        tracker.register(menu, CmdLibTest.class);
        String identity = ScreenTracker.formatClass(CmdLibTest.class);
        assertTrue(CmdLib.suggest(sender, new String[]{"screen-toggle", "eu.xap3y"}).contains(identity));
        CmdLib.processCmd(sender, new String[]{"screen-toggle", identity});
        assertTrue(tracker.isDisabled(menu));
        CmdLib.processCmd(sender, new String[]{"screen-toggle", identity});
        assertFalse(tracker.isDisabled(menu));
        CmdLib.processCmd(sender, new String[]{"screen-toggle", "missing@Menu.java"});
        assertTrue(messages.get(2).contains("Unknown GUI class"));
    }
}
