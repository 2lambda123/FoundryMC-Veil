package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslAssignableNode;
import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;

import java.util.Collection;
import java.util.List;

public record GlslSetFieldNode(GlslNode expression,
                               String fieldSelection,
                               GlslAssignableNode.Assignment assignment,
                               GlslNode value) implements GlslNode {

    @Override
    public void visit(GlslVisitor visitor) {

    }

    @Override
    public Collection<GlslNode> children() {
        return List.of(this.expression, this.value);
    }
}
