import foundry.veil.impl.glsl.GlslLexer;
import foundry.veil.impl.glsl.GlslParser;
import foundry.veil.impl.glsl.GlslSyntaxException;
import foundry.veil.impl.glsl.node.GlslTree;
import foundry.veil.impl.glsl.node.GlslVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GlslTest {

    @Test
    void testLexer() throws GlslSyntaxException {
        GlslLexer.Token[] tokens = GlslLexer.createTokens("float a = 4e2; // comment");
        Assertions.assertEquals("float a = 4e2 ;\n// comment\n", this.toString(tokens));
    }

    private String toString(GlslLexer.Token[] tokens) {
        StringBuilder build = new StringBuilder();
        for (GlslLexer.Token token : tokens) {
            build.append(token.value());
            if (token.type() == GlslLexer.TokenType.COMMENT || token.type() == GlslLexer.TokenType.SEMICOLON) {
                build.append('\n');
            } else {
                build.append(' ');
            }
        }
        return build.toString();
    }

    @Test
    void testParser() throws GlslSyntaxException {
        GlslLexer.Token[] tokens = GlslLexer.createTokens("#version 330 core\nfloat a = 32.0;");
        GlslTree tree = GlslParser.parse(tokens);
        Assertions.assertEquals(new GlslVersion(330, true), tree.getVersion());
    }

    @Test
    void testPrecision() throws GlslSyntaxException {
        GlslLexer.Token[] tokens = GlslLexer.createTokens("""
                uniform highp float h1;
                highp float h2 = 2.3 * 4.7; // operation and result are highp
                precision
                mediump float m;
                m = 3.7 * h1 * h2; // all operations are highp precision
                h2 = m * h1; // operation is highp precision
                m = h2 - h1; // operation is highp precision
                h2 = m + m; // addition and result at mediump precision
                void f(highp float p);
                f(3.3); // 3.3 will be passed in at highp precision""");
        GlslTree tree = GlslParser.parse(tokens);
        System.out.println(this.toString(tokens));
    }
}
