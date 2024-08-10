package eu.xap3y.xagui.interfaces

import eu.xap3y.xagui.models.GuiPageSwitchModel
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/**
 * Interface for GUIs
 */
interface GuiInterface {

    fun setOnOpen(openAction: (InventoryOpenEvent) -> Unit)
    fun setOnOpen(openAction: GuiOpenInterface)

    fun setOnClose(closeAction: (InventoryCloseEvent) -> Unit)
    fun setOnClose(closeAction: GuiCloseInterface)

    fun setOnClick(onClick: (InventoryClickEvent) -> Unit)
    fun setOnClick(onClick: GuiClickInterface)

    fun setOnPageSwitch(onPageSwitch: (GuiPageSwitchModel) -> Unit)
    fun setOnPageSwitch(onPageSwitch: GuiPageSwitchInterface)

    fun setName(newName: String)

    fun getName(): String

    fun getRawName(): String

    fun getSize(): Int

    fun getPages(): Int

    fun getCurrentPageIndex(): Int

    fun getCurrentPage(): Int

    fun setSlot(slot: Int, button: GuiButtonInterface)

    fun setSlot(slot: Int, item: ItemStack)

    fun setSlot(slot: Int, item: Material)

    fun setSlot(page: Int, slot: Int, button: GuiButtonInterface)

    fun setSlot(page: Int, slot: Int, button: ItemStack)

    fun setSlot(page: Int, slot: Int, button: Material)

    fun updateSlot(slot: Int, item: ItemStack)

    fun updateSlot(slot: Int, item: Material)

    fun updateSlot(page: Int, slot: Int, item: ItemStack)

    fun updateSlot(page: Int, slot: Int, item: Material)

    fun getSlot(slot: Int): GuiButtonInterface?

    fun getSlot(page: Int, slot: Int): GuiButtonInterface?

    fun clearSlot(slot: Int)

    fun clearSlot(page: Int, slot: Int)

    fun clearAllSlots()

    fun clearAllSlots(page: Int)

    //fun refresh()

    fun getOwner(): JavaPlugin

    fun unlockButton(slot: Int)

    fun unlockButton(page: Int, slot: Int)

    fun lockButton(slot: Int)

    fun lockButton(page: Int, slot: Int)

    fun open(player: Player)

    fun open(page: Int, player: Player)

    fun switchPage(pageIndex: Int, player: Player)

    fun getMaxPages(): Int

    fun stickSlot(slot: Int)

    fun unStickSlot(slot: Int)

    fun close(player: Player)

    fun fillSlots(slots: Set<Int>)

    fun fillSlots(slots: Set<Int>, item: ItemStack)

    fun fillSlots(page: Int, slots: Set<Int>)

    fun fillSlots(page: Int, slots: Set<Int>, item: ItemStack)

    fun fillSlots(page: Int, slots: Set<Int>, button: GuiButtonInterface)

    fun addCloseButton()

    fun addCloseButtonAllPages()

    fun addCloseButton(page: Int, button: ItemStack)

    fun fillBorder()

    fun fillBorder(page: Int, item: ItemStack)

    fun fillBorder(item: ItemStack)

    fun fillBorder(material: Material)

    fun setSelfInventoryAccess(value: Boolean)

    fun getSelfInventoryAccess(): Boolean

    fun allowClickTypes(vararg types: ClickType)

    fun blacklistClickTypes(vararg types: ClickType)

    fun getAllowedClickTypes(): Set<ClickType>

    fun getBlacklistedClickTypes(): Set<ClickType>


}