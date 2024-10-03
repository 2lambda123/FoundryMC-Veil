package foundry.veil.impl.glsl.type;

import foundry.veil.impl.glsl.node.GlslNode;
import org.jetbrains.annotations.Nullable;

public interface GlslTypeQualifier {

  static GlslTypeQualifier storage(Storage.Type type) {
    return new Storage(type, new String[0]);
  }

  static GlslTypeQualifier storage(String[] typeNames) {
    return new Storage(Storage.Type.SUBROUTINE, typeNames);
  }

  static GlslTypeQualifier
  identifierLayout(String identifier, @Nullable GlslNode constantExpression) {
    return new Layout(identifier, constantExpression);
  }

  static GlslTypeQualifier sharedLayout() { return new Layout("shared", null); }

  record Storage(Type type, String[] typeNames) implements GlslTypeQualifier {

    public enum Type {
      CONST,
      IN,
      OUT,
      INOUT,
      CENTROID,
      PATCH,
      SAMPLE,
      UNIFORM,
      BUFFER,
      SHARED,
      COHERENT,
      VOLATILE,
      RESTRICT,
      READONLY,
      WRITEONLY,
      SUBROUTINE
    }
  }

  record Layout(String identifier, @Nullable GlslNode constantExpression)
      implements GlslTypeQualifier {

    public boolean shared() { return "shared".equals(this.identifier); }

    @Override
    public @Nullable GlslNode constantExpression() {
      return this.shared() ? null : this.constantExpression;
    }
  }

  enum Precision implements GlslTypeQualifier {
    HIGH_PRECISION,
    MEDIUM_PRECISION,
    LOW_PRECISION
  }

  enum Interpolation implements GlslTypeQualifier {
    SMOOTH,
    FLAT,
    NOPERSPECTIVE
  }

  enum Invariant implements GlslTypeQualifier { INVARIANT }

  enum Precise implements GlslTypeQualifier { PRECISE }
}
