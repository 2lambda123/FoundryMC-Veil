package foundry.veil.impl.glsl.node.postfix;

import foundry.veil.impl.glsl.node.GlslAssignableNode;
import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslVisitor;
import java.util.Collection;
import java.util.Collections;

public record GlslGetVarNode(String name) implements GlslAssignableNode {

  @Override
  public void visit(GlslVisitor visitor) {}

  @Override
  public GlslNode toAssignment(Assignment assignment, GlslNode value) {
    return new GlslSetVarNode(this.name, assignment, value);
  }
}
