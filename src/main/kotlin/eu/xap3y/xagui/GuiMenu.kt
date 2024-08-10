@file:Suppress("DEPRECATION")

package eu.xap3y.xagui

import com.cryptomorin.xseries.XMaterial
import eu.xap3y.xagui.exceptions.PageOutOfBoundException
import eu.xap3y.xagui.exceptions.SlotOutOfBoundException
import eu.xap3y.xagui.interfaces.*
import eu.xap3y.xagui.models.GuiButton
import eu.xap3y.xagui.models.GuiPageSwitchModel
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
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
 * @param pages The number of pages the menu should have
 */
class GuiMenu(private val plugin: JavaPlugin, private val title: String, private val rowsToSet: Int, private val pages: Int): InventoryHolder, GuiInterface {

    //private val slots: ConcurrentHashMap<Int, GuiButtonInterface> = ConcurrentHashMap()
    private val pageMapping: ConcurrentHashMap<Int, ConcurrentHashMap<Int, GuiButtonInterface>> = ConcurrentHashMap()
    private val invMapping: ConcurrentHashMap<Int, Inventory> = ConcurrentHashMap()
    private val totalPages = pages
    private val stickySlots = mutableSetOf<Int>()
    private var name: String
    private var rows: Int
    private var unlockSelfInventoryClick: Boolean = false
    private val allowedClickTypes = mutableSetOf<ClickType>()
    private val blacklistedClickTypes = mutableSetOf<ClickType>()

    var onCloseAction: GuiCloseInterface? = null

    var onOpenAction: GuiOpenInterface? = null

    var onClickAction: GuiClickInterface? = null

    var onPageSwitchAction: GuiPageSwitchInterface? = null

    private var currentOpenedPage: Int = 0
    val unlockedSlots: HashMap<Int, MutableSet<Int>> = hashMapOf() // page -> slots

    /**
     * Initialize the menu
     */
    init {
        name = title
        rows = rowsToSet
        (totalPages-1).let {
            for (i in 0..it) {
                pageMapping[i] = ConcurrentHashMap()
                invMapping[i] = Bukkit.createInventory(this, getSize(), getName())
                unlockedSlots[i] = mutableSetOf()
            }
        }
    }

    /**
     * The inventory of the menu
     * @return The inventory
     */
    override fun getInventory(): Inventory {
        return invMapping[currentOpenedPage] ?: Bukkit.createInventory(this, getSize(), getName())
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
     * The action to be executed when the menu is opened
     * @param openAction The action to be executed
     */
    override fun setOnOpen(openAction: GuiOpenInterface) {
        this.onOpenAction = openAction
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
     * The action to be executed when the menu is closed
     * @param closeAction The action to be executed
     */
    override fun setOnClose(closeAction: GuiCloseInterface) {
        this.onCloseAction = closeAction
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
     * Set the action to be executed when a button is clicked
     * @param onClick The action to be executed
     */
    override fun setOnClick(onClick: GuiClickInterface) {
        this.onClickAction = onClick
    }

    /**
     * Set the action to be executed when the page is switched
     * @param onPageSwitch The action to be executed
     */
    override fun setOnPageSwitch(onPageSwitch: (GuiPageSwitchModel) -> Unit) {
        this.onPageSwitchAction = object : GuiPageSwitchInterface {
            override fun onPageSwitch(data: GuiPageSwitchModel) {
                onPageSwitch(data)
            }
        }
    }

    /**
     * Set the action to be executed when the page is switched
     * @param onPageSwitch The action to be executed
     */
    override fun setOnPageSwitch(onPageSwitch: GuiPageSwitchInterface) {
        this.onPageSwitchAction = onPageSwitch
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
     * Get the number of pages in the menu
     * @return The number of pages in the menu
     */
    override fun getPages(): Int {
        return pageMapping.keys.size
    }

    /**
     * Get the current index of page of the menu
     * @return The current index of the menu page
     */
    override fun getCurrentPageIndex(): Int {
        return currentOpenedPage
    }

    /**
     * Get the current page of the menu
     * @return The current page of the menu
     */
    override fun getCurrentPage(): Int {
        return currentOpenedPage + 1
    }

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param button The button to set
     */
    override fun setSlot(slot: Int, button: GuiButtonInterface) {
        setSlot(currentOpenedPage, slot, button)
    }

    /**
     * Set a button in a slot
     * @param page The page of the menu
     * @param slot The slot to set the button in
     * @param button The button to set
     */
    override fun setSlot(page: Int, slot: Int, button: GuiButtonInterface) {
        pageMapping[page]?.set(slot, button)
        invMapping[page]?.setItem(slot, button.getItem())
    }


    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param button The button to set
     */
    override fun setSlot(page: Int, slot: Int, button: ItemStack) {
        setSlot(page, slot, GuiButton(button))
    }

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param button The button to set
     */
    override fun setSlot(page: Int, slot: Int, button: Material) {
        setSlot(page, slot, GuiButton(ItemStack(button)))
    }

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param item The item to set
     */
    override fun setSlot(slot: Int, item: ItemStack) {
        setSlot(currentOpenedPage, slot, GuiButton(item))
    }

    /**
     * Set a button in a slot
     * @param slot The slot to set the button in
     * @param item The item to set
     */
    override fun setSlot(slot: Int, item: Material) {
        setSlot(currentOpenedPage, slot, GuiButton(ItemStack(item)))
    }

    /**
     * Update the item in a slot
     * @param slot The slot to update
     * @param item The item to update to
     */
    override fun updateSlot(slot: Int, item: ItemStack) {
        updateSlot(currentOpenedPage, slot, item)
    }

    /**
     * Update the item in a slot
     * @param slot The slot to update
     * @param item The item to update to
     */
    override fun updateSlot(slot: Int, item: Material) {
        updateSlot(currentOpenedPage, slot, ItemStack(item))
    }

    /**
     * Update the item in a slot
     * @param page The page of the menu
     * @param slot The slot to update
     * @param item The item to update to
     */
    override fun updateSlot(page: Int, slot: Int, item: ItemStack) {
        val old = pageMapping[page]?.get(slot) ?: return
        old.setItem(item)
        setSlot(slot, old)
    }

    /**
     * Update the item in a slot
     * @param page The page of the menu
     * @param slot The slot to update
     * @param item The item to update to
     */
    override fun updateSlot(page: Int, slot: Int, item: Material) {
        updateSlot(page, slot, ItemStack(item))
    }

    /**
     * Get the button in a slot
     * @param slot The slot to get the button from
     * @return The button in the slot
     */
    override fun getSlot(slot: Int): GuiButtonInterface? =
        getSlot(currentOpenedPage, slot)

    /**
     * Get the button in a slot
     * @param page The page of the menu
     * @param slot The slot to get the button from
     * @return The button in the slot
     */
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

    /**
     * Clear the button in a slot
     *
     * @param page The page of the menu
     * @param slot The slot to clear
     */
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

    /**
     * Clear all buttons in the menu
     *
     * @param page The page of the menu
     */
    override fun clearAllSlots(page: Int) {
        pageMapping[page]?.clear()
        invMapping[currentOpenedPage]?.clear()
    }

    /**
     * Get the plugin that owns the menu
     *
     * @return The plugin that owns the menu
     */
    override fun getOwner(): JavaPlugin = plugin

    /**
     * Unlock a slot so the player can take items from it
     *
     * @param slot The slot to unlock
     * @throws SlotOutOfBoundException If the slot is out of bounds
     */
    @Throws(SlotOutOfBoundException::class)
    override fun unlockButton(slot: Int) {
        unlockButton(currentOpenedPage, slot)
    }

    /**
     * Unlock a slot so the player can take items from it
     *
     * @param page The page of the menu
     * @param slot The slot to unlock
     * @throws SlotOutOfBoundException If the slot is out of bounds
     */
    @Throws(SlotOutOfBoundException::class)
    override fun unlockButton(page: Int, slot: Int) {
        if (slot > getSize()) throw SlotOutOfBoundException()
        unlockedSlots[page]?.add(slot)
    }

    /**
     * Lock a slot
     *
     * @param slot The slot to lock
     * @throws SlotOutOfBoundException If the slot is out of bounds
     */
    @Throws(SlotOutOfBoundException::class)
    override fun lockButton(slot: Int) {
       lockButton(currentOpenedPage, slot)
    }

    /**
     * Lock a slot
     *
     * @param page The page of the menu
     * @param slot The slot to lock
     * @throws SlotOutOfBoundException If the slot is out of bounds
     */
    @Throws(SlotOutOfBoundException::class)
    override fun lockButton(page: Int, slot: Int) {
        if (slot > getSize()) throw SlotOutOfBoundException()
        unlockedSlots[page]?.remove(slot)
    }

    /**
     * Open the menu for a player
     *
     * @param player The player to open the menu for
     */
    override fun open(player: Player) {
        switchPage(0, player)
    }

    /**
     * Open a specific page of the menu for a player
     *
     * @param page The page to open
     * @param player The player to open the menu for
     */
    override fun open(page: Int, player: Player) {
        switchPage(page, player)
    }

    /**
     * Switch the page of the menu for a player
     *
     * @param pageIndex The index of a page to switch to
     * @param player The player to switch the page for
     * @throws PageOutOfBoundException If the page is out of bounds
     */
    @Throws(PageOutOfBoundException::class)
    override fun switchPage(pageIndex: Int, player: Player) {
        if (pageIndex > totalPages-1 || pageIndex < 0) throw PageOutOfBoundException()
        val oldPage: Int = currentOpenedPage
        currentOpenedPage = pageIndex
        val inv: Inventory = invMapping[currentOpenedPage] ?: return
        stickySlots.forEach {
            setSlot(pageIndex, it, pageMapping[0]?.get(it) ?: return@forEach)
        }
        Bukkit.getScheduler().runTask(plugin, Runnable {
            //plugin.server.pluginManager.callEvent(GuiPageSwitchEvent(player, page, currentOpenedPage, inv, invMapping[currentOpenedPage]))
            player.openInventory(inv)
        })
        this.onPageSwitchAction?.onPageSwitch(GuiPageSwitchModel(player, pageIndex, oldPage))
    }

    /**
     * Get the maximum number of pages in the menu
     * @return The maximum number of pages
     */
    override fun getMaxPages(): Int {
        return totalPages
    }

    /**
     * Stick a slot so it will be displayed on every page
     * @param slot The slot to stick
     */
    override fun stickSlot(slot: Int) {
        stickySlots.add(slot)
    }

    /**
     * Unstick a slot
     * @param slot The slot to unstick
     */
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

    /**
     * Fill a set of slots with a gray stained glass pane
     * @param slots The slots to fill
     */
    override fun fillSlots(slots: Set<Int>) {
        fillSlots(currentOpenedPage, slots, ItemStack(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial() ?: Material.AIR).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }})
    }

    /**
     * Fill a set of slots with an item
     * @param slots The slots to fill
     * @param item The item to fill the slots with
     */
    override fun fillSlots(slots: Set<Int>, item: ItemStack) {
        fillSlots(currentOpenedPage, slots, item)
    }

    /**
     * Fill a set of slots with an item
     * @param page The page of the menu
     * @param slots The slots to fill
     */
    override fun fillSlots(page: Int, slots: Set<Int>) {
        fillSlots(page, slots, ItemStack(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial() ?: Material.AIR).apply { itemMeta = itemMeta.apply { setDisplayName(" ") }} )
    }

    /**
     * Fill a set of slots with an item
     * @param page The page of the menu
     * @param slots The slots to fill
     * @param item The item to fill the slots with
     */
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
     * Add a close button to the menu
     */
    override fun addCloseButton() {
        addCloseButton(0, ItemStack(XMaterial.BARRIER.parseMaterial() ?: Material.AIR))
    }

    /**
     * Add a close button to the menu
     */
    override fun addCloseButtonAllPages() {
        for (i in 0 until totalPages) {
            addCloseButton(i, ItemStack(XMaterial.BARRIER.parseMaterial() ?: Material.AIR))
        }
        //setSlot(row * 9 + middle, GuiButton(button).setName("&c&lClose").withListener { it.whoClicked.closeInventory() })
    }

    override fun addCloseButton(page: Int, button: ItemStack) {
        val row = rows - 1
        val middle = 4
        val slot = row * 9 + middle
        setSlot(page, slot, GuiButton(button).setName("&c&lClose").withListener { it.whoClicked.closeInventory() })
    }

    /**
     * Fills a border for all pages with gray stained glass panes
     */
    override fun fillBorder() {
        (totalPages-1).let {
            for (i in 0..it) {
                fillBorder(i, GuiButton(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.AIR)).setName("&r").getItem())
            }
        }
        //fillBorder(currentOpenedPage, GuiButton(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.AIR)).setName("").getItem())
    }

    /**
     * Fills a border with specified itemstack
     * @param item ItemStack to fill border with
     */
    override fun fillBorder(item: ItemStack) {
        fillBorder(currentOpenedPage, item)
    }

    /**
     * Fills a border with specified ItemStack for a specific page
     * @param page The page of the menu
     * @param item ItemStack to fill border with
     */
    override fun fillBorder(page: Int, item: ItemStack) {
        val slots = mutableSetOf<Int>()
        for (i in 0 until rows * 9) {
            if (i < 9 || i >= (rows - 1) * 9 || i % 9 == 0 || i % 9 == 8) {
                slots.add(i)
            }
        }
        fillSlots(page, slots, item)
    }

    /**
     * Fills a border with specified material
     * @param material Material to fill border with
     */
    override fun fillBorder(material: Material) {
        fillBorder(ItemStack(material))
    }

    /**
     * If the player can access their own inventory
     * @param value The new value (boolean)
     */
    override fun setSelfInventoryAccess(value: Boolean) {
        unlockSelfInventoryClick = value
    }

    /**
     * If the player can access their own inventory
     * @return If the player can access their own inventory
     */
    override fun getSelfInventoryAccess(): Boolean {
        return unlockSelfInventoryClick
    }

    /**
     * Allow a set of click types
     * @param types The click types to allow
     */
    override fun allowClickTypes(vararg types: ClickType) {
        allowedClickTypes.addAll(types.toSet())
    }

    /**
     * Get the allowed click types
     * @return The allowed click types
     */
    override fun getAllowedClickTypes(): Set<ClickType> {
        return allowedClickTypes
    }

    /**
     * Blacklist a set of click types
     * @param types The click types to blacklist
     */
    override fun blacklistClickTypes(vararg types: ClickType) {
        blacklistedClickTypes.addAll(types.toSet())
    }

    /**
     * Get the blacklisted click types
     * @return The blacklisted click types
     */
    override fun getBlacklistedClickTypes(): Set<ClickType> {
        return blacklistedClickTypes
    }

    //private val inv: Inventory = Bukkit.createInventory(this, getSize(), getName())
}