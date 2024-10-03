package foundry.veil.impl.glsl.node.primary;

import foundry.veil.impl.glsl.node.GlslConstantNode;
import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;

public record GlslIntConstantNode(GlslIntFormat format, int value) implements GlslConstantNode {

    @Override
    public void visit(GlslVisitor visitor) {

    }

    @Override
    public Object rawValue() {
        return this.value;
    }
}
