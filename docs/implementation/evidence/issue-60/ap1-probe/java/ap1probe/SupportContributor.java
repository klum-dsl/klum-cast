/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2026 Stephan Pauxberger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ap1probe;
import com.blackbuild.klum.cast.compiler.EffectiveAnnotation;
import com.blackbuild.klum.cast.compiler.EffectiveDeclaration;
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContribution;
import com.blackbuild.klum.cast.compiler.EffectiveDeclarationContributor;
import com.blackbuild.klum.ast.Required;
import com.blackbuild.klum.ast.Builder;
import com.blackbuild.klum.ast.layer3.AutoCreate;
import com.blackbuild.klum.ast.layer3.Cluster;
import probe.ForeignAnnotation;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import java.util.List;
public final class SupportContributor implements EffectiveDeclarationContributor {
    public EffectiveDeclarationContribution contribute(EffectiveDeclaration declaration) {
        Class<?> annotation = null;
        if (declaration.getTarget() instanceof ClassNode) {
            if (((ClassNode) declaration.getTarget()).getName().equals("ap1candidate.Candidate"))
                annotation = ForeignAnnotation.class;
        } else if (declaration.getTarget() instanceof FieldNode) {
            FieldNode field = (FieldNode) declaration.getTarget();
            if (!field.getDeclaringClass().getName().equals("ap1candidate.Candidate"))
                return EffectiveDeclarationContribution.none();
            if (field.getName().equals("requiredValue")) annotation = Required.class;
            if (field.getName().equals("child")) annotation = AutoCreate.class;
            if (field.getName().equals("children")) annotation = Cluster.class;
        } else if (declaration.getTarget() instanceof MethodNode) {
            MethodNode method = (MethodNode) declaration.getTarget();
            if (method.getDeclaringClass().getName().equals("ap1candidate.Candidate")
                    && method.getName().equals("query")) annotation = Builder.Query.class;
        }
        if (annotation == null) return EffectiveDeclarationContribution.none();
        AnnotationNode added = new AnnotationNode(ClassHelper.make(annotation));
        added.setSourcePosition(declaration.getTarget());
        return new EffectiveDeclarationContribution(List.of(new EffectiveAnnotation(
                added, "KlumCast RC-fix consumer probe", declaration.getTarget())));
    }
}
