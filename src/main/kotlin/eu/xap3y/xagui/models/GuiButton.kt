package eu.xap3y.xagui.models

import eu.xap3y.xagui.interfaces.ButtonListener
import eu.xap3y.xagui.interfaces.GuiButtonInterface
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Represents a button in a GUI
 */
class GuiButton(private val item: ItemStack): GuiButtonInterface {

    /**
     * The icon of the button
     * @return The material of the button
     */
    override fun getIcon(): Material = icon.type

    /**
     * The item of the button
     * @return The itemstack of the button
     */
    override fun getItem(): ItemStack = icon

    /**
     * Sets the item of the button
     * @param item The new itemstack
     */
    override fun setItem(item: ItemStack) {
        icon = item
    }

    /**
     * Sets the listener of the button
     * @param newListener The new listener
     * @return The button
     */
    override fun setListener(newListener: (InventoryClickEvent) -> Unit): GuiButton {
        this.listener = object : ButtonListener {
            override fun onClick(event: InventoryClickEvent) {
                newListener(event)
            }
        }
        return this
    }

    /**
     * Sets the name of the button
     * @param name The new name
     * @return The button
     */
    override fun setName(name: String): GuiButton {
        icon.itemMeta?.setDisplayName(ChatColor.translateAlternateColorCodes('&', name))
        return this
    }

    //override fun getListener(): ButtonListener? = listener

    private var icon: ItemStack = item

    override var listener: ButtonListener? = null


}