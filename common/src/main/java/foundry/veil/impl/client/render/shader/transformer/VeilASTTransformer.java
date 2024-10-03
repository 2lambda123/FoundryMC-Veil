//package foundry.veil.impl.client.render.shader.transformer;
//
//import foundry.veil.Veil;
//import foundry.veil.impl.client.render.shader.modifier.ShaderModification;
//import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
//import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
//import io.github.douira.glsl_transformer.ast.query.RootSupplier;
//import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
//
//public class VeilASTTransformer extends ASTTransformer<VeilJobParameters, String> {
//
//    @Override
//    public String transform(RootSupplier rootSupplier, String input) {
//        TranslationUnit tree = this.parseTranslationUnit(rootSupplier, input);
//        VeilJobParameters parameters = this.getJobParameters();
//
//        for (ShaderModification modification : parameters.modifiers()) {
//            try {
//                modification.inject(this, tree, parameters);
//            } catch (Exception e) {
//                Veil.LOGGER.error("Failed to apply modification {} to shader instance {}. Skipping", parameters.modificationManager().getModifierId(modification), parameters.shaderId(), e);
//            }
//        }
//
//        return ASTPrinter.print(this.getPrintType(), tree);
//    }
//}
