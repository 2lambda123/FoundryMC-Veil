package foundry.veil.impl.glsl.node.primary;

import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;

public record GlslExpressionNode(GlslNode node) implements GlslNode {

    @Override
    public void visit(GlslVisitor visitor) {

    }
}
