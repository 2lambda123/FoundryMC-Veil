package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;
import foundry.veil.impl.glsl.type.GlslSpecifiedType;
import java.util.Collection;
import java.util.List;

public record GlslNewNode(GlslSpecifiedType type, String name, GlslNode value)
    implements GlslNode {

  @Override
  public void visit(GlslVisitor visitor) {}

  @Override
  public Collection<GlslNode> children() {
    return List.of(this.value);
  }
}
