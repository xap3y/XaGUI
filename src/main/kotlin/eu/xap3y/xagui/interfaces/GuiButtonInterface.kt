package eu.xap3y.xagui.interfaces

import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Interface for buttons in a GUI
 * @see ButtonListener
 */
interface GuiButtonInterface {

    fun getIcon(): Material

    fun getItem(): ItemStack

    fun setItem(item: ItemStack)

    fun withListener(newListener: (InventoryClickEvent) -> Unit): GuiButton

    fun setName(name: String): GuiButton

    fun setLore(newLore: List<String>): GuiButton

    fun addItemFlag(flag: ItemFlag): GuiButton

    fun removeItemFlag(flag: ItemFlag): GuiButton

    fun setAmount(amount: Int): GuiButton

    fun addEnchantment(enchantment: Enchantment): GuiButton

    fun addEnchantment(enchantment: Enchantment, level: Int): GuiButton

    fun removeEnchantment(enchantment: Enchantment): GuiButton

    fun removeAllEnchantments(): GuiButton

    fun setDurability(durability: Short): GuiButton

    var listener: ButtonListener?

}