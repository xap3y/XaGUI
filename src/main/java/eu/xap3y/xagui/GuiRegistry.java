package eu.xap3y.xagui;

import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping arbitrary keys to {@link VirtualMenu} instances.
 * <p>
 * Provides:
 * <ul>
 *     <li>Registration of typed virtual menus</li>
 *     <li>Safe retrieval by key + expected data class</li>
 *     <li>Invocation helpers to build and open menus (with or without context)</li>
 * </ul>
 *
 * @param <K> key type used to identify registered menus (e.g. enum, string, UUID)
 */
public class GuiRegistry<K> {
    private final Map<K, MenuWrapper<?>> registry = new HashMap<>();

    /**
     * Register a new virtual menu under a key.
     *
     * @param key     registry key
     * @param guiMenu virtual menu instance
     * @param clazz   expected context class for type-safety checks
     * @param <T>     context type for the virtual menu
     */
    public <T> void register(K key, VirtualMenu<T> guiMenu, Class<T> clazz) {
        registry.put(key, new MenuWrapper<>(guiMenu, clazz));
    }

    /**
     * Retrieve a previously registered virtual menu if the requested context type is compatible.
     *
     * @param key   registry key
     * @param clazz expected context class
     * @param <T>   context type
     * @return the virtual menu cast to the requested type, or {@code null} if not found / incompatible
     */
    public <T> VirtualMenu<T> get(K key, Class<T> clazz) {
        MenuWrapper<?> wrapper = registry.get(key);
        if (wrapper != null && wrapper.clazz.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            VirtualMenu<T> vm = (VirtualMenu<T>) wrapper.guiMenu;
            return vm;
        }
        return null;
    }

    /**
     * Build (static) and open a menu registered under a key (no context object).
     *
     * @param key    registry key
     * @param player target player
     */
    public void invoke(K key, Player player) {
        MenuWrapper<?> wrapper = registry.get(key);
        if (wrapper != null) {
            GuiMenuInterface built = wrapper.guiMenu.build();
            if (built != null) {
                built.open(player);
            }
        }
    }

    /**
     * Build (context-aware) and open a menu registered under a key with a context object.
     *
     * @param key    registry key
     * @param player target player
     * @param o      context object
     * @param clazz  expected class of the context
     * @param <T>    context type
     */
    public <T> void invoke(K key, Player player, T o, Class<T> clazz) {
        VirtualMenu<T> menu = get(key, clazz);
        if (menu != null) {
            GuiMenuInterface built = menu.build(o);
            built.open(player);
        }
    }

    /**
     * Internal wrapper capturing a virtual menu and its associated context class.
     *
     * @param <T> context type
     */
    private static class MenuWrapper<T> {
        final VirtualMenu<T> guiMenu;
        final Class<T> clazz;

        MenuWrapper(VirtualMenu<T> guiMenu, Class<T> clazz) {
            this.guiMenu = guiMenu;
            this.clazz = clazz;
        }
    }
}