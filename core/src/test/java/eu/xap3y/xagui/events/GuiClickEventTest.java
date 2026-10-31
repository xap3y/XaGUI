package eu.xap3y.xagui.events;

import eu.xap3y.xagui.listeners.MenuListener;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class GuiClickEventTest {

    @Test
    public void resolvesTopBottomAndOutsideClicks() {
        TestView view = new TestView();
        assertSame(view.getTopInventory(), MenuListener.getClickedInventory(click(view, 0)));
        assertSame(view.getTopInventory(), MenuListener.getClickedInventory(click(view, 26)));
        assertSame(view.getBottomInventory(), MenuListener.getClickedInventory(click(view, 27)));
        assertSame(view.getBottomInventory(), MenuListener.getClickedInventory(click(view, 62)));
        assertNull(MenuListener.getClickedInventory(click(view, -999)));
    }

    @Test
    public void wrappedClickPreservesRawSlotAndInventory() {
        TestView view = new TestView();
        InventoryClickEvent original = click(view, 54);
        GuiClickEvent wrapped = new GuiClickEvent(original);
        assertEquals(original.getRawSlot(), wrapped.getRawSlot());
        assertEquals(original.getSlot(), wrapped.getSlot());
        assertSame(view.getBottomInventory(), wrapped.getClickedInventory());
        assertNull(wrapped.getClickedButton());
        assertNull(new GuiClickEvent(click(view, -999)).getClickedInventory());
    }

    private InventoryClickEvent click(InventoryView view, int rawSlot) {
        return new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                rawSlot,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
    }

    public static class TestView extends InventoryView {

        private final Inventory top = inventory(27);
        private final Inventory bottom = inventory(36);

        private static Inventory inventory(int size) {
            return (Inventory) Proxy.newProxyInstance(
                    Inventory.class.getClassLoader(),
                    new Class<?>[]{Inventory.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getSize")) {
                            return size;
                        }
                        if (method.getName().equals("getType")) {
                            return InventoryType.CHEST;
                        }
                        return null;
                    }
            );
        }

        @Override
        public Inventory getTopInventory() {
            return top;
        }

        @Override
        public Inventory getBottomInventory() {
            return bottom;
        }

        @Override
        public HumanEntity getPlayer() {
            return (HumanEntity) Proxy.newProxyInstance(
                    HumanEntity.class.getClassLoader(),
                    new Class<?>[]{HumanEntity.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getGameMode")) {
                            return org.bukkit.GameMode.SURVIVAL;
                        }
                        return null;
                    }
            );
        }

        @Override
        public InventoryType getType() {
            return InventoryType.CHEST;
        }
    }
}
