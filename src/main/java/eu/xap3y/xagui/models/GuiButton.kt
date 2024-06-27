package eu.xap3y.xagui.models

import eu.xap3y.xagui.interfaces.ButtonListener
import eu.xap3y.xagui.interfaces.GuiButtonInterface
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class GuiButton(private val item: ItemStack): GuiButtonInterface {

    override fun getIcon(): Material = icon.type

    override fun getItem(): ItemStack = icon

    override fun setItem(item: ItemStack) {
        icon = item
    }

    override fun setListener(newListener: (InventoryClickEvent) -> Unit): GuiButton {
        this.listener = object : ButtonListener {
            override fun onClick(event: InventoryClickEvent) {
                newListener(event)
            }
        }
        return this
    }

    //override fun getListener(): ButtonListener? = listener

    private var icon: ItemStack = item

    override var listener: ButtonListener? = null


}