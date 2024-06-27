package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryCloseEvent

interface GuiCloseInterface {

    fun onClose(event: InventoryCloseEvent)
}