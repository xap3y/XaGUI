package eu.xap3y.thepit.manager;

import eu.xap3y.thepit.api.enums.GuiMenuEnum;
import eu.xap3y.thepit.api.gui.VirtualMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GuiRegistery {

    private final Map<GuiMenuEnum, MenuWrapper<?>> registry = new HashMap<>();

    public <T> void register(GuiMenuEnum key, VirtualMenu<T> guiMenu, Class<T> clazz) {
        registry.put(key, new MenuWrapper<>(guiMenu, clazz));
    }

    public <T> VirtualMenu<T> get(GuiMenuEnum key, Class<T> clazz) {
        MenuWrapper<?> wrapper = registry.get(key);

        if (wrapper != null && wrapper.clazz.isAssignableFrom(clazz)) {
            return (VirtualMenu<T>) wrapper.guiMenu;
        }
        return null;
    }

    public void invoke(GuiMenuEnum key, Player player) {
        MenuWrapper<?> wrapper = registry.get(key);

        if (wrapper != null) {
            wrapper.guiMenu.build().open(player);
        }
    }

    public <T> void invoke(GuiMenuEnum key, Player player, T o, Class<T> clazz) {
        VirtualMenu<T> menu = get(key, clazz);
        menu.build(o).open(player);
    }

    private record MenuWrapper<T>(VirtualMenu<T> guiMenu, Class<T> clazz) { }
}

