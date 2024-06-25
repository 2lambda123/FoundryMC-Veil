package foundry.veil.api.resource;

import net.minecraft.network.chat.Component;

import java.util.OptionalInt;

public interface VeilResourceAction<T extends VeilResource<?>> {

    /**
     * @return The name of the action
     */
    Component getName();

    /**
     * @return A brief description of the action
     */
    Component getDescription();

    /**
     * @return The icon to display for the action
     */
    OptionalInt getIcon();

    /**
     * Performs the action on the specified resource
     */
    void perform(VeilEditorEnvironment environment, T resource);

}
