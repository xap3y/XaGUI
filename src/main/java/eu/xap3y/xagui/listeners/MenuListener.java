package eu.xap3y.xagui.listeners;

import eu.xap3y.xagui.GuiMenu;
import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.events.GuiClickEvent;
import eu.xap3y.xagui.interfaces.GuiButtonInterface;
import lombok.AllArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
public class MenuListener implements Listener {

    private final JavaPlugin plugin;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof GuiMenu clickedInventory)) {
            if (e.getView().getTopInventory().getHolder() instanceof GuiMenu clickedInventory) {
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
            player.playSound(player, button.getClickSound() != null ? button.getClickSound() : Sound.UI_BUTTON_CLICK, button.getClickSoundVolume(), 1f);
        } else {
            if (XaGui.getClickSound() != null) {
                Player player = (Player) e.getWhoClicked();
                player.playSound(player, XaGui.getClickSound(), 1f, 1f);
            }
        }

        if (button.getClickListener() != null) {
            button.getClickListener().onClick(new GuiClickEvent(e));
        }

        button.callRedirect((Player) e.getWhoClicked());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiMenu clickedInventory)) return;

        JavaPlugin owner = clickedInventory.getOwner();
        if (!Objects.equals(owner, plugin)) return;

        XaGui.removeOpenMenu(e.getPlayer().getUniqueId());

        if (clickedInventory.onCloseAction != null) {
            clickedInventory.onCloseAction.onClose(e);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiMenu clickedInventory)) return;

        JavaPlugin owner = clickedInventory.getOwner();
        if (!Objects.equals(owner, plugin)) return;

        XaGui.addOpenMenu(e.getPlayer().getUniqueId(), clickedInventory);

        if (clickedInventory.onOpenAction != null) {
            clickedInventory.onOpenAction.onOpen(e);
        }
    }
}
