package eu.xap3y.xagui.listeners;

import eu.xap3y.xagui.GuiMenu;
import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.tracking.ScreenTracker;
import eu.xap3y.xagui.events.GuiClickEvent;
import eu.xap3y.xagui.interfaces.GuiButtonInterface;
import lombok.AllArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
public class MenuListener implements Listener {

    private final JavaPlugin plugin;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        try {
            Inventory inventory = getClickedInventory(e);
            if (inventory == null || !(inventory.getHolder() instanceof GuiMenu)) {
                if (getTopInventory(e).getHolder() instanceof GuiMenu) {
                    GuiMenu clickedInventory = (GuiMenu) getTopInventory(e).getHolder();
                    if (!clickedInventory.getSelfInventoryAccess()) {
                        e.setResult(Event.Result.DENY);
                    } else {
                        ClickType[] allowedSelf = clickedInventory.getAllowedSelfInventoryClickTypes();
                        if (allowedSelf.length > 0 && Arrays.asList(allowedSelf).contains(e.getClick())) {
                            e.setResult(Event.Result.ALLOW);
                        } else {
                            e.setResult(Event.Result.DENY);
                        }
                    }
                    if (clickedInventory.onClickActionOwn != null) {
                        clickedInventory.onClickActionOwn.onClick(new GuiClickEvent(e));
                    }
                }
                return;
            }

            GuiMenu clickedInventory = (GuiMenu) inventory.getHolder();
            JavaPlugin owner = clickedInventory.getOwner();
            if (!Objects.equals(owner, plugin)) return;

            ClickType[] allowed = clickedInventory.getAllowedClickTypes();
            if (allowed.length > 0 && !Arrays.asList(allowed).contains(e.getClick())) {
                e.setResult(Event.Result.DENY);
                return;
            } else {
                ClickType[] blacklisted = clickedInventory.getBlacklistedClickTypes();
                if (blacklisted.length > 0 && Arrays.asList(blacklisted).contains(e.getClick())) {
                    e.setResult(Event.Result.DENY);
                    return;
                }
            }

            Set<Integer> pageUnlocked = clickedInventory.unlockedSlots.get(clickedInventory.getCurrentPageIndex());
            boolean allowClick = pageUnlocked != null && pageUnlocked.contains(e.getSlot());

            if (!allowClick) {
                e.setResult(Event.Result.DENY);
            } else {
                e.setResult(Event.Result.ALLOW);
            }

            if (clickedInventory.onClickAction != null) {
                clickedInventory.onClickAction.onClick(new GuiClickEvent(e));
            }

            GuiButtonInterface button = clickedInventory.getSlot(e.getSlot());

            if (button == null) return;

            if (button.getClickSound() != null) {
                Player player = (Player) e.getWhoClicked();
                try {
                    player.playSound(player.getLocation(), button.getClickSound(), button.getClickSoundVolume(), 1f);
                } catch (Exception ignored) {}
            } else {
                if (XaGui.getClickSound() != null) {
                    Player player = (Player) e.getWhoClicked();
                    try {
                        player.playSound(player.getLocation(), XaGui.getClickSound(), 1f, 1f);
                    } catch (Exception ignored) {}
                }
            }

            if (button.getClickListener() != null) {
                button.getClickListener().onClick(new GuiClickEvent(e));
            }

            button.callRedirect((Player) e.getWhoClicked());
        } catch (Exception ignored) {
            // IGNORE
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiMenu)) return;
        GuiMenu clickedInventory = (GuiMenu) e.getInventory().getHolder();

        JavaPlugin owner = clickedInventory.getOwner();
        if (!Objects.equals(owner, plugin)) return;

        XaGui.removeOpenMenu(e.getPlayer().getUniqueId());
        ScreenTracker.getInstance().closed(e.getPlayer().getUniqueId(), clickedInventory);

        if (XaGui.getOnCloseAction() != null) {
            XaGui.getOnCloseAction().onClose(e);
        }

        if (clickedInventory.onCloseAction != null) {
            clickedInventory.onCloseAction.onClose(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiMenu)) return;
        GuiMenu clickedInventory = (GuiMenu) e.getInventory().getHolder();

        JavaPlugin owner = clickedInventory.getOwner();
        if (!Objects.equals(owner, plugin)) return;

        if (!XaGui.isEnabled() || ScreenTracker.getInstance().isDisabled(clickedInventory)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cOpening this GUI is currently disabled.");
            plugin.getServer().getConsoleSender().sendMessage(
                    "§a[§bXaGui§a] §cBlocked " + e.getPlayer().getName()
                            + " from opening " + ScreenTracker.getInstance().getIdentity(clickedInventory)
            );
            return;
        }

        if (clickedInventory.getOpenSound() != null) {
            Player player = (Player) e.getPlayer();
            try {
                player.playSound(player.getLocation(), clickedInventory.getOpenSound(), clickedInventory.getOpenSoundVolume(), 1f);
            } catch (Exception ignored) {
            }
        }

        if (XaGui.getOnOpenAction() != null) {
            XaGui.getOnOpenAction().onOpen(e);
        }

        if (clickedInventory.onOpenAction != null) {
            clickedInventory.onOpenAction.onOpen(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void trackInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiMenu)) {
            return;
        }
        GuiMenu menu = (GuiMenu) event.getInventory().getHolder();
        if (!Objects.equals(menu.getOwner(), plugin)) {
            return;
        }
        XaGui.addOpenMenu(event.getPlayer().getUniqueId(), menu);
        ScreenTracker.getInstance().opened(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                menu,
                menu.getRawName()
        );
    }

    /**
     * Resolves the clicked inventory without the API method missing on old Bukkit.
     */
    public static Inventory getClickedInventory(InventoryClickEvent event) {
        if (event.getRawSlot() < 0) {
            return null;
        }

        Inventory topInventory = getTopInventory(event);
        if (event.getRawSlot() < topInventory.getSize()) {
            return topInventory;
        }

        try {
            Object view = event.getView();
            Method getBottomInventory = view.getClass().getMethod("getBottomInventory");
            getBottomInventory.setAccessible(true);
            return (Inventory) getBottomInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Inventory getTopInventory(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
