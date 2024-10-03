package foundry.veil.impl.glsl;

import foundry.veil.impl.glsl.node.GlslAssignableNode;
import foundry.veil.impl.glsl.node.GlslNode;
import foundry.veil.impl.glsl.node.GlslTree;
import foundry.veil.impl.glsl.node.GlslVersion;
import foundry.veil.impl.glsl.node.postfix.*;
import foundry.veil.impl.glsl.node.primary.*;
import foundry.veil.impl.glsl.type.GlslSpecifiedType;
import foundry.veil.impl.glsl.type.GlslTypeQualifier;
import foundry.veil.impl.glsl.type.TypeSpecifier;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class GlslParser {

  private GlslParser() {}

  public static GlslTree parse(GlslLexer.Token[] tokens)
      throws GlslSyntaxException {
    TokenReader reader = new TokenReader(tokens);

    GlslVersion version = new GlslVersion(110, true);

    // Try to parse version statements
    GlslLexer.Token token = reader.peek();
    if (token.type() == GlslLexer.TokenType.DIRECTIVE &&
        token.value().startsWith("#version ")) {
      reader.skip();
      String[] parts = token.value().substring(9).split(" +", 2);
      try {
        int ver = Integer.parseInt(parts[0]);
        boolean core = parts.length == 1 || parts[1].equals("core");
        version = new GlslVersion(ver, core);
      } catch (NumberFormatException e) {
        throw reader.error("Invalid Version: " + token.value() + ". " +
                           e.getMessage());
      }
    }

    reader.skipWhitespace();
    while (reader.canRead()) {
      GlslNode node = parseCondition(reader);
      System.out.println(node);
      reader.skipWhitespace();
    }

    return new GlslTree(version);
  }

  private static @Nullable GlslNode parseCondition(TokenReader reader)
      throws GlslSyntaxException {
    if (!reader.canRead()) {
      return null;
    }
    if (reader.peek().type().isType()) {
      GlslSpecifiedType type = parseFullySpecifiedType(reader);
      String name = reader.consume(GlslLexer.TokenType.IDENTIFIER).value();
      reader.consume(GlslLexer.TokenType.EQUAL);
      GlslNode value = parseInitializer(reader);
      return new GlslNewNode(type, name, value);
    }

    return parseExpression(reader);
  }

  private static GlslNode parseInitializer(TokenReader reader)
      throws GlslSyntaxException {
    if (!reader.canRead()) {
      throw reader.error("Expected initializer");
    }

    if (reader.peek().type() == GlslLexer.TokenType.LEFT_BRACE) {
      reader.skip();
      List<GlslNode> expressions = new ArrayList<>();
      expressions.add(parseInitializer(reader));
      while (reader.peek().type() == GlslLexer.TokenType.COMMA) {
        reader.skip();
        if (!reader.canRead()) {
          throw reader.error("Expected initializer or right brace");
        }

        if (reader.peek().type() == GlslLexer.TokenType.RIGHT_BRACE) {
          break;
        }

        expressions.add(parseInitializer(reader));
      }
      reader.consume(GlslLexer.TokenType.RIGHT_BRACE);
      return new GlslInitializerNode(expressions.toArray(GlslNode[] ::new));
    }

    return parseAssignmentExpression(reader);
  }

  private static GlslNode parseExpression(TokenReader reader)
      throws GlslSyntaxException {
    return parseAssignmentExpression(reader);
  }

  private static GlslNode parseAssignmentExpression(TokenReader reader)
      throws GlslSyntaxException {
    if (!reader.canRead()) {
      throw reader.error("Expected assignment expression");
    }

    GlslNode expression = parseConditionalExpression(reader);
    if (reader.canRead() && reader.peek().type().isAssignmentOperator()) {
      if (!(expression instanceof GlslAssignableNode left)) {
        throw reader.error("Invalid left-hand operand");
      }

      GlslAssignableNode.Assignment assignment = switch (reader.peek().type()) {
                case EQUAL -> GlslAssignableNode.Assignment.EQUAL;
                case MUL_ASSIGN -> GlslAssignableNode.Assignment.MUL_ASSIGN;
                case DIV_ASSIGN -> GlslAssignableNode.Assignment.DIV_ASSIGN;
                case MOD_ASSIGN -> GlslAssignableNode.Assignment.MOD_ASSIGN;
                case ADD_ASSIGN -> GlslAssignableNode.Assignment.ADD_ASSIGN;
                case SUB_ASSIGN -> GlslAssignableNode.Assignment.SUB_ASSIGN;
                case LEFT_ASSIGN -> GlslAssignableNode.Assignment.LEFT_ASSIGN;
                case RIGHT_ASSIGN -> GlslAssignableNode.Assignment.RIGHT_ASSIGN;
                case AND_ASSIGN -> GlslAssignableNode.Assignment.AND_ASSIGN;
                case XOR_ASSIGN -> GlslAssignableNode.Assignment.XOR_ASSIGN;
                case OR_ASSIGN -> GlslAssignableNode.Assignment.OR_ASSIGN;
                default -> throw reader.error("Expected assignment operator");
            };
            reader.skip();
            GlslNode value = parseAssignmentExpression(reader);
            return left.toAssignment(assignment, value);
        }

        return expression;
    }

    private static GlslNode parseUnaryExpression(TokenReader reader) throws GlslSyntaxException {
        if (!reader.canRead()) {
            throw reader.error("Expected unary expression");
        }

        switch (reader.peek().type()) {
            case INC_OP -> {
                reader.skip();
                return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.PRE_INCREMENT, parseUnaryExpression(reader));
            }
            case DEC_OP -> {
                reader.skip();
                return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.PRE_DECREMENT, parseUnaryExpression(reader));
            }
            case PLUS -> {
                reader.skip();
                return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.PLUS, parseUnaryExpression(reader));
            }
            case DASH -> {
                reader.skip();
                return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.DASH, parseUnaryExpression(reader));
            }
            case BANG -> {
                reader.skip();
                return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.BANG, parseUnaryExpression(reader));
            }
            case TILDE -> {
                reader.skip();
                return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.TILDE, parseUnaryExpression(reader));
            }
        }

        return parsePostfixExpression(reader);
    }

    private static GlslNode parsePostfixExpression(TokenReader reader) throws GlslSyntaxException {
        if (!reader.canRead()) {
            throw reader.error("Expected postfix expression");
        }

        TypeSpecifier functionIdentifier;
        String varName;
        GlslNode postFixExpression;

        GlslLexer.TokenType type = reader.peek().type();
        if (type.isType()) {
            functionIdentifier = parseTypeSpecifier(reader);
            varName = null;
            postFixExpression = null;
        } else if (type == GlslLexer.TokenType.IDENTIFIER) {
            functionIdentifier = null;
            varName = reader.consume(GlslLexer.TokenType.IDENTIFIER).value();
            postFixExpression = null;
        } else {
            varName = null;
            functionIdentifier = null;
            postFixExpression = switch (type) {
                case UINTEGER_HEXADECIMAL_CONSTANT -> {
                    try {
                        int value = Integer.parseUnsignedInt(reader.consume(GlslLexer.TokenType.UINTEGER_HEXADECIMAL_CONSTANT).value(), 16);
                        yield new GlslUIntConstantNode(GlslIntFormat.HEXADECIMAL, value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case UINTEGER_OCTAL_CONSTANT -> {
                    try {
                        int value = Integer.parseUnsignedInt(reader.consume(GlslLexer.TokenType.UINTEGER_OCTAL_CONSTANT).value(), 8);
                        yield new GlslUIntConstantNode(GlslIntFormat.OCTAL, value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case UINTEGER_DECIMAL_CONSTANT -> {
                    try {
                        int value = Integer.parseUnsignedInt(reader.consume(GlslLexer.TokenType.UINTEGER_DECIMAL_CONSTANT).value());
                        yield new GlslUIntConstantNode(GlslIntFormat.DECIMAL, value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case INTEGER_HEXADECIMAL_CONSTANT -> {
                    try {
                        int value = Integer.parseInt(reader.consume(GlslLexer.TokenType.INTEGER_HEXADECIMAL_CONSTANT).value(), 16);
                        yield new GlslIntConstantNode(GlslIntFormat.HEXADECIMAL, value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case INTEGER_OCTAL_CONSTANT -> {
                    try {
                        int value = Integer.parseInt(reader.consume(GlslLexer.TokenType.INTEGER_OCTAL_CONSTANT).value(), 8);
                        yield new GlslIntConstantNode(GlslIntFormat.OCTAL, value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case INTEGER_DECIMAL_CONSTANT -> {
                    try {
                        int value = Integer.parseInt(reader.consume(GlslLexer.TokenType.INTEGER_DECIMAL_CONSTANT).value());
                        yield new GlslIntConstantNode(GlslIntFormat.DECIMAL, value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case FLOATING_CONSTANT -> {
                    try {
                        float value = Float.parseFloat(reader.consume(GlslLexer.TokenType.FLOATING_CONSTANT).value());
                        yield new GlslFloatConstantNode(value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case BOOL_CONSTANT ->
                        new GlslBoolConstantNode("true".equalsIgnoreCase(reader.consume(GlslLexer.TokenType.BOOL_CONSTANT).value()));
                case DOUBLE -> {
                    try {
                        double value = Double.parseDouble(reader.consume(GlslLexer.TokenType.DOUBLE).value());
                        yield new GlslDoubleConstantNode(value);
                    } catch (NumberFormatException e) {
                        throw reader.error(e.getMessage());
                    }
                }
                case LEFT_PAREN -> {
                    reader.skip();
                    GlslNode expression = parseExpression(reader);
                    reader.consume(GlslLexer.TokenType.RIGHT_PAREN);
                    yield new GlslExpressionNode(expression);
                }
                default -> throw reader.error("Expected postfix expression");
            };
        }

        if (functionIdentifier != null) {
            reader.consume(GlslLexer.TokenType.LEFT_PAREN);
            reader.skip();
            if (!reader.canRead(2)) {
                throw reader.error("Unexpected end of function definition");
            }

            if (reader.peek().type() == GlslLexer.TokenType.VOID) {
                reader.skip();
            } else if (reader.peek().type() != GlslLexer.TokenType.RIGHT_PAREN) {
                List<GlslNode> params = new ArrayList<>();
                params.add(parseConditionalExpression(reader));
                while (reader.canRead() && reader.peek().type() == GlslLexer.TokenType.COMMA) {
                    reader.skip();
                    params.add(parseConditionalExpression(reader));
                }
                reader.consume(GlslLexer.TokenType.RIGHT_PAREN);
                postFixExpression = new GlslFunctionNode(functionIdentifier, params.toArray(GlslNode[]::new));
            } else {
                reader.consume(GlslLexer.TokenType.RIGHT_PAREN);
                postFixExpression = new GlslFunctionNode(functionIdentifier, new GlslNode[0]);
            }
        }
        if (varName != null) {
            postFixExpression = new GlslGetVarNode(varName);
        }

        if (reader.canRead()) {
            GlslLexer.Token token = reader.peek();
            switch (token.type()) {
                case LEFT_BRACKET -> {
                    reader.skip();
                    GlslNode index = parseExpression(reader);
                    reader.consume(GlslLexer.TokenType.RIGHT_BRACKET);
                    return new GlslGetArrayNode(postFixExpression, index);
                }
                case DOT -> {
                    reader.skip();
                    String fieldSelection = reader.consume(GlslLexer.TokenType.IDENTIFIER).value();
                    return new GlslGetFieldNode(postFixExpression, fieldSelection);
                }
                case INC_OP -> {
                    reader.skip();
                    return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.POST_INCREMENT, postFixExpression);
                }
                case DEC_OP -> {
                    reader.skip();
                    return new GlslUnaryExpressionNode(GlslUnaryExpressionNode.Operator.POST_DECREMENT, postFixExpression);
                }
            }
        }
        return postFixExpression;
    }

    private static GlslNode parseConditionalExpression(TokenReader reader) throws GlslSyntaxException {
        if (!reader.canRead()) {
            throw reader.error("Expected expression");
        }

        GlslNode left = parseUnaryExpression(reader);

        return left; // TODO
    }

    private static GlslNode parseMultiplicativeExpression(TokenReader reader) throws GlslSyntaxException {
        if (!reader.canRead()) {
            throw reader.error("Expected expression");
        }

        GlslNode left = parseUnaryExpression(reader);

        return left; // TODO
    }

    private static GlslSpecifiedType parseFullySpecifiedType(TokenReader reader) throws GlslSyntaxException {
        List<GlslTypeQualifier> typeQualifiers = new ArrayList<>();
        while (reader.canRead() && !reader.peek().type().isType()) {
            GlslLexer.Token token = reader.peek();
            GlslLexer.TokenType type = token.type();

            if (type.isStorageQualifier()) {
                if (type == GlslLexer.TokenType.SUBROUTINE) {
                    GlslLexer.Token next = reader.peek(2);
                    if (next != null && next.type() == GlslLexer.TokenType.LEFT_PAREN) {
                        reader.skip(2);
                        List<String> typeNames = new ArrayList<>();
                        typeNames.add(reader.consume(GlslLexer.TokenType.IDENTIFIER).value());

                        while (reader.canRead() && reader.peek().type() == GlslLexer.TokenType.COMMA) {
                            reader.skip();
                            typeNames.add(reader.consume(GlslLexer.TokenType.IDENTIFIER).value());
                        }

                        reader.consume(GlslLexer.TokenType.RIGHT_PAREN);
                        GlslTypeQualifier.storage(typeNames.toArray(String[]::new));
                    }
                } else {
                    GlslTypeQualifier.storage(new String[0]);
                    reader.skip();
                }
            } else if (type.isLayoutQualifier()) {
                reader.skip();
                reader.consume(GlslLexer.TokenType.LEFT_PAREN);

                typeQualifiers.add(parseLayout(reader));
                while (reader.canRead() && reader.peek().type() == GlslLexer.TokenType.COMMA) {
                    reader.skip();
                    typeQualifiers.add(parseLayout(reader));
                }

                reader.consume(GlslLexer.TokenType.RIGHT_PAREN);
            } else if (type.isPrecisionQualifier()) {
                GlslTypeQualifier.Precision precision = switch (type) {
                    case HIGH_PRECISION -> GlslTypeQualifier.Precision.HIGH_PRECISION;
                    case MEDIUM_PRECISION -> GlslTypeQualifier.Precision.MEDIUM_PRECISION;
                    case LOW_PRECISION -> GlslTypeQualifier.Precision.LOW_PRECISION;
                    default -> throw new AssertionError();
                };
                typeQualifiers.add(precision);
                reader.skip();
            } else if (type.isInterpolationQualifier()) {
                GlslTypeQualifier.Interpolation interpolation = switch (type) {
                    case SMOOTH -> GlslTypeQualifier.Interpolation.SMOOTH;
                    case FLAT -> GlslTypeQualifier.Interpolation.FLAT;
                    case NOPERSPECTIVE -> GlslTypeQualifier.Interpolation.NOPERSPECTIVE;
                    default -> throw new AssertionError();
                };
                typeQualifiers.add(interpolation);
                reader.skip();
            } else if (type.isInvariantQualifier()) {
                typeQualifiers.add(GlslTypeQualifier.Invariant.INVARIANT);
                reader.skip();
            } else if (type.isPreciseQualifier()) {
                typeQualifiers.add(GlslTypeQualifier.Precise.PRECISE);
                reader.skip();
            } else {
                break;
            }
        }

        TypeSpecifier type = parseTypeSpecifier(reader);
        return new GlslSpecifiedType(type, typeQualifiers.toArray(GlslTypeQualifier[]::new));
    }

    private static GlslTypeQualifier parseLayout(TokenReader reader) throws GlslSyntaxException {
        if (!reader.canRead()) {
            throw reader.error("Expected IDENTIFIER or SHARED");
        }

        GlslLexer.Token token = reader.peek();
        switch (token.type()) {
            case IDENTIFIER -> {
                reader.skip();
                String identifier = reader.consume(GlslLexer.TokenType.IDENTIFIER).value();
                if (reader.canConsume(GlslLexer.TokenType.EQUAL)) {
                    reader.skip();
                    GlslNode expression = parseConditionalExpression(reader);
                    return GlslTypeQualifier.identifierLayout(identifier, expression);
                } else {
                    return GlslTypeQualifier.identifierLayout(identifier, null);
                }
            }
            case SHARED -> {
                return GlslTypeQualifier.sharedLayout();
            }
            default -> throw reader.error("Expected IDENTIFIER or SHARED, got: " + token);
        }
    }

    private static TypeSpecifier parseTypeSpecifier(TokenReader reader) throws GlslSyntaxException {
        if (!reader.canRead()) {
            throw reader.error("Expected type specifier");
        }
        GlslLexer.Token token = reader.peek();
        if (!token.type().isType()) {
            throw reader.error("Invalid type specifier: " + token.value());
        }
        reader.skip();
        return TypeSpecifier.simple(token);
    }

    private static class TokenReader {

        private final GlslLexer.Token[] tokens;
        private int cursor;

        public TokenReader(GlslLexer.Token[] tokens) {
            this.tokens = tokens;
        }

        public String getString() {
            StringBuilder builder = new StringBuilder();
            for (GlslLexer.Token token : this.tokens) {
                builder.append(token.value());
            }
                  return builder.toString();
                }

                public boolean canRead(int length) {
                  return this.cursor + length <= this.tokens.length;
                }

                public boolean canRead() { return this.canRead(1); }

                public int getCursorOffset() {
                  int offset = 0;
                  for (int i = 0;
                       i <= Math.min(this.cursor, this.tokens.length - 1);
                       i++) {
                    offset += this.tokens[i].value().length();
                  }
                  return offset;
                }

                public @Nullable GlslLexer.Token peek() { return this.peek(0); }

                public @Nullable GlslLexer.Token peek(int amount) {
                  return this.cursor + amount < this.tokens.length
                      ? this.tokens[this.cursor + amount]
                      : null;
                }

                public boolean canConsume(GlslLexer.TokenType token) {
                  return this.canRead() && this.peek().type() == token;
                }

                public GlslLexer.Token consume(GlslLexer.TokenType token)
                    throws GlslSyntaxException {
                  if (!this.canRead() || this.peek().type() != token) {
                    throw this.error("Expected " + token);
                  }
                  this.cursor++;
                  return this.peek(-1);
                }

                public GlslSyntaxException error(String error) {
                  return new GlslSyntaxException(error, this.getString(),
                                                 this.getCursorOffset());
                }

                public void skip() { this.cursor++; }

                public void skip(int amount) { this.cursor += amount; }

                public void skipWhitespace() {
                  while (this.canRead() &&
                         this.peek().type() == GlslLexer.TokenType.COMMENT) {
                    this.skip();
                  }
                }
    }
  }
