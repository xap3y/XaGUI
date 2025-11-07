package eu.xap3y.xagui.interfaces.listeners;

import eu.xap3y.xagui.models.GuiPageSwitchModel;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface GuiPageSwitchInterface {
    void onPageSwitch(@NotNull GuiPageSwitchModel event);
}
