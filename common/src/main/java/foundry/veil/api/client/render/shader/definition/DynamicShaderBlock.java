package foundry.veil.api.client.render.shader.definition;

/**
 * A {@link ShaderBlock} that can be resized.
 *
 * @param <T> The type of object to serialize
 * @author Ocelot
 */
public interface DynamicShaderBlock<T> extends ShaderBlock<T> {

    /**
     * Resizes this shader block to match the new size.
     *
     * @param newSize The size in bytes
     */
    void setSize(long newSize);
}
