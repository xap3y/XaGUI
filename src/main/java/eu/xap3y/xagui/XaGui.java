package eu.xap3y.xagui;

import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.xagui.interfaces.GuiButtonInterface;
import eu.xap3y.xagui.listeners.MenuListener;
import eu.xap3y.xagui.models.GuiButton;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class XaGui {

    private final JavaPlugin plugin;

    public XaGui(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new MenuListener(plugin), plugin);
    }

    /**
     * Create a new menu
     *
     * @param name The name of the menu
     * @param rows The number of rows in the menu
     * @return The created menu
     * @see GuiMenu
     */
    public GuiMenu createMenu(String name, int rows) {
        return createMenu(name, rows, 1);
    }

    /**
     * Create a new menu
     *
     * @param name  The name of the menu
     * @param rows  The number of rows in the menu
     * @param pages The number of pages in the menu
     * @return The created menu
     * @see GuiMenu
     */
    public GuiMenu createMenu(String name, int rows, int pages) {
        return new GuiMenu(plugin, name, rows, pages);
    }

    /**
     * Set the item that will be used to fill the menu border
     *
     * @param item The item that will fill the border
     */
    public void setBorderItem(ItemStack item) {
        borderFiller = item;
    }

    /**
     * Set the item that will be used as the close button
     *
     * @param item The item that will be used as the close button
     */
    public void setCloseButton(GuiButton item) {
        closeButton = item;
    }

    /**
     * Set the item that will be used in paginator as next page button
     *
     * @param item the item to use as next page button
     */
    public void setNextPageButton(ItemStack item) {
        nextPageButton = item;
    }

    /**
     * Set the item that will be used in paginator as previous page button
     *
     * @param item the item to use as previous page button
     */
    public void setPreviousPageButton(ItemStack item) {
        previousPageButton = item;
    }

    /**
     * Set the sound that will be played when a player is redirected to another menu
     *
     * @param sound The sound that will be played
     */
    public void setRedirectSound(Sound sound) {
        redirectSound = sound;
    }

    /**
     * Set the sound that will be played when a player closes a menu
     *
     * @param sound The sound that will be played
     */
    public void setCloseButtonSound(Sound sound) {
        closeButtonSound = sound;
    }

    @Getter
    private static Sound redirectSound = null;

    @Getter
    private static Sound closeButtonSound = null;

    @Getter
    private static ItemStack borderFiller = new GuiButton(
            XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() != null
                    ? XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()
                    : new ItemStack(Material.AIR)
    ).setName("&r").getItem();

    @Getter
    private static GuiButtonInterface closeButton = new GuiButton(
            new ItemStack(
                    XMaterial.BARRIER.parseMaterial() != null
                            ? XMaterial.BARRIER.parseMaterial()
                            : Material.AIR
            )
    ).setName("&cClose").withListener(e -> {
        e.getWhoClicked().closeInventory();
        if (closeButtonSound != null) {
            Player p = (Player) e.getWhoClicked();
            p.playSound(p, closeButtonSound != null ? closeButtonSound : Sound.BLOCK_ENDER_CHEST_CLOSE, .5f, 1f);
        }
    });

    @Getter
    private static ItemStack nextPageButton = new GuiButton(Material.ARROW).setName("&eNext page").getItem();

    @Getter
    private static ItemStack previousPageButton = new GuiButton(Material.ARROW).setName("&ePrevious page").getItem();
}