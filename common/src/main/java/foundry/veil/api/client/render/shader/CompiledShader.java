package foundry.veil.api.client.render.shader;

import foundry.veil.api.client.render.shader.program.ShaderProgram;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * A shader instance that has additional pre-compiled data.
 * {@link #apply(ShaderProgram)} should be called after this shader is attached to a program.
 *
 * @param sourceFile             The source file this shader was compiled from or <code>null</code> if the shader has no file
 * @param id                     The OpenGL id of the shader. The shader is automatically deleted later
 * @param uniformBindings        The bindings set by the shader
 * @param definitionDependencies The shader pre-definitions this shader is dependent on
 * @param includes               All shader imports included in this file
 * @author Ocelot
 */
public record CompiledShader(@Nullable ResourceLocation sourceFile,
                             int id,
                             Object2IntMap<String> uniformBindings,
                             Set<String> definitionDependencies,
                             Set<ResourceLocation> includes) {

    /**
     * Applies the additional attributes of this shader to the specified program.
     */
    public void apply(ShaderProgram program) {
        this.uniformBindings.forEach(program::setInt);
    }
}