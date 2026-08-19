package eu.xap3y.xagui.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * Contract for data-driven (contextual) GUI builders.
 * <p>
 * Implementations are expected to adapt or populate a GUI using a context object {@code T}
 * and return a {@link GuiMenuInterface} ready to be shown.
 *
 * @param <T> context type used to (re)build the GUI
 */
public interface VirtualMenuInterface<T> {

    /**
     * Build (or reconfigure) a GUI using the provided context object.
     *
     * @param t non-null context object
     * @return a non-null {@link GuiMenuInterface} representing the final GUI state
     */
    @NotNull GuiMenuInterface build(@NotNull T t);
}