package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;

import java.util.Collection;
import java.util.Collections;

public record GlslUnaryExpressionNode(Operator operator, GlslNode expression) implements GlslNode {

    @Override
    public void visit(GlslVisitor visitor) {

    }

    @Override
    public Collection<GlslNode> children() {
        return Collections.singleton(this.expression);
    }

    public enum Operator {
        PRE_INCREMENT, PRE_DECREMENT, POST_INCREMENT, POST_DECREMENT, PLUS, DASH, BANG, TILDE
    }
}
