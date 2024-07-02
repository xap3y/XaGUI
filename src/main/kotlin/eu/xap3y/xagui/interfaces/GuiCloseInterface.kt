package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Interface for close event listeners
 * @see GuiInterface
 */
interface GuiCloseInterface {

    /**
     * Called when the GUI is closed
     * @param event The event of the close
     */
    fun onClose(event: InventoryCloseEvent)
}