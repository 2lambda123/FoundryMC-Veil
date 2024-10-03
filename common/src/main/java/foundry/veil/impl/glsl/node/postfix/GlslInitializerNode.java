package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;
import foundry.veil.impl.glsl.node.primary.GlslExpressionNode;
import foundry.veil.impl.glsl.type.TypeSpecifier;
import java.util.Arrays;
import java.util.Collection;

public record GlslInitializerNode(GlslNode[] expressions) implements GlslNode {

  @Override
  public void visit(GlslVisitor visitor) {}

  @Override
  public Collection<GlslNode> children() {
    return Arrays.asList(this.expressions);
  }
}
