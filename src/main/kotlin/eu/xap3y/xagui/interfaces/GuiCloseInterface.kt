package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Interface for close event listeners
 * @see GuiInterface
 */
interface GuiCloseInterface {

    fun onClose(event: InventoryCloseEvent)
}