package foundry.veil.impl.client.render.shader.modifier;

import foundry.veil.impl.client.render.shader.transformer.VeilJobParameters;
import foundry.veil.impl.glsl.node.GlslTree;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@ApiStatus.Internal
public record ReplaceShaderModification(int priority, ResourceLocation veilShader) implements ShaderModification {

    @Override
    public void inject(GlslTree tree, VeilJobParameters parameters) throws IOException {
        throw new UnsupportedEncodingException("Replace modification replaces file");
    }
}
