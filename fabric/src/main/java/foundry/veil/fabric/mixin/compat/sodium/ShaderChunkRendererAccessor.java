package foundry.veil.fabric.mixin.compat.sodium;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ShaderChunkRenderer.class)
public interface ShaderChunkRendererAccessor {

    @Accessor("programs")
    Map<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> getPrograms();
}
