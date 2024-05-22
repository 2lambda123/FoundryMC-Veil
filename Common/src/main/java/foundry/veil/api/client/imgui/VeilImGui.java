package foundry.veil.api.client.imgui;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

/**
 * Manages the internal ImGui state.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public interface VeilImGui extends NativeResource {

    void begin();

    void end();

    void onGrabMouse();

    void toggle();

    void updateFonts();

    boolean mouseButtonCallback(long window, int button, int action, int mods);

    boolean scrollCallback(long window, double xOffset, double yOffset);

    boolean keyCallback(long window, int key, int scancode, int action, int mods);

    boolean charCallback(long window, int codepoint);

    boolean shouldHideMouse();

    IntList getTypedCharacters();
}
