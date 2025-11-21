package eu.xap3y.xagui;

import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.interfaces.VirtualMenuInterface;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract helper base for creating type-aware (data-driven) virtual menus.
 * <p>
 * A {@code VirtualMenu<T>} wraps a concrete {@link GuiMenuInterface} instance and optionally
 * provides a data-bound build method ({@link #build(Object)}) that can transform / adapt
 * the menu before it is opened for a player using some contextual object of type {@code T}.
 * <p>
 * Typical usage pattern:
 * <pre>
 * public class ProfileMenu extends VirtualMenu&lt;String&gt; {
 *     public ProfileMenu(XaGui xaGui) {
 *         super("Profile", 3, xaGui); // 3 rows, 1 page by default
 *     }
 *
 *     &#64;Override
 *     public GuiMenuInterface build(@NotNull String name) {
 *         // configure gui buttons based on profile fields
 *         return build(); // returns the already constructed (base) menu
 *     }
 * }
 *
 * // Opening without data (static build):
 * new ProfileMenu(xaGui).open(player);
 *
 * // Opening with contextual data (dynamic build):
 * new ProfileMenu(xaGui).open(player, profileObject);
 * </pre>
 *
 * @param <T> The contextual data type used to (re)build or adapt the GUI before opening.
 */
public abstract class VirtualMenu<T> implements VirtualMenuInterface<T> {

    /**
     * The underlying GUI instance this virtual menu manipulates.
     */
    protected final GuiMenuInterface gui;

    /**
     * Construct a virtual menu with a single-page GUI.
     *
     * @param name     GUI title (supports color codes if downstream implementation does)
     * @param rows     number of rows (1-6 typical in Bukkit inventories)
     * @param instance active {@link XaGui} manager used to create the backing menu
     */
    public VirtualMenu(String name, int rows, XaGui instance) {
        this.gui = instance.createMenu(name, rows);
    }

    /**
     * Construct a virtual menu with a multi-page GUI.
     *
     * @param name     GUI title
     * @param rows     number of rows
     * @param pages    initial number of pages to allocate
     * @param instance active {@link XaGui} manager
     */
    public VirtualMenu(String name, int rows, int pages, XaGui instance) {
        this.gui = instance.createMenu(name, rows, pages);
    }

    /**
     * Wrap an existing {@link GuiMenuInterface} instance.
     *
     * @param gui existing GUI to virtualize
     */
    public VirtualMenu(GuiMenuInterface gui) {
        this.gui = gui;
    }

    /**
     * Open the (static) menu for a player (does not invoke {@link #build(Object)}).
     *
     * @param player target player
     */
    public void open(Player player) {
        gui.open(player);
    }

    /**
     * Open a specific page of the (static) menu for a player (does not invoke {@link #build(Object)}).
     *
     * @param page   zero-based page index
     * @param player target player
     */
    public void open(int page, Player player) {
        gui.open(page, player);
    }

    /**
     * Access the underlying (already constructed) GUI instance without contextual rebuild.
     *
     * @return backing {@link GuiMenuInterface}
     */
    public GuiMenuInterface build() {
        return gui;
    }

    /**
     * Build (or adapt) the GUI instance using a contextual object.
     * <p>
     * Implementations should mutate {@link #gui} as needed and return it.
     *
     * @param ctx context object (non-null)
     * @return the (possibly modified) GUI instance to open
     */
    @Override
    public abstract @NotNull GuiMenuInterface build(@NotNull T ctx);

    /**
     * Build the GUI with a context object and open it for a player.
     *
     * @param player target player
     * @param ctx      contextual data
     */
    public void open(Player player, T ctx) {
        GuiMenuInterface built = build(ctx);
        built.open(player);
    }

    public GuiMenuInterface getGui() {
        return gui;
    }
}