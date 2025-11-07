package eu.xap3y.xagui.interfaces.listeners;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface GuiClickInterface {
    void onClick(@NotNull InventoryClickEvent event);
}
