package eu.xap3y.xagui.interfaces

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

interface GuiInterface {

    fun onOpen(onOpen: () -> Unit)

    fun onClose(onClose: () -> Unit)

    fun setName(newName: String)

    fun getName(): String

    fun getRawName(): String

    fun getSize(): Int

    fun setSlot(slot: Int, button: GuiButtonInterface)

    fun updateSlot(slot: Int, item: ItemStack)

    fun getSlot(slot: Int): GuiButtonInterface?

    fun clearSlot(slot: Int)

    fun clearAllSlots()

    fun refresh()

    fun getOwner(): JavaPlugin

    fun unlockButton(slot: Int)

    fun lockButton(slot: Int)

    fun open(player: Player)

}