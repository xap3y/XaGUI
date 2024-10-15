package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryOpenEvent

/**
 * Interface for open event listeners
 *
 * @see GuiInterface
 */
interface GuiOpenInterface {

    fun onOpen(event: InventoryOpenEvent)

}
