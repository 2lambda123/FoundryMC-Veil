package foundry.veil.impl.client.render.shader.modifier;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class VertexShaderModification extends SimpleShaderModification {

//    public static final Matcher<ExternalDeclaration> INPUT = new Matcher<>("in type name;", ParseShape.EXTERNAL_DECLARATION) {
//        {
//            Root root = this.pattern.getRoot();
//            this.markClassWildcard("type", root.identifierIndex.getUnique("type").getAncestor(TypeSpecifier.class), BuiltinNumericTypeSpecifier.class);
//            this.markClassWildcard("name*", root.identifierIndex.getUnique("name").getAncestor(DeclarationMember.class));
//        }
//    };

    private final Attribute[] attributes;
    private final Map<String, String> mapper;

    public VertexShaderModification(int version, int priority, ResourceLocation[] includes, @Nullable String output, @Nullable String uniform, Function[] functions, Attribute[] attributes) {
        super(version, priority, includes, output, uniform, functions);
        this.attributes = attributes;
        this.mapper = new HashMap<>(this.attributes.length);
    }

//    @Override
//    public void inject(ASTParser parser, TranslationUnit tree, VeilJobParameters parameters) throws IOException {
//        if (this.attributes.length > 0) {
//            Map<Integer, Attribute> validInputs = new Int2ObjectArrayMap<>();
//
//            Root root = tree.getRoot();
//
//            root.processMatches(parser, tree.getChildren().stream().filter(dec -> dec.hasAncestor(INPUT.getPatternClass())), INPUT, externalDeclaration -> {
//                String[] parts = {null, null};
//                ASTWalker.walk(new ASTListener() {
//                    @Override
//                    public void enterTypeSpecifier(TypeSpecifier node) {
//                        parts[0] = ASTPrinter.printSimple(node);
//                    }
//
//                    @Override
//                    public void enterDeclarationMember(DeclarationMember node) {
//                        parts[1] = node.getName().getName();
//                    }
//                }, externalDeclaration);
//                validInputs.put(validInputs.size(), new Attribute(validInputs.size(), parts[0], parts[1]));
//            });
//
//            this.mapper.clear();
//            for (Attribute attribute : this.attributes) {
//                Attribute sourceAttribute = validInputs.get(attribute.index);
//                if (sourceAttribute == null) {
//                    // TODO this might be messed up on mac. It needs to be tested
//                    tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS, "layout(location = " + attribute.index + ") in " + attribute.type + " " + attribute.name + ";");
//                    this.mapper.put(attribute.name, attribute.name);
//                    continue;
//                }
//
//                if (!sourceAttribute.type.equals(attribute.type)) {
//                    throw new IOException("Expected attribute " + attribute.index + " to be " + attribute.type + " but was " + sourceAttribute.type);
//                }
//
//                this.mapper.put(attribute.name, sourceAttribute.name);
//            }
//        }
//
//        super.inject(parser, tree, parameters);
//    }

    @Override
    protected String getPlaceholder(String key) {
        String name = this.mapper.get(key);
        return name != null ? name : super.getPlaceholder(key);
    }

    public record Attribute(int index, String type, String name) {
    }
}
