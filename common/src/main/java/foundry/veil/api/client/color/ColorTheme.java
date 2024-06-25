package foundry.veil.api.client.color;

import foundry.veil.api.client.color.theme.IThemeProperty;
import foundry.veil.api.client.tooltip.Tooltippable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A color theme is a collection of colors. The colors can be accessed by name. Themes are intended to be used for color schemes.
 * <p>
 * A color theme can be used to apply a color scheme to a {@link Tooltippable} tooltip.
 * Themes can also be used to hold arbitrary color data mapped to strings.
 *
 * @author amo
 */
public class ColorTheme {

    private final Map<String, Color> colors = new HashMap<>();
    private final Map<String, IThemeProperty<?>> properties = new HashMap<>();

    public ColorTheme() {
    }

    public ColorTheme(Color... colors) {
        for (Color color : colors) {
            this.addColor(color);
        }
    }

    public void addProperty(@Nullable String name, IThemeProperty<?> property) {
        this.properties.put(name, property);
    }

    public void addProperty(IThemeProperty<?> property) {
        this.properties.put(null, property);
    }

    public @Nullable Object getAndCastProperty(@Nullable String name) {
        IThemeProperty<?> property = this.properties.get(name);
        return property != null ? property.getType().cast(property) : null;
    }

    public @Nullable IThemeProperty<?> getProperty(@Nullable String name) {
        return this.properties.get(name);
    }

    public void removeProperty(@Nullable String name) {
        this.properties.remove(name);
    }

    public void clearProperties() {
        this.properties.clear();
    }

    public void addColor(@Nullable String name, Color color) {
        this.colors.put(name, color);
    }

    public void addColor(Color color) {
        this.colors.put(null, color);
    }

    public Color getColor(@Nullable String name) {
        return this.colors.get(name);
    }

    public Color getColor() {
        return this.colors.get(null);
    }

    public void removeColor(@Nullable String name) {
        this.colors.remove(name);
    }

    public void removeColor() {
        this.colors.remove(null);
    }

    public void clear() {
        this.colors.clear();
    }

    public List<String> getNames() {
        return this.colors.keySet().stream().filter(Objects::nonNull).toList();
    }

    public List<Color> getColors() {
        return (List<Color>) this.colors.values();
    }

    public Map<String, Color> getColorsMap() {
        return this.colors;
    }
}