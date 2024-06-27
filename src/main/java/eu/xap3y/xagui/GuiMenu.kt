@file:Suppress("DEPRECATION")

package eu.xap3y.xagui

import eu.xap3y.xagui.interfaces.GuiButtonInterface
import eu.xap3y.xagui.interfaces.GuiInterface
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

class GuiMenu(private val plugin: JavaPlugin, private val title: String, private val rowsToSet: Int): InventoryHolder, GuiInterface {

    private val slots: ConcurrentHashMap<Int, GuiButtonInterface> = ConcurrentHashMap()
    val unlockedSlots = mutableSetOf<Int>()

    override fun getInventory(): Inventory {
        return inv
    }

    override fun onOpen(onOpen: () -> Unit) {
        onOpenAction = onOpen
    }

    override fun onClose(onClose: () -> Unit) {
        onCloseAction = onClose
    }

    override fun setName(newName: String) {
        name = newName
    }

    override fun getName(): String =
        ChatColor.translateAlternateColorCodes('&', name)

    override fun getRawName(): String = name

    override fun getSize(): Int = rows * 9

    override fun setSlot(slot: Int, button: GuiButtonInterface) {
        slots[slot] = button
        inv.setItem(slot, button.getItem())
    }

    override fun getSlot(slot: Int): GuiButtonInterface? =
        slots[slot]

    override fun clearSlot(slot: Int) {
        slots.remove(slot)
        inv.setItem(slot, null)
    }

    override fun clearAllSlots() {
        inv.clear()
    }

    override fun refresh() {
        // REOPEN THE MENU
    }

    override fun getOwner(): JavaPlugin = plugin

    override fun unlockButton(slot: Int) {
        unlockedSlots.add(slot)
    }

    override fun lockButton(slot: Int) {
        unlockedSlots.remove(slot)
    }

    override fun open(player: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.openInventory(inv)
        })
    }

    private var rows: Int = rowsToSet

    var onCloseAction: (() -> Unit)? = null

    private var onOpenAction: (() -> Unit)? = null

    private var name: String = title

    private val inv: Inventory = Bukkit.createInventory(this, getSize(), getName())
}