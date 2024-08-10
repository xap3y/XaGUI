package eu.xap3y.xagui.models

import eu.xap3y.xagui.GuiMenu
import eu.xap3y.xagui.interfaces.ButtonListener
import eu.xap3y.xagui.interfaces.GuiButtonInterface
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Represents a button in a GUI
 *
 * It can be clickable and have a listener
 * You can set the item, name, lore, amount, enchantments, durability, item flags
 *
 * @see GuiButtonInterface
 * @see ButtonListener
 * @param item The ItemStack of the button
 *
 */
class GuiButton(private val item: ItemStack): GuiButtonInterface {

    constructor(material: Material) : this(ItemStack(material))

    /**
     * The icon of the button
     *
     * @return The material of the button
     */
    override fun getIcon(): Material = icon.type

    /**
     * The item of the button
     *
     * @return The ItemStack of the button
     */
    override fun getItem(): ItemStack = icon

    /**
     * Sets the item of the button
     *
     * @param item The new itemstack
     */
    override fun setItem(item: ItemStack) {
        icon = item
    }

    /**
     * Sets the listener of the button
     *
     * Use this if you are writing in Kotlin
     *
     * @param newListener The new listener
     * @return The button with the new listener
     */
    override fun withListener(newListener: (InventoryClickEvent) -> Unit): GuiButton {
        this.listener = object : ButtonListener {
            override fun onClick(event: InventoryClickEvent) {
                newListener(event)
            }
        }
        return this
    }

    /**
     * Sets the listener of the button
     *
     * Use this if you are writing in Java so no return statement is needed
     *
     * @param newListener The new listener
     * @return The button with the new listener
     */
    override fun withListener(newListener: ButtonListener): GuiButton {
        this.listener = newListener
        return this
    }

    /**
     * Sets the name of the button
     *
     * @param name The new name
     * @return The button with the new name
     */
    override fun setName(name: String): GuiButton {
        icon.apply {
            itemMeta = itemMeta.apply {
                setDisplayName(ChatColor.translateAlternateColorCodes('&', name))
            }
        }
        return this
    }

    /**
     * Sets the lore of the button
     *
     * @param newLore The new lore
     * @return The button with the new lore
     */
    override fun setLoreList(newLore: List<String>): GuiButton {
        val newLore2: List<String> = newLore.map { ChatColor.translateAlternateColorCodes('&', it) }
        icon.apply {
            itemMeta = itemMeta.apply {
                lore = newLore2
            }
        }
        return this
    }

    /**
     * Sets the lore of the button
     *
     * @param args The new lore
     * @return The button with the new lore
     */
    override fun setLore(vararg args: String): GuiButton {
        return setLoreList(args.toList())
    }

    /**
     * Sets the lore of the button
     *
     * @param array The new lore
     * @return The button with the new lore
     */
    override fun setLoreArray(array: Array<String>): GuiButton {
        return setLoreList(array.toList())
    }

    /**
     * Adds a line to the lore of the button
     *
     * @param line The line to add
     * @return The button with the line added
     */
    override fun addLoreLine(line: String): GuiButton {
        icon.apply {
            itemMeta = itemMeta.apply {
                lore = lore?.plus(ChatColor.translateAlternateColorCodes('&', line))
            }
        }
        return this
    }

    /**
     * Adds lore to the button
     *
     * @param lines The lore to add
     * @return The button with the lore added
     */
    override fun addLoreList(lines: List<String>): GuiButton {
        val lines2: List<String> = lines.map { ChatColor.translateAlternateColorCodes('&', it) }
        icon.apply {
            itemMeta = itemMeta.apply {
                lore = lore?.plus(lines2)
            }
        }
        return this
    }

    /**
     * Adds lore to the button
     *
     * @param args The lore to add
     * @return The button with the lore added
     */
    override fun addLore(vararg args: String): GuiButton {
        return addLoreList(args.toList())
    }

    /**
     * Adds lore to the button
     *
     * @param array The lore to add
     * @return The button with the lore added
     */
    override fun addLoreArray(array: Array<String>): GuiButton {
        return addLoreList(array.toList())
    }

    /**
     * Clears the lore of the button
     *
     * @return The button with the lore cleared
     */
    override fun clearLore(): GuiButton {
        icon.apply {
            itemMeta = itemMeta.apply {
                lore = null
            }
        }
        return this
    }

    /**
     * Sets the amount of the button
     *
     * @param amount The new amount
     * @return The button with the new amount
     */
    override fun setAmount(amount: Int): GuiButton {
        icon.amount = amount
        return this
    }

    /**
     * Adds an enchantment to the button
     *
     * @param enchantment The enchantment to add
     * @return The button with the enchantment added
     */
    override fun addEnchantment(enchantment: Enchantment): GuiButton {
        icon.addUnsafeEnchantment(enchantment, 1)
        return this
    }

    /**
     * Adds an enchantment to the button
     *
     * @param enchantment The enchantment to add
     * @param level The level of the enchantment
     * @return The button with the enchantment added
     */
    override fun addEnchantment(enchantment: Enchantment, level: Int): GuiButton {
        icon = icon.apply {
            itemMeta = itemMeta.apply {
                addUnsafeEnchantment(enchantment, level)
            }
        }
        return this
    }

    /**
     * Removes an enchantment from the button
     *
     * @param enchantment The enchantment to remove
     * @return The button with the enchantment removed
     */
    override fun removeEnchantment(enchantment: Enchantment): GuiButton {
        icon.removeEnchantment(enchantment)
        return this
    }

    /**
     * Removes all enchantments from the button
     *
     * @return The button with all enchantments removed
     */
    override fun removeAllEnchantments(): GuiButton {
        icon = icon.apply {
            itemMeta = itemMeta.apply {
                enchants.clear()
            }
        }
        return this
    }

    /**
     * Sets the durability of the button
     *
     * @param durability The new durability
     * @return The button with the new durability
     */
    override fun setDurability(durability: Short): GuiButton {
        icon.durability = durability
        return this
    }

    /**
     * Adds an item flag to the button
     *
     * @param flag The flag to add
     * @return The button with the flag added
     */
    override fun addItemFlag(flag: ItemFlag): GuiButton {
        icon = icon.apply {
            itemMeta = itemMeta.apply {
                addItemFlags(flag)
            }
        }
        return this
    }

    /**
     * Removes an item flag from the button
     *
     * @param flag The flag to remove
     * @return The button with the flag removed
     */
    override fun removeItemFlag(flag: ItemFlag): GuiButton {
        icon = icon.apply {
            itemMeta = itemMeta.apply {
                removeItemFlags(flag)
            }
        }
        return this
    }

    /**
     * Clone a button class
     *
     * @return The cloned button
     */
    override fun clone(): GuiButton {
        val new = GuiButton(this.item.clone())
        new.listener = this.listener
        new.redirectMenu = this.redirectMenu
        return new
    }

    /**
     * Redirect player to another menu
     *
     * @param menu The menu to redirect to after clicking
     * @return The button with the new redirect
     */
    override fun withRedirect(menu: () -> GuiMenu): GuiButton {
        redirectMenu = menu
        return this
    }

    /**
     * Redirect player to another menu
     *
     * @param p The player that will be redirected
     * @return The cloned button
     */
    override fun callRedirect(p: Player) {
        redirectMenu?.invoke()?.open(p)
    }


    //override fun getListener(): ButtonListener? = listener

    private var icon: ItemStack = item

    private var listener: ButtonListener? = null

    private var redirectMenu: (() -> GuiMenu)? = null

    override fun getClickListener(): ButtonListener? {
        return listener
    }


}