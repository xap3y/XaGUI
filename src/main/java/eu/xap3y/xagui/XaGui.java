package eu.xap3y.xagui;

import eu.xap3y.xagui.interfaces.GuiButtonInterface;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.listeners.MenuListener;
import eu.xap3y.xagui.models.GuiButton;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class XaGui {

    private final JavaPlugin plugin;

    @Getter
    private static boolean isPaper = false;

    @Getter
    private static boolean isFolia = false;

    @Getter
    private static boolean useKyoriText = false;

    private final static Map<UUID, GuiMenuInterface> openMenus = new ConcurrentHashMap<>();

    public XaGui(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getConsoleSender().sendMessage("Registering XaGui..");
        plugin.getServer().getPluginManager().registerEvents(new MenuListener(plugin), plugin);

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {}

        plugin.getServer().getConsoleSender().sendMessage("Checking for Kyori Adventure...");
        try {
            //plugin.getServer().getConsoleSender().sendMessage("&cUsing Kyori Adventure for text components");
            Class.forName("net.kyori.adventure.text.Component");
            useKyoriText = true;
        } catch (ClassNotFoundException ignored) {
            //plugin.getServer().getConsoleSender().sendMessage("&cNot using Kyori Adventure for text components");
        }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException ignored) {}
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
     * Register an open menu
     *
     * @param uuid The UUID of the player
     */
    public void closeMenu(UUID uuid) {
        if (openMenus.containsKey(uuid)) {
            GuiMenuInterface menu = openMenus.get(uuid);
            menu.close();
        }
        openMenus.remove(uuid);
    }

    /**
     * Close all open menus
     */
    public void closeAll() {
        for (UUID uuid : openMenus.keySet()) {
            closeMenu(uuid);
        }
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
     * Get all open menus
     *
     * @return A map of all open menus
     */
    public Map<UUID, GuiMenuInterface> getOpenMenus() {
        return openMenus;
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

    /**
     * Set the sound that will be played when a player clicks a button
     *
     * @param sound The sound that will be played
     */
    public void setButtonClickSound(Sound sound, float volume) {
        buttonClickSoundVolume = volume;
        buttonClickSound = sound;
    }

    /**
     * Set the sound that will be played when a player clicks a button
     *
     * @param sound The sound that will be played
     */
    public void setClickSound(Sound sound) {
        clickSound = sound;
    }

    @Getter
    private static Sound redirectSound = null;

    @Getter
    private static Sound clickSound = null;

    @Getter
    private static Sound buttonClickSound = null;

    @Getter
    private static float buttonClickSoundVolume = 1f;

    @Getter
    private static Sound closeButtonSound = null;

    @Getter
    private static ItemStack borderFiller = new GuiButton(Material.GRAY_STAINED_GLASS_PANE).setName("&r").getItem();

    @Getter
    private static GuiButtonInterface closeButton = new GuiButton(
            new ItemStack(Material.BARRIER)
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

    public static void addOpenMenu(UUID uuid, GuiMenuInterface menu) {
        openMenus.put(uuid, menu);
    }

    public static void removeOpenMenu(UUID uuid) {
        openMenus.remove(uuid);
    }
}