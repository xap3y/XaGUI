package eu.xap3y.xagui.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class GuiClickEvent extends InventoryClickEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public GuiClickEvent(InventoryView view, InventoryType.SlotType slotType, int slot, ClickType click, InventoryAction action) {
        super(view, slotType, slot, click, action);
    }

    public GuiClickEvent(InventoryView view, InventoryType.SlotType slotType, int slot, ClickType click, InventoryAction action, int hotbarButton) {
        super(view, slotType, slot, click, action, hotbarButton);
    }

    public GuiClickEvent(InventoryClickEvent e) {
        super(e.getView(), e.getSlotType(), e.getSlot(), e.getClick(), e.getAction(), e.getHotbarButton());
    }

    /**
     * Convenience accessor casting getWhoClicked() to Player.
     * This will throw ClassCastException if the clicker is not a Player.
     */
    public Player getPlayer() {
        return (Player) getWhoClicked();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}