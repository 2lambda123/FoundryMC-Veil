package foundry.veil.impl.client.editor;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.imgui.CodeEditor;
import foundry.veil.api.client.imgui.VeilImGuiUtil;
import foundry.veil.api.resource.*;
import foundry.veil.impl.resource.VeilPackResources;
import foundry.veil.impl.resource.VeilResourceManagerImpl;
import foundry.veil.impl.resource.VeilResourceRenderer;
import foundry.veil.impl.resource.tree.VeilResourceFolder;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@ApiStatus.Internal
public class ResourceManagerEditor extends SingleWindowEditor implements VeilEditorEnvironment {

    private VeilResource<?> contextResource;
    private List<? extends VeilResourceAction<?>> actions;

    private final CodeEditor editor;
    private ResourceManager serverResourceManager;
    private CompletableFuture<?> reloadFuture;

    public ResourceManagerEditor() {
        this.editor = new CodeEditor("Save");
    }

    @Override
    public void renderComponents() {
        this.contextResource = null;
        this.actions = Collections.emptyList();

        ImGui.beginDisabled(this.reloadFuture != null && !this.reloadFuture.isDone());
        if (ImGui.button("Reload Resources")) {
            this.reloadFuture = Minecraft.getInstance().reloadResourcePacks();
        }
        ImGui.endDisabled();

        VeilResourceManagerImpl resourceManager = VeilClient.resourceManager();
        if (ImGui.beginListBox("##file_tree", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
            for (VeilPackResources pack : resourceManager.getAllPacks()) {
                String modid = pack.getName();
                int color = VeilImGuiUtil.colorOf(modid);

                boolean open = ImGui.treeNodeEx("##" + modid, ImGuiTreeNodeFlags.SpanAvailWidth);

                ImGui.pushStyleColor(ImGuiCol.Text, color);
                ImGui.sameLine();
                int icon = pack.getTexture();
                if (icon != 0) {
                    float size = ImGui.getTextLineHeight();
                    ImGui.image(icon, size, size);
                } else {
                    VeilImGuiUtil.icon(0xEA7D, color);
                }
                ImGui.sameLine();
                ImGui.text(modid);
                ImGui.popStyleColor();

                if (open) {
                    this.renderFolderContents(pack.getRoot());
                    ImGui.treePop();
                }

                ImGui.separator();
            }
            ImGui.endListBox();
        }

        this.editor.renderWindow();
        if (ImGui.beginPopupModal("###open_failed")) {
            ImGui.text("Failed to open file");
            ImGui.endPopup();
        }
    }

    private void renderFolder(VeilResourceFolder folder) {
        boolean open = ImGui.treeNodeEx("##" + folder.getName(), ImGuiTreeNodeFlags.SpanAvailWidth);
        ImGui.sameLine();
        VeilImGuiUtil.icon(open ? 0xED6F : 0xF43B);
        ImGui.sameLine();
        ImGui.text(folder.getName());

        if (open) {
            this.renderFolderContents(folder);
            ImGui.treePop();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderFolderContents(VeilResourceFolder folder) {
        for (VeilResourceFolder subFolder : folder.getSubFolders()) {
            this.renderFolder(subFolder);
        }

        ImGui.indent();
        for (VeilResource<?> resource : folder.getResources()) {
            VeilResourceInfo info = resource.resourceInfo();
            if (info.hidden()) {
                continue;
            }

            if (ImGui.selectable("##" + resource.hashCode())) {
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
            ImGui.setItemAllowOverlap();
            ImGui.sameLine();
            ImGui.popStyleVar();
            VeilResourceRenderer.renderFilename(resource);

            if (ImGui.beginPopupContextItem("" + resource.hashCode())) {
                if (resource != this.contextResource) {
                    this.contextResource = resource;
                    this.actions = resource.getActions();
                }

                if (ImGui.selectable("##copy_path")) {
                    ImGui.setClipboardText(info.path().toString());
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.setItemAllowOverlap();
                ImGui.sameLine();
                VeilImGuiUtil.icon(0xEB91);
                ImGui.sameLine();
                ImGui.popStyleVar();
                ImGui.text("Copy Path");

                ImGui.beginDisabled(info.isStatic());
                if (ImGui.selectable("##open_folder")) {
                    Path file = info.modResourcePath() != null ? info.modResourcePath() : info.filePath();
                    if (file.getParent() != null) {
                        Util.getPlatform().openFile(file.getParent().toFile());
                    }
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.setItemAllowOverlap();
                ImGui.sameLine();
                VeilImGuiUtil.icon(0xECAF);
                ImGui.sameLine();
                ImGui.popStyleVar();
                ImGui.text("Open in Explorer");
                ImGui.endDisabled();

                for (int i = 0; i < this.actions.size(); i++) {
                    VeilResourceAction action = this.actions.get(i);
                    if (ImGui.selectable("##action" + i)) {
                        action.perform(this, resource);
                    }

                    ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                    ImGui.setItemAllowOverlap();
                    ImGui.sameLine();
                    action.getIcon().ifPresent(icon -> {
                        VeilImGuiUtil.icon(icon);
                        ImGui.sameLine();
                    });
                    ImGui.popStyleVar();
                    ImGui.text(action.getName());
                }

                ImGui.endPopup();
            }
        }
        ImGui.unindent();
    }

    @Override
    public String getDisplayName() {
        return "Resource Browser";
    }

    @Override
    public @Nullable String getGroup() {
        return "Resources";
    }

    @Override
    public void open(VeilResource<?> resource, @Nullable TextEditorLanguageDefinition languageDefinition) {
        VeilResourceInfo info = resource.resourceInfo();
        this.editor.show(info.fileName(), "");
        this.editor.setSaveCallback(null);
        this.editor.getEditor().setReadOnly(true);
        this.editor.getEditor().setColorizerEnable(false);
        VeilResourceManager resourceManager = this.getResourceManager();
        resourceManager.resources(info).getResource(info.path()).ifPresentOrElse(data -> CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = data.open()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.ioPool()).handleAsync((contents, error) -> {
            if (error != null) {
                this.editor.hide();
                ImGui.openPopup("###open_failed");
                Veil.LOGGER.error("Failed to open file", error);
                return null;
            }

            this.editor.show(info.fileName(), contents);

            boolean readOnly = resource.resourceInfo().isStatic();
            this.editor.setSaveCallback((source, errorConsumer) -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        if (readOnly) {
                            throw new IOException("Read-only resource");
                        }

                        Path path = resource.resourceInfo().filePath();
                        try (OutputStream os = Files.newOutputStream(path)) {
                            os.write(source.getBytes(StandardCharsets.UTF_8));
                        }

                        Path buildPath = resource.resourceInfo().modResourcePath();
                        if (buildPath != null) {
                            try (OutputStream os = Files.newOutputStream(buildPath)) {
                                os.write(source.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    } catch (Exception e) {
                        Veil.LOGGER.error("Failed to write resource: {}", resource.resourceInfo().path(), e);
                    }
                }, Util.ioPool()).thenRunAsync(resource::hotReload, Minecraft.getInstance()).exceptionally(e -> {
                    Veil.LOGGER.error("Failed to hot-swap resource: {}", resource.resourceInfo().path(), e);
                    return null;
                });
            });

            TextEditor textEditor = this.editor.getEditor();
            textEditor.setReadOnly(readOnly);
            if (languageDefinition != null) {
                textEditor.setColorizerEnable(true);
                textEditor.setLanguageDefinition(languageDefinition);
            }
            return null;
        }, Minecraft.getInstance()), () -> {
            this.editor.hide();
            ImGui.openPopup("###open_failed");
        });
    }

    @Override
    public VeilResourceManager getResourceManager() {
        return VeilClient.resourceManager();
    }
}