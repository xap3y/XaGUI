package eu.xap3y.xagui.interfaces.listeners;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public interface GuiCloseInterface {
    void onClose(@NotNull InventoryCloseEvent event);
}
