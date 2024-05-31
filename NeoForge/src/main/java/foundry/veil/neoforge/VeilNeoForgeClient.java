package foundry.veil.neoforge;

import com.google.common.collect.ImmutableList;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.VeilBuiltinPacks;
import foundry.veil.impl.VeilReloadListeners;
import foundry.veil.impl.client.render.VeilUITooltipRenderer;
import foundry.veil.impl.client.render.shader.VeilVanillaShaders;
import foundry.veil.mixin.accessor.RenderStateShardAccessor;
import foundry.veil.neoforge.event.NeoForgeVeilRegisterBlockLayerEvent;
import foundry.veil.neoforge.event.NeoForgeVeilRegisterFixedBuffersEvent;
import foundry.veil.neoforge.event.NeoForgeVeilRendererEvent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

@ApiStatus.Internal
public class VeilNeoForgeClient {

    public static void init(IEventBus modEventBus) {
        VeilClient.init();

        modEventBus.addListener(VeilNeoForgeClient::registerKeys);
        modEventBus.addListener(VeilNeoForgeClient::registerGuiOverlays);
        modEventBus.addListener(VeilNeoForgeClient::registerListeners);
        modEventBus.addListener(VeilNeoForgeClient::registerShaders);
        modEventBus.addListener(VeilNeoForgeClient::addPackFinders);

        ImmutableList.Builder<RenderType> blockLayers = ImmutableList.builder();
        NeoForge.EVENT_BUS.post(new NeoForgeVeilRegisterBlockLayerEvent(renderType -> {
            if (Veil.platform().isDevelopmentEnvironment() && renderType.bufferSize() > RenderType.SMALL_BUFFER_SIZE) {
                Veil.LOGGER.warn("Block render layer '{}' uses a large buffer size: {}. If this is intended you can ignore this message", ((RenderStateShardAccessor) renderType).getName(), renderType.bufferSize());
            }
            blockLayers.add(renderType);
        }));
        NeoForgeRenderTypeStageHandler.setBlockLayers(blockLayers);
    }

    private static void registerListeners(RegisterClientReloadListenersEvent event) {
        VeilClient.initRenderer();
        VeilReloadListeners.registerListeners((type, id, listener) -> event.registerReloadListener(listener));
        ModLoader loader = ModLoader.get();
        loader.postEvent(new NeoForgeVeilRendererEvent(VeilRenderSystem.renderer()));
        loader.postEvent(new NeoForgeVeilRegisterFixedBuffersEvent(NeoForgeRenderTypeStageHandler::register));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(VeilClient.EDITOR_KEY);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), Veil.veilPath("uitooltip"), VeilUITooltipRenderer::renderOverlay);
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            VeilVanillaShaders.registerShaders((id, vertexFormat, loadCallback) -> event.registerShader(new ShaderInstance(event.getResourceProvider(), id, vertexFormat), loadCallback));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO allow pack enabled by default
    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            VeilBuiltinPacks.registerPacks((id, defaultEnabled) -> registerBuiltinPack(event, id));
        }
    }

    private static void registerBuiltinPack(AddPackFindersEvent event, ResourceLocation id) {
        Path resourcePath = ModList.get().getModFileById(Veil.MODID).getFile().findResource("resourcepacks/" + id.getPath());
        Pack pack = Pack.readMetaAndCreate(id.toString(), Component.literal(id.getNamespace() + "/" + id.getPath()), false,
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(String s) {
                        return new PathPackResources(s, resourcePath, false);
                    }

                    @Override
                    public PackResources openFull(String s, Pack.Info info) {
                        return new PathPackResources(s, resourcePath, false);
                    }
                }, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        event.addRepositorySource(packConsumer -> packConsumer.accept(pack));
    }
}
