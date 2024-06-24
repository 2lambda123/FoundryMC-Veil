package foundry.veil.impl.resource.loader;

import foundry.veil.api.resource.VeilResource;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceLoader;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.api.resource.type.FramebufferResource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class FramebufferResourceLoader implements VeilResourceLoader {

    @Override
    public boolean canLoad(PackType packType, ResourceLocation location, @Nullable Path filePath, @Nullable Path modResourcePath) {
        String path = location.getPath();
        return path.startsWith("pinwheel/framebuffers") && path.endsWith(".json");
    }

    @Override
    public VeilResource<?> load(VeilResourceManager resourceManager, ResourceProvider provider, PackType packType, ResourceLocation location, @Nullable Path filePath, @Nullable Path modResourcePath) throws IOException {
        return new FramebufferResource(new VeilResourceInfo(packType, location, filePath, modResourcePath, false));
    }
}
