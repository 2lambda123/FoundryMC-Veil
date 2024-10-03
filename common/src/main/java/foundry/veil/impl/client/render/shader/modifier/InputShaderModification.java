package foundry.veil.impl.client.render.shader.modifier;

import foundry.veil.impl.client.render.shader.transformer.VeilJobParameters;
import foundry.veil.impl.glsl.node.GlslTree;
import java.io.IOException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InputShaderModification implements ShaderModification {

  private final int priority;
  private final String input;

  public InputShaderModification(int priority, String input) {
    this.priority = priority;
    this.input = input;
  }

  @Override
  public void inject(GlslTree tree, VeilJobParameters parameters)
      throws IOException {
    //        tree.parseAndInjectNodes(parser,
    //        ASTInjectionPoint.BEFORE_DECLARATIONS, this.input.split("\n"));
  }

  @Override
  public int priority() {
    return this.priority;
  }
}
