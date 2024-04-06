package foundry.veil.fabric.mixin.client;

import foundry.veil.fabric.FabricRenderTypeStageHandler;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Inject(method = "chunkBufferLayers", at = @At("HEAD"), cancellable = true)
    private static void injectChunkBufferLayers(CallbackInfoReturnable<List<RenderType>> cir) {
        List<RenderType> renderTypes = FabricRenderTypeStageHandler.getBlockLayers();
        if (renderTypes != null) {
            cir.setReturnValue(renderTypes);
        }
    }
}
