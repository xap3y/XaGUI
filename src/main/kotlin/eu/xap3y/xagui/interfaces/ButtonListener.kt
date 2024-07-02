package eu.xap3y.xagui.interfaces

import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Interface for button listeners
 * @see GuiButtonInterface
 */
interface ButtonListener {

    /**
     * Called when the button is clicked
     * @param event The event of the click
     */
    fun onClick(event: InventoryClickEvent)
}