package foundry.veil.impl.client.render.shader;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.CompiledShader;
import foundry.veil.api.client.render.shader.ShaderCompiler;
import foundry.veil.api.client.render.shader.ShaderException;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.api.client.render.shader.processor.*;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * Creates a new shader and compiles each time {@link #compile(ShaderCompiler.Context, int, String)} is called.
 * This should only be used for compiling single shaders.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public class DirectShaderCompiler implements ShaderCompiler {

    private final ResourceProvider provider;
    private final List<ShaderPreProcessor> preProcessors;
    private final List<ShaderPreProcessor> importProcessors;
    private final Set<Integer> shaders;
    private ResourceLocation compilingName;

    public DirectShaderCompiler(@Nullable ResourceProvider provider) {
        this.provider = provider;
        this.preProcessors = new LinkedList<>();
        this.importProcessors = new LinkedList<>();
        this.shaders = new HashSet<>();
    }

    private void validateType(int type) throws ShaderException {
        if (type == GL_COMPUTE_SHADER && !VeilRenderSystem.computeSupported()) {
            throw new ShaderException("Compute is not supported", null);
        }
    }

    private String modifySource(ShaderCompiler.Context context, List<ShaderPreProcessor> preProcessors, Map<String, Integer> uniformBindings, Set<String> dependencies, Set<ResourceLocation> includes, @Nullable ResourceLocation name, String source, int type, boolean sourceFile) throws IOException {
        for (ShaderPreProcessor preProcessor : preProcessors) {
            source = preProcessor.modify(new PreProcessorContext(this, context, uniformBindings, dependencies, includes, name, source, type, sourceFile));
        }
        return source;
    }

    @Override
    public CompiledShader compile(ShaderCompiler.Context context, int type, ResourceLocation id) throws IOException, ShaderException {
        if (this.provider == null) {
            throw new IOException("Failed to read " + ShaderManager.getTypeName(type) + " from " + id + " because no provider was specified");
        }
        this.validateType(type);

        ResourceLocation location = context.sourceSet().getTypeConverter(type).idToFile(id);
        try (Reader reader = this.provider.openAsReader(location)) {
            this.compilingName = id;
            return this.compile(context, type, IOUtils.toString(reader));
        } finally {
            this.compilingName = null;
        }
    }

    @Override
    public CompiledShader compile(ShaderCompiler.Context context, int type, String source) throws IOException, ShaderException {
        this.validateType(type);
        this.preProcessors.forEach(ShaderPreProcessor::prepare);

        Map<String, Integer> uniformBindings = new HashMap<>();
        Set<String> dependencies = new HashSet<>();
        Set<ResourceLocation> includes = new HashSet<>();
        source = this.modifySource(context, this.preProcessors, uniformBindings, dependencies, includes, this.compilingName, source, type, true);

        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            String log = glGetShaderInfoLog(shader);
            if (Veil.VERBOSE_SHADER_ERRORS) {
                log += "\n" + source;
            }
            glDeleteShader(shader); // Delete to prevent leaks
            throw new ShaderException("Failed to compile " + ShaderManager.getTypeName(type) + " shader", log);
        }

        this.shaders.add(shader);
        return new CompiledShader(this.compilingName, shader, Collections.unmodifiableMap(uniformBindings), Collections.unmodifiableSet(dependencies), Collections.unmodifiableSet(includes));
    }

    @Override
    public ShaderCompiler addPreprocessor(ShaderPreProcessor processor, boolean modifyImports) {
        this.preProcessors.add(processor);
        if (modifyImports) {
            this.importProcessors.add(processor);
        }
        return this;
    }

    @Override
    public ShaderCompiler addDefaultProcessors() {
        if (this.provider != null) {
            this.addPreprocessor(new ShaderImportProcessor(this.provider));
        }
        this.addPreprocessor(new ShaderBindingProcessor());
        this.addPreprocessor(new ShaderPredefinitionProcessor(), false);
        this.addPreprocessor(new ShaderVersionProcessor(), false);
        return this;
    }

    @Override
    public void free() {
        this.preProcessors.clear();
        this.shaders.forEach(GL20C::glDeleteShader);
        this.shaders.clear();
    }

    private record PreProcessorContext(DirectShaderCompiler compiler,
                                       ShaderCompiler.Context context,
                                       Map<String, Integer> uniformBindings,
                                       Set<String> dependencies,
                                       Set<ResourceLocation> includes,
                                       @Nullable ResourceLocation name,
                                       String input,
                                       int type,
                                       boolean sourceFile) implements ShaderPreProcessor.Context {

        @Override
        public String modify(@Nullable ResourceLocation name, String source) throws IOException {
            return this.compiler.modifySource(this.context, this.compiler.importProcessors, this.uniformBindings, this.dependencies, this.includes, name, source, this.type, false);
        }

        @Override
        public void addUniformBinding(String name, int binding) {
            this.uniformBindings.put(name, binding);
        }

        @Override
        public void addDefinitionDependency(String name) {
            this.dependencies.add(name);
        }

        @Override
        public void addInclude(ResourceLocation name) {
            this.includes.add(name);
        }

        @Override
        public @Nullable ResourceLocation getName() {
            return this.name;
        }

        @Override
        public String getInput() {
            return this.input;
        }

        @Override
        public int getType() {
            return this.type;
        }

        @Override
        public FileToIdConverter getConverter() {
            return this.context.sourceSet().getTypeConverter(this.getType());
        }

        @Override
        public boolean isSourceFile() {
            return this.sourceFile;
        }

        @Override
        public @Nullable ProgramDefinition getDefinition() {
            return this.context.definition();
        }

        @Override
        public ShaderPreDefinitions getPreDefinitions() {
            return this.context.preDefinitions();
        }
    }
}
