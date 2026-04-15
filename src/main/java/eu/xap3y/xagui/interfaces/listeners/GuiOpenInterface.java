package eu.xap3y.xagui.interfaces.listeners;

import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public interface GuiOpenInterface {
    void onOpen(@NotNull InventoryOpenEvent event);
}
