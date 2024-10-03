package foundry.veil.impl.glsl.node;

public class GlslTree {

    private GlslVersion version;

    public GlslTree(GlslVersion version) {
        this.version = version;
    }

    public void visit(GlslVisitor visitor) {

    }

    public GlslVersion getVersion() {
        return this.version;
    }

    public void setVersion(GlslVersion version) {
        this.version = version;
    }
}
