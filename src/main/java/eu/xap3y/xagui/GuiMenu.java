package eu.xap3y.xagui;

import eu.xap3y.xagui.adapter.Legacy;
import eu.xap3y.xagui.adapter.PaperAdapter;
import eu.xap3y.xagui.exception.PageOutOfBoundException;
import eu.xap3y.xagui.interfaces.GuiButtonInterface;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.interfaces.listeners.GuiClickInterface;
import eu.xap3y.xagui.interfaces.listeners.GuiCloseInterface;
import eu.xap3y.xagui.interfaces.listeners.GuiOpenInterface;
import eu.xap3y.xagui.interfaces.listeners.GuiOwnClickInterface;
import eu.xap3y.xagui.interfaces.listeners.GuiPageSwitchInterface;
import eu.xap3y.xagui.models.GuiButton;
import eu.xap3y.xagui.models.GuiPageSwitchModel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a paginated GUI menu.
 * <p>
 * Supports:
 * <ul>
 *   <li>Multiple pages with optional paginator controls</li>
 *   <li>Sticky slots rendered on every page</li>
 *   <li>Per-slot buttons with click listeners</li>
 *   <li>Per-inventory and self-inventory click policies (allow/blacklist)</li>
 *   <li>Open/close/click/page-switch callbacks</li>
 * </ul>
 * Implements InventoryHolder so it can be used directly as a Bukkit/Spigot inventory holder.
 */
@SuppressWarnings("deprecation")
public class GuiMenu implements InventoryHolder, GuiMenuInterface {
    private final JavaPlugin plugin;

    // page -> (slot -> button)
    private final Map<Integer, Map<Integer, GuiButtonInterface>> pageMapping = new ConcurrentHashMap<>();
    private final Map<Integer, Inventory> invMapping = new ConcurrentHashMap<>();
    private final Set<Integer> stickySlots = new HashSet<>();
    /** Tracks unlocked slots per page (page -> set of slot indices) */
    public final Map<Integer, Set<Integer>> unlockedSlots = new HashMap<>();

    private int totalPages;
    private String name;

    private final int rows;

    private boolean unlockSelfInventoryClick = false;
    private final Set<ClickType> allowedClickTypes = new HashSet<>();
    private final Set<ClickType> allowedClickTypesSelf = new HashSet<>();
    private final Set<ClickType> blacklistedClickTypes = new HashSet<>();

    private boolean paginator = false;
    private ItemStack nextPageButton = null;
    private ItemStack previousPageButton = null;
    private Sound pageSwitchSound = null;
    private Sound openSound = null;
    private float openSoundFloat = 1f;

    /** Callback invoked on inventory close. */
    public GuiCloseInterface onCloseAction = null;
    /** Callback invoked on inventory open. */
    public GuiOpenInterface onOpenAction = null;
    /** Callback invoked on top-inventory clicks. */
    public GuiClickInterface onClickAction = null;
    /** Callback invoked on self-inventory clicks while viewing this GUI. */
    public GuiOwnClickInterface onClickActionOwn = null;
    /** Callback invoked after a page switch occurs. */
    public GuiPageSwitchInterface onPageSwitchAction = null;

    private Runnable callbackAction = null;

    private int currentOpenedPage = 0;

    /**
     * Create a new GUI menu.
     *
     * @param plugin     the owning plugin
     * @param title      the menu title (supports color codes with {@literal &})
     * @param rowsToSet  number of rows (1-6 typical)
     * @param pages      initial number of pages to allocate
     */
    public GuiMenu(JavaPlugin plugin, String title, int rowsToSet, int pages) {
        this.plugin = plugin;
        this.name = title;
        this.rows = rowsToSet;
        this.totalPages = pages;

        for (int i = 0; i <= totalPages - 1; i++) {
            pageMapping.put(i, new ConcurrentHashMap<>());
            if (XaGui.isUseKyoriText()) {
                invMapping.put(i, PaperAdapter.createInventory(this, getSize(), getName()));
            } else {
                invMapping.put(i, Bukkit.createInventory(this, getSize(), getName()));
            }
            unlockedSlots.put(i, new HashSet<>());
        }
    }

    /**
     * Get the current page's Inventory.
     *
     * @return the currently active page inventory
     */
    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = invMapping.get(currentOpenedPage);
        return inv != null ? inv : Bukkit.createInventory(this, getSize(), getName());
    }

    /**
     * Close the GUI for the current viewer.
     */
    @Override
    public void close() {
        if (XaGui.isFolia()) {
            getInventory().getViewers().forEach(viewer -> {
                viewer.getScheduler().run(plugin, (e) -> viewer.closeInventory(), null);
            });
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                getInventory().close();
            });
        }

    }

    // Open/Close/Click wiring

    /**
     * Set a handler invoked when the inventory is opened.
     *
     * @param openAction open handler
     */
    @Override
    public void setOnOpen(GuiOpenInterface openAction) {
        this.onOpenAction = openAction;
    }

    /**
     * Set a handler invoked when the inventory is closed.
     *
     * @param closeAction close handler
     */
    @Override
    public void setOnClose(GuiCloseInterface closeAction) {
        this.onCloseAction = closeAction;
    }

    /**
     * Set a handler invoked on clicks inside the top (GUI) inventory.
     *
     * @param onClick click handler
     */
    @Override
    public void setOnClick(GuiClickInterface onClick) {
        this.onClickAction = onClick;
    }

    /**
     * Set a handler invoked on clicks in the player's own inventory while this GUI is open.
     *
     * @param onClick own-inventory click handler
     */
    @Override
    public void setOnClickOwn(GuiOwnClickInterface onClick) {
        this.onClickActionOwn = onClick;
    }

    /**
     * Set a handler invoked after a page switch occurs.
     *
     * @param onPageSwitch page switch handler
     */
    @Override
    public void setOnPageSwitch(GuiPageSwitchInterface onPageSwitch) {
        this.onPageSwitchAction = onPageSwitch;
    }

    /**
     * Set the (raw) name used as the inventory title.
     *
     * @param newName the new raw title
     */
    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Get the colored title used for the inventory (translates {@literal &} codes).
     *
     * @return colored title
     */
    @Override
    public String getName() {
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    /**
     * Get the uncolored, raw title value.
     *
     * @return raw title
     */
    @Override
    public String getRawName() {
        return name;
    }

    /**
     * Get the inventory size in slots.
     *
     * @return rows * 9
     */
    @Override
    public int getSize() {
        return rows * 9;
    }

    /**
     * Get the number of allocated pages.
     *
     * @return total pages mapping currently created
     */
    @Override
    public int getPages() {
        return pageMapping.size();
    }

    /**
     * Get the zero-based index of the current page.
     *
     * @return current page index
     */
    @Override
    public int getCurrentPageIndex() {
        return currentOpenedPage;
    }

    /**
     * Get the one-based current page number.
     *
     * @return current page number
     */
    @Override
    public int getCurrentPage() {
        return currentOpenedPage + 1;
    }

    // Slot management

    /**
     * Set a button at the given slot in the current page.
     *
     * @param slot   slot index
     * @param button button instance
     */
    @Override
    public void setSlot(int slot, GuiButtonInterface button) {
        setSlot(currentOpenedPage, slot, button);
    }

    /**
     * Set the same button to the given slot across all pages.
     *
     * @param slot   slot index
     * @param button button instance to reuse
     */
    @Override
    public void setAllPageSlot(int slot, GuiButtonInterface button) {
        GuiButtonInterface btn = button;
        for (int i = 0; i < totalPages; i++) {
            setSlot(i, slot, btn);
        }
    }

    /**
     * Set the same ItemStack to the given slot across all pages.
     *
     * @param slot slot index
     * @param item itemstack to place
     */
    @Override
    public void setAllPageSlot(int slot, ItemStack item) {
        for (int i = 0; i < totalPages; i++) {
            setSlot(i, slot, item);
        }
    }

    /**
     * Set the same Material to the given slot across all pages.
     *
     * @param slot slot index
     * @param item material to place (amount 1)
     */
    @Override
    public void setAllPageSlot(int slot, Material item) {
        for (int i = 0; i < totalPages; i++) {
            setSlot(i, slot, item);
        }
    }

    /**
     * Set an ItemStack at the given slot in the current page.
     *
     * @param slot slot index
     * @param item itemstack to place
     */
    @Override
    public void setSlot(int slot, ItemStack item) {
        setSlot(currentOpenedPage, slot, new GuiButton(item));
    }

    /**
     * Set a Material at the given slot in the current page.
     *
     * @param slot slot index
     * @param item material to place (amount 1)
     */
    @Override
    public void setSlot(int slot, Material item) {
        setSlot(currentOpenedPage, slot, new GuiButton(item));
    }

    /**
     * Set a button at a slot in a specific page.
     *
     * @param page   page index (0-based)
     * @param slot   slot index
     * @param button button to place
     */
    @Override
    public void setSlot(int page, int slot, GuiButtonInterface button) {
        setSlotSafe(page, slot, button);
    }

    /**
     * Internal helper to write slot state and backing Inventory safely.
     *
     * @param page   page index (0-based)
     * @param slot   slot index
     * @param button button to place
     */
    public void setSlotSafe(int page, int slot, GuiButtonInterface button) {
        Map<Integer, GuiButtonInterface> map = pageMapping.get(page);
        Inventory inv = invMapping.get(page);
        if (map != null) map.put(slot, button);
        if (inv != null) inv.setItem(slot, button.getItem());
    }

    /**
     * Set an ItemStack at a slot in a specific page.
     *
     * @param page   page index (0-based)
     * @param slot   slot index
     * @param button itemstack to place
     */
    @Override
    public void setSlot(int page, int slot, ItemStack button) {
        setSlot(page, slot, new GuiButton(button));
    }

    /**
     * Set a Material at a slot in a specific page.
     *
     * @param page   page index (0-based)
     * @param slot   slot index
     * @param button material to place (amount 1)
     */
    @Override
    public void setSlot(int page, int slot, Material button) {
        setSlot(page, slot, new GuiButton(new ItemStack(button)));
    }

    /**
     * Update the ItemStack in a slot on the current page.
     *
     * @param slot slot index
     * @param item new itemstack
     */
    @Override
    public void updateSlot(int slot, ItemStack item) {
        updateSlot(currentOpenedPage, slot, item);
    }

    /**
     * Update the Material in a slot on the current page.
     *
     * @param slot slot index
     * @param item new material (amount 1)
     */
    @Override
    public void updateSlot(int slot, Material item) {
        updateSlot(currentOpenedPage, slot, new ItemStack(item));
    }

    /**
     * Update the ItemStack in a slot on a specific page.
     *
     * @param page page index (0-based)
     * @param slot slot index
     * @param item new itemstack
     */
    @Override
    public void updateSlot(int page, int slot, ItemStack item) {
        Map<Integer, GuiButtonInterface> map = pageMapping.get(page);
        if (map == null) return;
        GuiButtonInterface old = map.get(slot);
        if (old == null) return;
        old.setItem(item);
        setSlot(page, slot, old);
    }

    /**
     * Update the Material in a slot on a specific page.
     *
     * @param page page index (0-based)
     * @param slot slot index
     * @param item new material (amount 1)
     */
    @Override
    public void updateSlot(int page, int slot, Material item) {
        updateSlot(page, slot, new ItemStack(item));
    }

    /**
     * Get the button at a slot in the current page.
     *
     * @param slot slot index
     * @return button or null
     */
    @Override
    public GuiButtonInterface getSlot(int slot) {
        return getSlot(currentOpenedPage, slot);
    }

    /**
     * Get the button at a slot in a specific page.
     *
     * @param page page index (0-based)
     * @param slot slot index
     * @return button or null
     */
    @Override
    public GuiButtonInterface getSlot(int page, int slot) {
        Map<Integer, GuiButtonInterface> map = pageMapping.get(page);
        if (map == null) return null;
        return map.get(slot);
    }

    /**
     * Clear the button at a slot in the current page.
     *
     * @param slot slot index
     */
    @Override
    public void clearSlot(int slot) {
        clearSlot(currentOpenedPage, slot);
    }

    /**
     * Clear the button at a slot in a specific page.
     *
     * @param page page index (0-based)
     * @param slot slot index
     */
    @Override
    public void clearSlot(int page, int slot) {
        Map<Integer, GuiButtonInterface> map = pageMapping.get(page);
        Inventory inv = invMapping.get(page);
        if (map != null) map.remove(slot);
        if (inv != null) inv.setItem(slot, null);
    }

    /**
     * Clear all buttons from the current page.
     */
    @Override
    public void clearAllSlots() {
        clearAllSlots(currentOpenedPage);
    }

    /**
     * Clear all buttons from a specific page.
     *
     * @param page page index (0-based)
     */
    @Override
    public void clearAllSlots(int page) {
        Map<Integer, GuiButtonInterface> map = pageMapping.get(page);
        if (map != null) map.clear();
        Inventory inv = invMapping.get(page);
        if (inv != null) inv.clear();
    }

    /**
     * Get the owning plugin instance.
     *
     * @return owner plugin
     */
    @Override
    public JavaPlugin getOwner() {
        return plugin;
    }

    // Locking

    /**
     * Unlock a slot on the current page, allowing item pickup/move.
     *
     * @param slot slot index
     */
    @Override
    public void unlockButton(int slot) {
        unlockButton(currentOpenedPage, slot);
    }

    /**
     * Unlock a slot on a specific page, allowing item pickup/move.
     *
     * @param page page index (0-based)
     * @param slot slot index
     */
    @Override
    public void unlockButton(int page, int slot) {
        if (slot >= 0 && slot < getSize()) {
            Set<Integer> set = unlockedSlots.get(page);
            if (set != null) set.add(slot);
        }
    }

    /**
     * Lock a slot on the current page, disallowing item pickup/move.
     *
     * @param slot slot index
     */
    @Override
    public void lockButton(int slot) {
        lockButton(currentOpenedPage, slot);
    }

    /**
     * Lock a slot on a specific page, disallowing item pickup/move.
     *
     * @param page page index (0-based)
     * @param slot slot index
     */
    @Override
    public void lockButton(int page, int slot) {
        if (slot >= 0 && slot < getSize()) {
            Set<Integer> set = unlockedSlots.get(page);
            if (set != null) set.remove(slot);
        }
    }

    /**
     * Check if a slot in the current page is locked.
     *
     * @param slot slot index
     * @return true if locked; false if unlocked
     */
    @Override
    public boolean isButtonLocked(int slot) {
        Set<Integer> set = unlockedSlots.get(currentOpenedPage);
        return set != null && set.contains(slot);
    }

    // Open/Switch/Close

    /**
     * Open the GUI to a player at page 0.
     *
     * @param player target player
     */
    @Override
    public void open(Player player) {
        switchPage(0, player);
    }

    /**
     * Open the GUI to a player at a specific page.
     *
     * @param page   page index (0-based)
     * @param player target player
     */
    @Override
    public void open(int page, Player player) {
        switchPage(page, player);
    }

    /**
     * Switch the currently viewed page for a player.
     * Adds paginator controls if configured and plays an optional page switch sound.
     *
     * @param pageIndex target page index (0-based)
     * @param player    the player viewing the GUI
     * @throws PageOutOfBoundException if the index is negative or exceeds max pages
     */
    @Override
    public void switchPage(int pageIndex, Player player) {
        if (pageIndex > totalPages - 1 || pageIndex < 0) {
            throw new PageOutOfBoundException();
        }

        int oldPage = currentOpenedPage;
        currentOpenedPage = pageIndex;

        if (paginator) {
            if (pageIndex > 0 && previousPageButton != null) {
                int slot = 3 + ((rows - 1) * 9);
                setSlot(currentOpenedPage, slot,
                        new GuiButton(previousPageButton).withListener(e -> switchPage(pageIndex - 1, player)));
            }
            if (pageIndex < totalPages - 1 && nextPageButton != null) {
                int slot = 5 + ((rows - 1) * 9);
                setSlot(currentOpenedPage, slot,
                        new GuiButton(nextPageButton).withListener(e -> switchPage(pageIndex + 1, player)));
            }
        }

        Inventory inv = invMapping.get(currentOpenedPage);
        if (inv == null) return;

        // Copy sticky slots from page 0
        for (Integer s : stickySlots) {
            Map<Integer, GuiButtonInterface> firstPage = pageMapping.get(0);
            if (firstPage == null) continue;
            GuiButtonInterface btn = firstPage.get(s);
            if (btn == null) continue;
            setSlot(pageIndex, s, btn);
        }

        if (XaGui.isPaper()) {
            if (XaGui.isFolia()) {
                player.getScheduler().run(plugin, (e) -> player.openInventory(inv), null);
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(inv));
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv));
        }

        if (pageIndex != oldPage && this.pageSwitchSound != null) {
            player.playSound(player, this.pageSwitchSound, 1f, 1f);
        }

        if (this.onPageSwitchAction != null) {
            this.onPageSwitchAction.onPageSwitch(new GuiPageSwitchModel(player, pageIndex, oldPage));
        }
    }

    /**
     * Get the maximum pages currently allocated.
     *
     * @return total page count
     */
    @Override
    public int getMaxPages() {
        return totalPages;
    }

    /**
     * Mark a slot as sticky so it is displayed on every page.
     *
     * @param slot slot index
     */
    @Override
    public void stickSlot(int slot) {
        stickySlots.add(slot);
    }

    /**
     * Unmark a previously sticky slot.
     *
     * @param slot slot index
     */
    @Override
    public void unStickSlot(int slot) {
        stickySlots.remove(slot);
    }

    /**
     * Close the GUI for a player on the main server thread.
     *
     * @param player the player to close for
     */
    @Override
    public void close(Player player) {
        if (XaGui.isFolia()) {
            player.getScheduler().run(plugin, (e) -> player.closeInventory(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
        }
    }

    // Fillers

    /**
     * Fill the given slots on the current page with an ItemStack.
     *
     * @param item  itemstack to place
     * @param slots varargs of slot indices
     */
    @Override
    public void fillSlots(ItemStack item, int... slots) {
        int page = currentOpenedPage;
        for (int s : slots) {
            setSlot(page, s, new GuiButton(item));
        }
    }

    /**
     * Fill the given slots on the current page with an ItemStack.
     *
     * @param item  itemstack to place
     * @param slots array of slot indices
     */
    @Override
    public void fillSlots(ItemStack item, Integer[] slots) {
        int page = currentOpenedPage;
        for (int s : slots) {
            setSlot(page, s, new GuiButton(item));
        }
    }

    /**
     * Fill the given slots on the current page with an ItemStack.
     *
     * @param item  itemstack to place
     * @param slots set of slot indices
     */
    @Override
    public void fillSlots(ItemStack item, Set<Integer> slots) {
        int page = currentOpenedPage;
        for (int s : slots) {
            setSlot(page, s, new GuiButton(item));
        }
    }

    /**
     * Fill the given slots on the current page with an ItemStack.
     *
     * @param item  itemstack to place
     * @param slots list of slot indices
     */
    @Override
    public void fillSlots(ItemStack item, List<Integer> slots) {
        int page = currentOpenedPage;
        for (int s : slots) {
            setSlot(page, s, new GuiButton(item));
        }
    }

    /**
     * Fill the given slots on the current page with a button instance.
     *
     * @param item  button instance
     * @param slots varargs of slot indices
     */
    @Override
    public void fillSlots(GuiButtonInterface item, int... slots) {
        int page = currentOpenedPage;
        for (int s : slots) {
            setSlot(page, s, item);
        }
    }

    /**
     * Fill the given slots on a specific page with an ItemStack.
     *
     * @param page  page index (0-based)
     * @param item  itemstack to place
     * @param slots varargs of slot indices
     */
    @Override
    public void fillSlots(int page, ItemStack item, int... slots) {
        for (int s : slots) {
            setSlot(page, s, new GuiButton(item));
        }
    }

    /**
     * Fill the given slots on a specific page with a button.
     *
     * @param page  page index (0-based)
     * @param item  button instance
     * @param slots varargs of slot indices
     */
    @Override
    public void fillSlots(int page, GuiButtonInterface item, int... slots) {
        for (int s : slots) {
            setSlot(page, s, item);
        }
    }

    // Close button

    /**
     * Add a default close button to page 0 (center of bottom row).
     */
    @Override
    public void addCloseButton() {
        addCloseButton(0, XaGui.getCloseButton());
    }

    /**
     * Add a default close button to all pages (center of bottom row).
     */
    @Override
    public void addCloseButtonAllPages() {
        for (int i = 0; i < totalPages; i++) {
            addCloseButton(i, XaGui.getCloseButton());
        }
    }

    /**
     * Add a close button (ItemStack) to a specific page at the default close-slot.
     *
     * @param page   page index (0-based)
     * @param button itemstack to use
     */
    @Override
    public void addCloseButton(int page, ItemStack button) {
        addCloseButton(page, new GuiButton(button).withListener(e -> e.getWhoClicked().closeInventory()));
    }

    /**
     * Add a close button (GuiButtonInterface) to a specific page at the default close-slot.
     *
     * @param page   page index (0-based)
     * @param button button instance to use
     */
    @Override
    public void addCloseButton(int page, GuiButtonInterface button) {
        int row = rows - 1;
        int middle = 4;
        int slot = row * 9 + middle;
        setSlot(page, slot, button);
    }

    // Pagination controls

    /**
     * Enable paginator controls using the globally configured XaGui next/previous items.
     */
    @Override
    public void addPaginator() {
        this.nextPageButton = XaGui.getNextPageButton();
        this.previousPageButton = XaGui.getPreviousPageButton();
        this.paginator = true;
    }

    /**
     * Set the item used for the "next page" control.
     *
     * @param item itemstack for next page
     */
    @Override
    public void setNextPageButton(ItemStack item) {
        this.nextPageButton = item;
    }

    /**
     * Set the item used for the "previous page" control.
     *
     * @param item itemstack for previous page
     */
    @Override
    public void setPreviousPageButton(ItemStack item) {
        this.previousPageButton = item;
    }

    /**
     * Set an optional sound to play when switching pages.
     *
     * @param sound page switch sound (nullable to disable)
     */
    @Override
    public void setPageSwitchSound(Sound sound) {
        this.pageSwitchSound = sound;
    }

    /**
     * Set an optional sound to play when opening the GUI.
     *
     * @param sound open sound (nullable to disable)
     * @param volume sound volume
     */
    @Override
    public void setOpenSound(Sound sound, float volume) {
        this.openSound = sound;
        this.openSoundFloat = volume;
    }

    /**
     * Get the sound played when opening the GUI.
     *
     * @return open sound
     */
    @Override
    public Sound getOpenSound() {
        return openSound;
    }

    /**
     * Get the volume of the sound played when opening the GUI.
     *
     * @return open sound volume
     */
    @Override
    public float getOpenSoundVolume() {
        return openSoundFloat;
    }

    // Borders

    /**
     * Fill the border slots of all pages using the globally configured XaGui border filler.
     */
    @Override
    public void fillBorder() {
        for (int i = 0; i <= totalPages - 1; i++) {
            fillBorder(i, XaGui.getBorderFiller());
        }
    }

    /**
     * Fill the border slots of a specific page with an item.
     *
     * @param page page index (0-based)
     * @param item itemstack to place in border
     */
    @Override
    public void fillBorder(int page, ItemStack item) {
        Set<Integer> slots = new HashSet<>();
        int size = rows * 9;
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= (rows - 1) * 9 || i % 9 == 0 || i % 9 == 8) {
                slots.add(i);
            }
        }
        for (int s : slots) {
            setSlot(page, s, new GuiButton(item));
        }
    }

    /**
     * Fill the border slots of the current page with an item.
     *
     * @param item itemstack to place in border
     */
    @Override
    public void fillBorder(ItemStack item) {
        fillBorder(currentOpenedPage, item);
    }

    /**
     * Fill the border slots of the current page with a material.
     *
     * @param material material to place (amount 1)
     */
    @Override
    public void fillBorder(Material material) {
        fillBorder(new ItemStack(material));
    }

    // Inventory access + click type policies

    /**
     * Allow or disallow interaction with the player's own inventory while this menu is open.
     *
     * @param value true to allow; false to deny
     */
    @Override
    public void setSelfInventoryAccess(boolean value) {
        this.unlockSelfInventoryClick = value;
    }

    /**
     * Get whether the player's own inventory can be interacted with while this menu is open.
     *
     * @return true if allowed; false otherwise
     */
    @Override
    public boolean getSelfInventoryAccess() {
        return unlockSelfInventoryClick;
    }

    /**
     * Allow only the specified self-inventory click types. If any types are set here,
     * only those will be allowed for the player's own inventory.
     *
     * @param types allowed self-inventory click types
     */
    @Override
    public void allowSelfInventoryClickTypes(ClickType... types) {
        Collections.addAll(allowedClickTypesSelf, types);
    }

    /**
     * Get the allowed self-inventory click types.
     *
     * @return array of allowed click types (empty means not restricted here)
     */
    @Override
    public ClickType[] getAllowedSelfInventoryClickTypes() {
        return allowedClickTypesSelf.toArray(new ClickType[0]);
    }

    /**
     * Add allowed click types for the top (GUI) inventory. If any are configured,
     * a click not present here will be denied.
     *
     * @param types allowed top-inventory click types
     */
    @Override
    public void allowClickTypes(ClickType... types) {
        Collections.addAll(allowedClickTypes, types);
    }

    /**
     * Add blacklisted click types for the top (GUI) inventory. If configured, any
     * click present here will be denied.
     *
     * @param types blacklisted top-inventory click types
     */
    @Override
    public void blacklistClickTypes(ClickType... types) {
        Collections.addAll(blacklistedClickTypes, types);
    }

    /**
     * Get the allowed top-inventory click types.
     *
     * @return array of allowed click types (empty means not restricted here)
     */
    @Override
    public ClickType[] getAllowedClickTypes() {
        return allowedClickTypes.toArray(new ClickType[0]);
    }

    /**
     * Get the blacklisted top-inventory click types.
     *
     * @return array of blacklisted click types
     */
    @Override
    public ClickType[] getBlacklistedClickTypes() {
        return blacklistedClickTypes.toArray(new ClickType[0]);
    }

    // Callback

    /**
     * Execute the configured callback action, if any.
     */
    @Override
    public void callback() {
        if (callbackAction != null) callbackAction.run();
    }

    /**
     * Set a generic callback action that can be triggered via {@link #callback()}.
     *
     * @param callback runnable to execute
     */
    @Override
    public void setCallback(Runnable callback) {
        this.callbackAction = callback;
    }

    // Pages reset

    /**
     * Reset total pages, clearing all current state and rebuilding internal mappings.
     *
     * @param pages new total page count
     */
    @Override
    public void setTotalPages(int pages) {
        this.totalPages = pages;
        pageMapping.clear();
        invMapping.clear();
        unlockedSlots.clear();

        for (int i = 0; i <= totalPages - 1; i++) {
            pageMapping.put(i, new ConcurrentHashMap<>());
            invMapping.put(i, Bukkit.createInventory(this, getSize(), getName()));
            unlockedSlots.put(i, new HashSet<>());
        }
    }

    // Helpers

    /**
     * Create a gray stained glass pane item named with a single space.
     *
     * @return a filler ItemStack
     */
    private static ItemStack grayPaneNamedSpace() {
        return Legacy.createBorderFiller();
    }
}