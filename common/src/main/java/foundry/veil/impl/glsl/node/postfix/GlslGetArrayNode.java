package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslAssignableNode;
import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;

import java.util.Collection;
import java.util.List;

public record GlslGetArrayNode(GlslNode expression, GlslNode index) implements GlslAssignableNode {

    @Override
    public void visit(GlslVisitor visitor) {

    }

    @Override
    public Collection<GlslNode> children() {
        return List.of(this.expression, this.index);
    }

    @Override
    public GlslNode toAssignment(Assignment assignment, GlslNode value) {
        return new GlslSetArrayNode(this.expression, this.index, assignment, value);
    }
}
