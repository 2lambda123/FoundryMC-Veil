package foundry.veil.impl.glsl.node;

import java.util.Collection;
import java.util.Collections;

public interface GlslNode {

    void visit(GlslVisitor visitor);

    default Collection<GlslNode> children() {
        return Collections.emptySet();
    }
}
