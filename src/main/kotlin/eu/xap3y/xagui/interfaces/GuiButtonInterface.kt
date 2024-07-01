package eu.xap3y.xagui.interfaces

import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

interface GuiButtonInterface {

    fun getIcon(): Material

    fun getItem(): ItemStack

    fun setItem(item: ItemStack)

    fun setListener(newListener: (InventoryClickEvent) -> Unit): GuiButton

    fun setName(name: String): GuiButton

    var listener: ButtonListener?

}