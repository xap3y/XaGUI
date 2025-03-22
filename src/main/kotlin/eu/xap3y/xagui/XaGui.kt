package eu.xap3y.xagui

import com.cryptomorin.xseries.XMaterial
import eu.xap3y.xagui.interfaces.GuiButtonInterface
import eu.xap3y.xagui.listeners.GuiMenuListener
import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin


/**
 * The main class of the library
 *
 * @param plugin The plugin instance
 */
class XaGui(private val plugin: JavaPlugin) {

    init {
        plugin.server.pluginManager.registerEvents(GuiMenuListener(plugin), plugin)
    }


    /**
     * Create a new menu
     *
     * @param name The name of the menu
     * @param rows The number of rows in the menu
     * @return The created menu
     * @see GuiMenu
     */
    fun createMenu(name: String, rows: Int): GuiMenu {
        return createMenu(name, rows, 1)
    }

    /**
     * Create a new menu
     *
     * @param name The name of the menu
     * @param rows The number of rows in the menu
     * @param pages The number of pages in the menu
     * @return The created menu
     * @see GuiMenu
     */
    fun createMenu(name: String, rows: Int, pages: Int): GuiMenu {
        return GuiMenu(plugin, name, rows, pages)
    }

    /**
     * Set the item that will be used to fill the menu border
     *
     * @param item The item that will fill the border
     */
    fun setBorderItem(item: ItemStack) {
        borderFiller = item
    }

    /**
     * Set the item that will be used as the close button
     *
     * @param item The item that will be used as the close button
     */
    fun setCloseButton(item: GuiButton) {
        closeButton = item
    }

    /**
     * Set the item that will be used in paginator as next page button
     *
     * @param item
     */
    fun setNextPageButton(item: ItemStack) {
        nextPageButton = item
    }

    /**
     * Set the item that will be used in paginator as previous page button
     *
     * @param item
     */
    fun setPreviousPageButton(item: ItemStack) {
        previousPageButton = item
    }

    /**
     * Set the sound that will be played when a player is redirected to another menu
     *
     * @param sound The sound that will be played
     */
    fun setRedirectSound(sound: Sound?) {
        redirectSound = sound
    }

    /**
     * Set the sound that will be played when a player closes a menu
     *
     * @param sound The sound that will be played
     */
    fun setCloseButtonSound(sound: Sound?) {
        closeButtonSound = sound
    }

    inline fun <reified T : Any> initRegistry() {
        registry = GuiRegistry<T>()
    }

    fun <T : Any> initRegistry(clazz: Class<T>) {
        registry = GuiRegistry<T>()
    }

    fun getRegistry(): GuiRegistry<*> {
        return registry
    }

    companion object {
        var redirectSound: Sound? = null
        var closeButtonSound: Sound? = null
        var borderFiller: ItemStack = GuiButton(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.AIR)).setName("&r").getItem()
        var closeButton: GuiButtonInterface = GuiButton(ItemStack(XMaterial.BARRIER.parseMaterial() ?: Material.AIR)).setName("&cClose").withListener {
            it.whoClicked.closeInventory()
            if (closeButtonSound != null) {
                (it.whoClicked as Player).playSound(it.whoClicked as Player, closeButtonSound ?: Sound.BLOCK_ENDER_CHEST_CLOSE, .5f, 1f)
            }
        }

        var nextPageButton: ItemStack = GuiButton(Material.ARROW).setName("&eNext page").getItem()
        var previousPageButton: ItemStack = GuiButton(Material.ARROW).setName("&ePrevious page").getItem()

        lateinit var registry: GuiRegistry<*>
    }

}
