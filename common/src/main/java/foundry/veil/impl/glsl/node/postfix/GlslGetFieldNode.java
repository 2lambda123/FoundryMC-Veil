package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslAssignableNode;
import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;
import java.util.Collection;
import java.util.Collections;

public record GlslGetFieldNode(GlslNode expression, String fieldSelection)
    implements GlslAssignableNode {

  @Override
  public void visit(GlslVisitor visitor) {}

  @Override
  public Collection<GlslNode> children() {
    return Collections.singleton(this.expression);
  }

  @Override
  public GlslNode toAssignment(Assignment assignment, GlslNode value) {
    return new GlslSetFieldNode(this.expression, this.fieldSelection,
                                assignment, value);
  }
}
