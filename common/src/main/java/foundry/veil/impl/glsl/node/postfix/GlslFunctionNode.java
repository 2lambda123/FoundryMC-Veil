package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;
import foundry.veil.impl.glsl.type.TypeSpecifier;

public record GlslFunctionNode(TypeSpecifier typeSpecifier, GlslNode[] params) implements GlslNode {

    @Override
    public void visit(GlslVisitor visitor) {

    }
}
