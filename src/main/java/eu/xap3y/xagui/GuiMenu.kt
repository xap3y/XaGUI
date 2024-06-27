@file:Suppress("DEPRECATION")

package eu.xap3y.xagui

import eu.xap3y.xagui.interfaces.GuiButtonInterface
import eu.xap3y.xagui.interfaces.GuiCloseInterface
import eu.xap3y.xagui.interfaces.GuiInterface
import eu.xap3y.xagui.interfaces.GuiOpenInterface
import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

class GuiMenu(private val plugin: JavaPlugin, private val title: String, private val rowsToSet: Int): InventoryHolder, GuiInterface {

    private val slots: ConcurrentHashMap<Int, GuiButtonInterface> = ConcurrentHashMap()
    val unlockedSlots = mutableSetOf<Int>()

    override fun getInventory(): Inventory {
        return inv
    }

    override fun setOnOpen(openAction: (InventoryOpenEvent) -> Unit) {
        this.onOpenAction = object : GuiOpenInterface {
            override fun onOpen(event: InventoryOpenEvent) {
                openAction(event)
            }
        }
    }

    override fun setOnClose(closeAction: (InventoryCloseEvent) -> Unit) {
        this.onCloseAction = object : GuiCloseInterface {
            override fun onClose(event: InventoryCloseEvent) {
                closeAction(event)
            }
        }
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

    fun setSlot(slot: Int, item: ItemStack) {
        setSlot(slot, GuiButton(item))
    }

    override fun updateSlot(slot: Int, item: ItemStack) {
        val old = slots[slot] ?: return
        old.setItem(item)
        setSlot(slot, old)
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

    override fun close(player: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.closeInventory()
        })
    }

    private var rows: Int = rowsToSet

    var onCloseAction: GuiCloseInterface? = null

    var onOpenAction: GuiOpenInterface? = null

    private var name: String = title

    private val inv: Inventory = Bukkit.createInventory(this, getSize(), getName())
}