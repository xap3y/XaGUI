package eu.xap3y.xagui.interfaces

import eu.xap3y.xagui.exceptions.RowsOutOfBoundException
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/**
 * Interface for GUIs
 */
interface GuiInterface {

    fun setOnOpen(openAction: (InventoryOpenEvent) -> Unit)

    fun setOnClose(closeAction: (InventoryCloseEvent) -> Unit)

    fun setOnClick(onClick: (InventoryClickEvent) -> Unit)

    fun setName(newName: String)

    fun getName(): String

    fun getRawName(): String

    fun getSize(): Int

    fun setSlot(slot: Int, button: GuiButtonInterface)

    fun updateSlot(slot: Int, item: ItemStack)

    fun getSlot(slot: Int): GuiButtonInterface?

    fun clearSlot(slot: Int)

    fun clearAllSlots()

    //fun refresh()

    fun getOwner(): JavaPlugin

    fun unlockButton(slot: Int)

    fun lockButton(slot: Int)

    fun open(player: Player)

    fun close(player: Player)

}