package eu.xap3y.xagui.listeners

import eu.xap3y.xagui.GuiMenu
import eu.xap3y.xagui.interfaces.GuiButtonInterface
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * The listener for the menus
 * @param plugin The plugin instance
 */
class GuiMenuListener(private val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {

        if (e.clickedInventory == null || e.clickedInventory?.holder !is GuiMenu) {
            if (e.view.topInventory.holder is GuiMenu) {
                val clickedInventory = e.view.topInventory.holder as GuiMenu
                if (!clickedInventory.getSelfInventoryAccess()) {
                    e.result = Event.Result.DENY
                } else if (clickedInventory.getAllowedSelfInventoryClickTypes().isNotEmpty() && clickedInventory.getAllowedSelfInventoryClickTypes().contains(e.click)) {
                    e.result = Event.Result.ALLOW
                } else {
                    e.result = Event.Result.DENY
                }
                clickedInventory.onClickActionOwn?.onClick(e)
            }
            return
        }

        val clickedInventory = (e.clickedInventory?.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        if (clickedInventory.getAllowedClickTypes().isNotEmpty() && !clickedInventory.getAllowedClickTypes().contains(e.click)) {
            e.result = Event.Result.DENY
            return
        }
        else if (clickedInventory.getBlacklistedClickTypes().isNotEmpty() && clickedInventory.getBlacklistedClickTypes().contains(e.click)) {
            e.result = Event.Result.DENY
            return
        }

        val allowClick: Boolean = clickedInventory.unlockedSlots[clickedInventory.getCurrentPageIndex()]?.contains(e.slot) ?: false

        if (!allowClick) {
            e.result = Event.Result.DENY
        } else {
            e.result = Event.Result.ALLOW
        }

        clickedInventory.onClickAction?.onClick(e)

        val button: GuiButtonInterface = clickedInventory.getSlot(e.slot) ?: return

        if (button.getClickSound() != null) {
            val player: Player = e.whoClicked as Player
            player.playSound(player, button.getClickSound() ?: Sound.UI_BUTTON_CLICK, 1f, 1f)
        }

        button.getClickListener()?.onClick(e)

        button.callRedirect(e.whoClicked as Player)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (e.inventory.holder !is GuiMenu) return

        val clickedInventory = (e.inventory.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        clickedInventory.onCloseAction?.onClose(e)
    }

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent) {
        if (e.inventory.holder !is GuiMenu) return

        val clickedInventory = (e.inventory.holder ?: return) as GuiMenu

        val owner = clickedInventory.getOwner()
        if (!Objects.equals(owner, plugin)) return

        clickedInventory.onOpenAction?.onOpen(e)
    }
}