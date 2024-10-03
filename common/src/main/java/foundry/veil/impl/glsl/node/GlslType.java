package foundry.veil.impl.glsl.node;

public record GlslType() {

  public enum StorageQualifier {
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
