package fixture.contributor;

import com.blackbuild.klum.cast.compiler.EffectiveAnnotation;
import com.blackbuild.klum.cast.compiler.EffectiveDeclaration;
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContribution;
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContributor;
import fixture.typed.TypedValidated;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;

import java.util.List;

public final class TypedContributor implements EffectiveDeclarationContributor {
    @Override
    public EffectiveDeclarationContribution contribute(EffectiveDeclaration declaration) {
        for (AnnotationNode annotation : declaration.getAnnotations()) {
            if (!annotation.getClassNode().getName().equals(EffectiveRequest.class.getName())) continue;
            AnnotationNode effective = new AnnotationNode(ClassHelper.make(TypedValidated.class));
            effective.setSourcePosition(annotation);
            return new EffectiveDeclarationContribution(List.of(new EffectiveAnnotation(effective,
                    "module-path fixture request @EffectiveRequest", annotation)));
        }
        return EffectiveDeclarationContribution.none();
    }
}
