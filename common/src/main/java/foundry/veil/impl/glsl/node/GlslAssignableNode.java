package foundry.veil.impl.glsl.node;

public interface GlslAssignableNode extends GlslNode {

    GlslNode toAssignment(Assignment assignment, GlslNode value);

    enum Assignment {
        EQUAL,
        MUL_ASSIGN,
        DIV_ASSIGN,
        MOD_ASSIGN,
        ADD_ASSIGN,
        SUB_ASSIGN,
        LEFT_ASSIGN,
        RIGHT_ASSIGN,
        AND_ASSIGN,
        XOR_ASSIGN,
        OR_ASSIGN,
    }
}
