@file:Suppress("DEPRECATION")

package eu.xap3y.xagui

import eu.xap3y.xagui.exceptions.RowsOutOfBoundException
import eu.xap3y.xagui.exceptions.SlotOutOfBoundException
import eu.xap3y.xagui.interfaces.*
import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws

/**
 * Represents a GUI menu
 * @param plugin The plugin that owns the menu
 * @param title The title of the menu
 * @param rowsToSet The number of rows the menu should have
 */
class GuiMenu(private val plugin: JavaPlugin, private val title: String, private val rowsToSet: Int, private val pages: Int): InventoryHolder, GuiInterface {

    //private val slots: ConcurrentHashMap<Int, GuiButtonInterface> = ConcurrentHashMap()
    private val pageMapping: ConcurrentHashMap<Int, ConcurrentHashMap<Int, GuiButtonInterface>> = ConcurrentHashMap()
    private val invMapping: ConcurrentHashMap<Int, Inventory> = ConcurrentHashMap()
    private val totalPages = pages
    private val stickySlots = mutableSetOf<Int>()
    private var name: String

    init {
        name = title
        totalPages.let {
            for (i in 0..it) {
                pageMapping[i] = ConcurrentHashMap()
                invMapping[i] = Bukkit.createInventory(this, getSize(), getName())
            }
        }
    }

    private var currentOpenedPage: Int = 0
    val unlockedSlots: ConcurrentHashMap<Int, MutableSet<Int>> = ConcurrentHashMap()

    /**
     * The inventory of the menu
     * @return The inventory
     */
    override fun getInventory(): Inventory {
        return invMapping[currentOpenedPage]!!
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
     * Set the action to be executed when a button is clicked
     * @param onClick The action to be executed
     */
    override fun setOnClick(onClick: (InventoryClickEvent) -> Unit) {
        this.onClickAction = object : GuiClickInterface {
            override fun onClick(event: InventoryClickEvent) {
                onClick(event)
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

    override fun getPages(): Int {
        return pageMapping.keys.size
    }

    override fun getCurrentPage(): Int {
        return currentOpenedPage
    }

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param button The button to set
     */
    override fun setSlot(slot: Int, button: GuiButtonInterface) {
        setSlot(currentOpenedPage, slot, button)
    }

    override fun setSlot(page: Int, slot: Int, button: GuiButtonInterface) {
        pageMapping[page]?.set(slot, button)
        invMapping[page]?.setItem(slot, button.getItem())
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
        updateSlot(currentOpenedPage, slot, item)
    }

    override fun updateSlot(page: Int, slot: Int, item: ItemStack) {
        val old = pageMapping[page]?.get(slot) ?: return
        old.setItem(item)
        setSlot(slot, old)
    }

    /**
     * Get the button in a slot
     * @param slot The slot to get the button from
     * @return The button in the slot
     */
    override fun getSlot(slot: Int): GuiButtonInterface? =
        getSlot(currentOpenedPage, slot)

    override fun getSlot(page: Int, slot: Int): GuiButtonInterface? {
        return pageMapping[page]?.get(slot)
    }

    /**
     * Clear the button in a slot
     * @param slot The slot to clear
     */
    override fun clearSlot(slot: Int) {
        clearSlot(currentOpenedPage, slot)
    }

    override fun clearSlot(page: Int, slot: Int) {
        pageMapping[page]?.remove(slot)
        invMapping[page]?.setItem(slot, null)
    }

    /**
     * Clear all buttons in the menu
     */
    override fun clearAllSlots() {
        clearAllSlots(currentOpenedPage)
    }

    override fun clearAllSlots(page: Int) {
        pageMapping[page]?.clear()
        invMapping[currentOpenedPage]?.clear()
    }

    /**
     * Get the plugin that owns the menu
     * @return The plugin that owns the menu
     */
    override fun getOwner(): JavaPlugin = plugin

    /**
     * Unlock a slot so the player can take items from it
     * @param slot The slot to unlock
     * @throws SlotOutOfBoundException If the slot is out of bounds
     */
    @Throws(SlotOutOfBoundException::class)
    override fun unlockButton(slot: Int) {
        if (slot > getSize()) throw SlotOutOfBoundException()
        unlockedSlots[0]?.add(slot)
    }

    override fun unlockButton(page: Int, slot: Int) {
        unlockedSlots[page]?.add(slot)
    }

    /**
     * Lock a slot
     * @param slot The slot to lock
     * @throws SlotOutOfBoundException If the slot is out of bounds
     */
    @Throws(SlotOutOfBoundException::class)
    override fun lockButton(slot: Int) {
        if (slot > getSize()) throw SlotOutOfBoundException()
        unlockedSlots[0]?.remove(slot)
    }

    override fun lockButton(page: Int, slot: Int) {
        unlockedSlots[page]?.remove(slot)
    }

    /**
     * Open the menu for a player
     * @param player The player to open the menu for
     */
    override fun open(player: Player) {
        switchPage(0, player)
    }

    override fun open(page: Int, player: Player) {
        switchPage(page, player)
    }

    override fun switchPage(page: Int, player: Player) {
        currentOpenedPage = page
        val inv = invMapping[currentOpenedPage] ?: return
        stickySlots.forEach {
            inv.setItem(it, pageMapping[0]?.get(it)?.getItem())
        }
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.openInventory(inv)
        })
    }

    override fun getMaxPages(): Int {
        return totalPages
    }

    override fun stickSlot(slot: Int) {
        stickySlots.add(slot)
    }

    override fun unStickSlot(slot: Int) {
        stickySlots.remove(slot)
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

    override fun fillSlots(slots: Set<Int>) {
        fillSlots(currentOpenedPage, slots, ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }})
    }

    /**
     * Fill a set of slots with an item
     * @param slots The slots to fill
     * @param item The item to fill the slots with
     */
    override fun fillSlots(slots: Set<Int>, item: ItemStack) {
        fillSlots(currentOpenedPage, slots, item)
    }

    override fun fillSlots(page: Int, slots: Set<Int>) {
        fillSlots(page, slots, ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }} )
    }

    override fun fillSlots(page: Int, slots: Set<Int>, item: ItemStack) {
        slots.forEach {
            setSlot(page, it, GuiButton(item))
        }
    }

    /**
     * Fill the border of the menu with an item
     * @param page The page of the menu
     * @param slots The set of slots to fill
     * @param button The gui button to fill the slots with
     */
    override fun fillSlots(page: Int, slots: Set<Int>, button: GuiButtonInterface) {
        slots.forEach {
            setSlot(page, it, button)
        }
    }

    /**
     * Fill the border of the menu with an item
     * @param rows The number of rows in the menu
     * @param item The item to fill the border with
     * @throws RowsOutOfBoundException If the number of rows is out of bounds
     */
    @Throws(RowsOutOfBoundException::class)
    fun fillBorder(rows: Int, item: ItemStack) {
        if (rows > this.rows || rows < 1) throw RowsOutOfBoundException()
        val slots = mutableSetOf<Int>()
        for (i in 0 until rows * 9) {
            if (i < 9 || i >= (rows - 1) * 9 || i % 9 == 0 || i % 9 == 8) {
                slots.add(i)
            }
        }
        fillSlots(slots, item)
    }

    /**
     * Fill the border of the menu with an item
     * @param rows The number of rows in the menu
     * @throws RowsOutOfBoundException If the number of rows is out of bounds
     */
    @Throws(RowsOutOfBoundException::class)
    fun fillBorder(rows: Int) {
        if (rows > this.rows || rows < 1) throw RowsOutOfBoundException()
        fillBorder(rows, ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }} )
    }

    private var rows: Int = rowsToSet

    var onCloseAction: GuiCloseInterface? = null

    var onOpenAction: GuiOpenInterface? = null

    var onClickAction: GuiClickInterface? = null

    //private val inv: Inventory = Bukkit.createInventory(this, getSize(), getName())
}