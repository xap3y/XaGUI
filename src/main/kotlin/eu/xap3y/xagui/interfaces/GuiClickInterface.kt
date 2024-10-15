package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Interface for click event listeners
 *
 * @see GuiInterface
 */
interface GuiClickInterface {

    /**
     * Called when the GUI is clicked
     * @param event The event of the click
     */
    fun onClick(event: InventoryClickEvent)

}