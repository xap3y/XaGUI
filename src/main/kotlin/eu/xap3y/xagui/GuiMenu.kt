@file:Suppress("DEPRECATION")

package eu.xap3y.xagui

import eu.xap3y.xagui.interfaces.GuiButtonInterface
import eu.xap3y.xagui.interfaces.GuiCloseInterface
import eu.xap3y.xagui.interfaces.GuiInterface
import eu.xap3y.xagui.interfaces.GuiOpenInterface
import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a GUI menu
 * @param plugin The plugin that owns the menu
 * @param title The title of the menu
 * @param rowsToSet The number of rows the menu should have
 */
class GuiMenu(private val plugin: JavaPlugin, private val title: String, private val rowsToSet: Int): InventoryHolder, GuiInterface {

    private val slots: ConcurrentHashMap<Int, GuiButtonInterface> = ConcurrentHashMap()
    val unlockedSlots = mutableSetOf<Int>()

    /**
     * The inventory of the menu
     * @return The inventory
     */
    override fun getInventory(): Inventory {
        return inv
    }

    /**
     * The action to be executed when the menu is opened
     * @param openAction The action to be executed
     */
    override fun setOnOpen(openAction: (InventoryOpenEvent) -> Unit) {
        this.onOpenAction = object : GuiOpenInterface {
            override fun onOpen(event: InventoryOpenEvent) {
                openAction(event)
            }
        }
    }

    /**
     * The action to be executed when the menu is closed
     * @param closeAction The action to be executed
     */
    override fun setOnClose(closeAction: (InventoryCloseEvent) -> Unit) {
        this.onCloseAction = object : GuiCloseInterface {
            override fun onClose(event: InventoryCloseEvent) {
                closeAction(event)
            }
        }
    }

    /**
     * Set the name of the menu
     * @param newName The new name of the menu
     */
    override fun setName(newName: String) {
        name = newName
    }

    /**
     * Get the name of the menu
     * @return The name of the menu
     */
    override fun getName(): String =
        ChatColor.translateAlternateColorCodes('&', name)

    /**
     * Get the uncolored name of the menu
     * @return The uncolored name of the menu
     */
    override fun getRawName(): String = name

    /**
     * Get the size of the menu
     * @return The size of the menu
     */
    override fun getSize(): Int = rows * 9

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param button The button to set
     */
    override fun setSlot(slot: Int, button: GuiButtonInterface) {
        slots[slot] = button
        inv.setItem(slot, button.getItem())
    }

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param item The item to set
     */
    fun setSlot(slot: Int, item: ItemStack) {
        setSlot(slot, GuiButton(item))
    }

    /**
     * Update the item in a slot
     * @param slot The slot to update
     * @param item The item to update to
     */
    override fun updateSlot(slot: Int, item: ItemStack) {
        val old = slots[slot] ?: return
        old.setItem(item)
        setSlot(slot, old)
    }

    /**
     * Get the button in a slot
     * @param slot The slot to get the button from
     * @return The button in the slot
     */
    override fun getSlot(slot: Int): GuiButtonInterface? =
        slots[slot]

    /**
     * Clear the button in a slot
     * @param slot The slot to clear
     */
    override fun clearSlot(slot: Int) {
        slots.remove(slot)
        inv.setItem(slot, null)
    }

    /**
     * Clear all buttons in the menu
     */
    override fun clearAllSlots() {
        inv.clear()
    }

    /**
     * Get the plugin that owns the menu
     * @return The plugin that owns the menu
     */
    override fun getOwner(): JavaPlugin = plugin

    /**
     * Unlock a slot so the player can take items from it
     * @param slot The slot to unlock
     */
    override fun unlockButton(slot: Int) {
        unlockedSlots.add(slot)
    }

    /**
     * Lock a slot
     * @param slot The slot to lock
     */
    override fun lockButton(slot: Int) {
        unlockedSlots.remove(slot)
    }

    /**
     * Open the menu for a player
     * @param player The player to open the menu for
     */
    override fun open(player: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.openInventory(inv)
        })
    }

    /**
     * Close the menu for a player
     * @param player The player to close the menu for
     */
    override fun close(player: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.closeInventory()
        })
    }

    /**
     * Fill a set of slots with an item
     * @param slots The slots to fill
     * @param item The item to fill the slots with
     */
    fun fillSlots(slots: Set<Int>, item: ItemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }}) {
        slots.forEach {
            setSlot(it, GuiButton(item))
        }
    }

    /**
     * Fill the border of the menu with an item
     * @param slots The set of slots to fill
     * @param item The gui button to fill the slots with
     */
    fun fillSlots(slots: Set<Int>, item: GuiButtonInterface = GuiButton(ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }})) {
        slots.forEach {
            setSlot(it, item)
        }
    }

    /**
     * Fill the border of the menu with an item
     * @param rows The number of rows in the menu
     * @param item The item to fill the border with
     */
    fun fillBorder(rows: Int, item: ItemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }}) {
        val slots = mutableSetOf<Int>()
        for (i in 0 until rows * 9) {
            if (i < 9 || i >= (rows - 1) * 9 || i % 9 == 0 || i % 9 == 8) {
                slots.add(i)
            }
        }
        fillSlots(slots, item)
    }

    private var rows: Int = rowsToSet

    var onCloseAction: GuiCloseInterface? = null

    var onOpenAction: GuiOpenInterface? = null

    private var name: String = title

    private val inv: Inventory = Bukkit.createInventory(this, getSize(), getName())
}