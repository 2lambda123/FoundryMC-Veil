package foundry.veil.impl.glsl.node;

import foundry.veil.impl.glsl.GlslLexer;

public class DeclarationNode implements GlslNode {

    private final GlslLexer.Token type;
    private final String name;

    public DeclarationNode(GlslLexer.Token type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public void visit(GlslVisitor visitor) {

    }

    @Override
    public String toString() {
        return this.type.value() + " " + this.name;
    }
}
