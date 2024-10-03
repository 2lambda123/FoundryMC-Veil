package foundry.veil.impl.glsl.type;

import java.util.Arrays;

public record GlslSpecifiedType(TypeSpecifier type,
                                GlslTypeQualifier[] qualifiers) {

  @Override
  public String toString() {
    return "GlslSpecifiedType[type=" + this.type +
        ", qualifiers=" + Arrays.toString(this.qualifiers);
  }
}
