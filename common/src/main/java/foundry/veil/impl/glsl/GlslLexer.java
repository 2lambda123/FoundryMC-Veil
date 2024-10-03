package foundry.veil.impl.glsl;

import com.mojang.brigadier.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public final class GlslLexer {

  public static Token[] createTokens(String input) throws GlslSyntaxException {
    StringReader reader = new StringReader(input);
    List<Token> tokens = new ArrayList<>();

    reader.skipWhitespace();
    while (reader.canRead()) {
      Token token = getToken(reader);
      if (token != null) {
        tokens.add(token);
        reader.skipWhitespace();
        continue;
      }

      throw new GlslSyntaxException("Unknown Token", reader.getString(),
                                    reader.getCursor() + 1);
    }

    return tokens.toArray(Token[] ::new);
  }

  private static @Nullable Token getToken(StringReader reader) {
    String word = reader.getRemaining();
    for (TokenType type : TokenType.values()) {
      Matcher matcher = type.pattern.matcher(word);
      if (matcher.find() && matcher.start() == 0) {
        reader.setCursor(reader.getCursor() + matcher.end());
        return new Token(type, word.substring(0, matcher.end()));
      }
    }

    return null;
  }

  private static @Nullable Token getToken(StringReader reader, TokenType type) {
    String word = reader.getRemaining();
    Matcher matcher = type.pattern.matcher(word);
    if (matcher.find() && matcher.start() == 0) {
      reader.setCursor(reader.getCursor() + matcher.end());
      return new Token(type, word.substring(0, matcher.end()));
    }
    return null;
  }

  public record Token(TokenType type, String value) {
    @Override
    public String toString() {
      return this.type + "[" + this.value + "]";
    }
  }

  private static final int FLAG_TYPE = 1;
  private static final int FLAG_STORAGE_QUALIFIER = 2;
  private static final int FLAG_CONSTANT_EXPRESSION = 4;
  private static final int FLAG_ASSIGNMENT_OP = 8;

  public enum TokenType {
    DIRECTIVE("#.*"),
    GLSL_MACRO("__LINE__|__FILE__|__VERSION__"),
    COMMENT("\\/\\/.*"),
    MULTI_COMMENT("\\/\\*(?:.|[\\n\\r])*\\*\\/|\\/\\/" +
                  "(?:.*\\\\[\\n\\r]?)+[\\n\\r]?(?:.*)"),

    CONST("const", FLAG_STORAGE_QUALIFIER),
    BOOL("bool", FLAG_TYPE),
    FLOAT("float", FLAG_TYPE),
    INT("int", FLAG_TYPE),
    UINT("uint", FLAG_TYPE),
    DOUBLE("double", FLAG_TYPE),

    BVEC2("bvec2", FLAG_TYPE),
    BVEC3("bvec3", FLAG_TYPE),
    BVEC4("bvec4", FLAG_TYPE),
    IVEC2("ivec2", FLAG_TYPE),
    IVEC3("ivec3", FLAG_TYPE),
    IVEC4("ivec4", FLAG_TYPE),
    UVEC2("uvec2", FLAG_TYPE),
    UVEC3("uvec3", FLAG_TYPE),
    UVEC4("uvec4", FLAG_TYPE),
    VEC2("vec2", FLAG_TYPE),
    VEC3("vec3", FLAG_TYPE),
    VEC4("vec4", FLAG_TYPE),

    MAT2("mat2", FLAG_TYPE),
    MAT3("mat3", FLAG_TYPE),
    MAT4("mat4", FLAG_TYPE),
    MAT2X2("mat2x2", FLAG_TYPE),
    MAT2X3("mat2x3", FLAG_TYPE),
    MAT2X4("mat2x4", FLAG_TYPE),
    MAT3X2("mat3x2", FLAG_TYPE),
    MAT3X3("mat3x3", FLAG_TYPE),
    MAT3X4("mat3x4", FLAG_TYPE),
    MAT4X2("mat4x2", FLAG_TYPE),
    MAT4X3("mat4x3", FLAG_TYPE),
    MAT4X4("mat4x4", FLAG_TYPE),

    DVEC2("dvec2", FLAG_TYPE),
    DVEC3("dvec3", FLAG_TYPE),
    DVEC4("dvec4", FLAG_TYPE),
    DMAT2("dmat2", FLAG_TYPE),
    DMAT3("dmat3", FLAG_TYPE),
    DMAT4("dmat4", FLAG_TYPE),
    DMAT2X2("dmat2x2", FLAG_TYPE),
    DMAT2X3("dmat2x3", FLAG_TYPE),
    DMAT2X4("dmat2x4", FLAG_TYPE),
    DMAT3X2("dmat3x2", FLAG_TYPE),
    DMAT3X3("dmat3x3", FLAG_TYPE),
    DMAT3X4("dmat3x4", FLAG_TYPE),
    DMAT4X2("dmat4x2", FLAG_TYPE),
    DMAT4X3("dmat4x3", FLAG_TYPE),
    DMAT4X4("dmat4x4", FLAG_TYPE),

    CENTROID("centroid", FLAG_STORAGE_QUALIFIER),
    IN("in", FLAG_STORAGE_QUALIFIER),
    OUT("out", FLAG_STORAGE_QUALIFIER),
    INOUT("inout", FLAG_STORAGE_QUALIFIER),
    UNIFORM("uniform", FLAG_STORAGE_QUALIFIER),
    PATCH("patch", FLAG_STORAGE_QUALIFIER),
    SAMPLE("sample", FLAG_STORAGE_QUALIFIER),
    BUFFER("buffer", FLAG_STORAGE_QUALIFIER),
    SHARED("shared", FLAG_STORAGE_QUALIFIER),
    COHERENT("cohent", FLAG_STORAGE_QUALIFIER),
    VOLATILE("volatile", FLAG_STORAGE_QUALIFIER),
    RESTRICT("restrict", FLAG_STORAGE_QUALIFIER),
    READONLY("readonly", FLAG_STORAGE_QUALIFIER),
    WRITEONLY("writeonly", FLAG_STORAGE_QUALIFIER),
    NOPERSPECTIVE("noperspective"),
    FLAT("flat"),
    SMOOTH("smooth"),
    LAYOUT("layout"),

    ATOMIC_UINT("atomic_uint", FLAG_TYPE),

    SAMPLER2D("sampler2D", FLAG_TYPE),
    SAMPLER3D("sampler3D", FLAG_TYPE),
    SAMPLERCUBE("samplerCube", FLAG_TYPE),
    SAMPLER2DSHADOW("sampler2DShadow", FLAG_TYPE),
    SAMPLERCUBESHADOW("samplerCubeShadow", FLAG_TYPE),
    SAMPLER2DARRAY("sampler2DArray", FLAG_TYPE),
    SAMPLER2DARRAYSHADOW("sampler2DArrayShadow", FLAG_TYPE),
    ISAMPLER2D("isampler2D", FLAG_TYPE),
    ISAMPLER3D("isampler3D", FLAG_TYPE),
    ISAMPLERCUBE("isamplerCube", FLAG_TYPE),
    ISAMPLER2DARRAY("isampler2DArray", FLAG_TYPE),
    USAMPLER2D("usampler2D", FLAG_TYPE),
    USAMPLER3D("usampler3D", FLAG_TYPE),
    USAMPLERCUBE("usamplerCube", FLAG_TYPE),
    USAMPLER2DARRAY("uSampler2DArray", FLAG_TYPE),

    SAMPLER1D("sampler1D", FLAG_TYPE),
    SAMPLER1DSHADOW("sampler1DShadow", FLAG_TYPE),
    SAMPLER1DARRAY("sampler1DArray", FLAG_TYPE),
    SAMPLER1DARRAYSHADOW("sampler1DArrayShadow", FLAG_TYPE),
    ISAMPLER1D("isampler1D", FLAG_TYPE),
    ISAMPLER1DARRAY("isampler1DArray", FLAG_TYPE),
    USAMPLER1D("usampler1D", FLAG_TYPE),
    USAMPLER1DARRAY("usampler1DArray", FLAG_TYPE),
    SAMPLER2DRECT("sampler2DRect", FLAG_TYPE),
    SAMPLER2DRECTSHADOW("sampler2DRectShadow", FLAG_TYPE),
    ISAMPLER2DRECT("isampler2DRect", FLAG_TYPE),
    USAMPLER2DRECT("usampler2DRect", FLAG_TYPE),

    SAMPLERBUFFER("samplerBuffer", FLAG_TYPE),
    ISAMPLERBUFFER("isamplerBuffer", FLAG_TYPE),
    USAMPLERBUFFER("usamplerBuffer", FLAG_TYPE),
    SAMPLERCUBEARRAY("samplerCubeArray", FLAG_TYPE),
    SAMPLERCUBEARRAYSHADOW("samplerCubeArrayShadow", FLAG_TYPE),
    ISAMPLERCUBEARRAY("isamplerCubeArray", FLAG_TYPE),
    USAMPLERCUBEARRAY("usamplerCubeArray", FLAG_TYPE),
    SAMPLER2DMS("sampler2Dms", FLAG_TYPE),
    ISAMPLER2DMS("isampler2Dms", FLAG_TYPE),
    USAMPLER2DMS("usampler2Dms", FLAG_TYPE),
    SAMPLER2DMSARRAY("sampler2DMSArray", FLAG_TYPE),
    ISAMPLER2DMSARRAY("isampler2DMSArray", FLAG_TYPE),
    USAMPLER2DMSARRAY("usampler2DMSArray", FLAG_TYPE),
    IMAGE2D("image2D", FLAG_TYPE),
    IIMAGE2D("iimage2D", FLAG_TYPE),
    UIMAGE2D("uimage2D", FLAG_TYPE),
    IMAGE3D("image3D", FLAG_TYPE),
    IIMAGE3D("iimage3D", FLAG_TYPE),
    UIMAGE3D("uimage3D", FLAG_TYPE),
    IMAGECUBE("imagecube", FLAG_TYPE),
    IIMAGECUBE("iimageCube", FLAG_TYPE),
    UIMAGECUBE("uimageCube", FLAG_TYPE),
    IMAGEBUFFER("imageBuffer", FLAG_TYPE),
    IIMAGEBUFFER("iimageBuffer", FLAG_TYPE),
    UIMAGEBUFFER("uimageBuffer", FLAG_TYPE),
    IMAGE2DARRAY("image2DArray", FLAG_TYPE),
    IIMAGE2DARRAY("iimage2DArray", FLAG_TYPE),
    UIMAGE2DARRAY("uimage2DArray", FLAG_TYPE),
    IMAGECUBEARRAY("imagecubeArray", FLAG_TYPE),
    IIMAGECUBEARRAY("iimageCubeArray", FLAG_TYPE),
    UIMAGECUBEARRAY("uimageCubeArray", FLAG_TYPE),

    IMAGE1D("image1D", FLAG_TYPE),
    IIMAGE1D("iimage1D", FLAG_TYPE),
    UIMAGE1D("uimage1D", FLAG_TYPE),
    IMAGE1DARRAY("image1DArray", FLAG_TYPE),
    IIMAGE1DARRAY("iimage1DArray", FLAG_TYPE),
    UIMAGE1DARRAY("uimage1DArray", FLAG_TYPE),
    IMAGE2DRECT("image2DRect", FLAG_TYPE),
    IIMAGE2DRECT("iimage2DRect", FLAG_TYPE),
    UIMAGE2DRECT("uimage2DRect", FLAG_TYPE),
    IMAGE2DMS("image2Dms", FLAG_TYPE),
    IIMAGE2DMS("iimage2DMS", FLAG_TYPE),
    UIMAGE2DMS("uimage2DMS", FLAG_TYPE),
    IMAGE2DMSARRAY("image2DMSArray", FLAG_TYPE),
    IIMAGE2DMSARRAY("iimage2DMSArray", FLAG_TYPE),
    UIMAGE2DMSARRAY("uimage2DMSArray", FLAG_TYPE),

    STRUCT("struct"),
    VOID("void", FLAG_TYPE),

    WHILE("while"),
    BREAK("break"),
    CONTINUE("continue"),
    DO("do"),
    ELSE("else"),
    FOR("for"),
    IF("if"),
    DISCARD("discard"),
    RETURN("return"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    SUBROUTINE("subroutine", FLAG_STORAGE_QUALIFIER),

    // TYPE_NAME ??
    FLOATING_CONSTANT(
        "(?:(?:\\d+\\.\\d+|\\d+\\.|\\.\\d+)(?:[eE][+-]?\\d+)?(?:f|F|lf|LF)?)|" +
        "(?:\\d+)(?:\\.|[eE][+-]?\\d+)(?:f|F|lf|LF)?",
        FLAG_CONSTANT_EXPRESSION),
    UINTEGER_HEXADECIMAL_CONSTANT("0[xX][0-9a-fA-F]*[uU]?",
                                  FLAG_CONSTANT_EXPRESSION),
    UINTEGER_OCTAL_CONSTANT("0[0-7]*[uU]?", FLAG_CONSTANT_EXPRESSION),
    UINTEGER_DECIMAL_CONSTANT("[1-9][\\d]*[uU]?", FLAG_CONSTANT_EXPRESSION),
    INTEGER_HEXADECIMAL_CONSTANT("0[xX][0-9a-fA-F]*", FLAG_CONSTANT_EXPRESSION),
    INTEGER_OCTAL_CONSTANT("0[0-7]*", FLAG_CONSTANT_EXPRESSION),
    INTEGER_DECIMAL_CONSTANT("[1-9][\\d]*", FLAG_CONSTANT_EXPRESSION),
    BOOL_CONSTANT("true|false", FLAG_CONSTANT_EXPRESSION),
    // FIELD_SELECTION

    LEFT_OP("<<"),
    RIGHT_OP(">>"),
    INC_OP("\\+\\+"),
    DEC_OP("--"),
    LE_OP("<="),
    GE_OP(">="),
    EQ_OP("=="),
    NE_OP("!="),
    AND_OP("&&"),
    OR_OP("\\|\\|"),
    XOR_OP("\\^\\^"),
    MUL_ASSIGN("\\*="),
    DIV_ASSIGN("\\/="),
    ADD_ASSIGN("\\+="),
    MOD_ASSIGN("%="),
    LEFT_ASSIGN("<<="),
    RIGHT_ASSIGN(">>="),
    AND_ASSIGN("&="),
    XOR_ASSIGN("\\^="),
    OR_ASSIGN("\\|="),
    SUB_ASSIGN("-="),
    LEFT_PAREN("\\("),
    RIGHT_PAREN("\\)"),
    LEFT_BRACKET("\\["),
    RIGHT_BRACKET("\\]"),
    LEFT_BRACE("\\{"),
    RIGHT_BRACE("\\}"),
    DOT("\\."),
    COMMA(","),
    COLON(":"),
    EQUAL("="),
    SEMICOLON(";"),
    BANG("!"),
    DASH("-"),
    TILDE("~"),
    PLUS("\\+"),
    STAR("\\*"),
    SLASH("\\/"),
    PERCENT("%"),
    LEFT_ANGLE("<"),
    RIGHT_ANGLE(">"),
    VERTICAL_BAR("\\|"),
    CARET("\\^"),
    AMPERSAND("&"),
    QUESTION("\\?"),

    INVARIANT("invariant"),
    PRECISE("precise"),
    HIGH_PRECISION("highp"),
    MEDIUM_PRECISION("mediump"),
    LOW_PRECISION("lowp"),
    PRECISION("precision"),

    IDENTIFIER("[_a-zA-Z][\\d_a-zA-Z]*");

    private final Pattern pattern;
    private final int flags;

    TokenType(String regex) { this(regex, 0); }

    TokenType(String regex, int flags) {
      this.pattern = Pattern.compile(regex);
      this.flags = flags;
    }

    public boolean isType() { return (this.flags & FLAG_TYPE) != 0; }

    public boolean isStorageQualifier() {
      return (this.flags & FLAG_STORAGE_QUALIFIER) != 0;
    }

    public boolean isLayoutQualifier() { return this == LAYOUT; }

    public boolean isPrecisionQualifier() {
      return this == HIGH_PRECISION || this == MEDIUM_PRECISION ||
          this == LOW_PRECISION;
    }

    public boolean isInterpolationQualifier() {
      return this == SMOOTH || this == FLAT || this == NOPERSPECTIVE;
    }

    public boolean isInvariantQualifier() { return this == INVARIANT; }

    public boolean isPreciseQualifier() { return this == PRECISE; }

    public boolean isAssignmentOperator() {
      return (this.flags & FLAG_ASSIGNMENT_OP) != 0;
    }

    public boolean isUnaryOperator() {
      return this == PLUS || this == DASH || this == BANG || this == TILDE;
    }

    public boolean isUnaryExpression() {
      if (this == INC_OP || this == DEC_OP || this.isUnaryOperator()) {
        return true;
      }

      // Primary Expression
      if (this == IDENTIFIER || (this.flags & FLAG_CONSTANT_EXPRESSION) != 0 ||
          this == LEFT_PAREN) {
        return true;
      }

      return this.isType();
    }
  }
}
