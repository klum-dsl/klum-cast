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
package probe;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public final class ForeignTransform implements ASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        ClassNode target = (ClassNode) nodes[1];
        if (!Boolean.TRUE.equals(target.getNodeMetaData("probe.existing"))
                || !Boolean.TRUE.equals(target.getNodeMetaData("probe.validated"))
                || !Boolean.TRUE.equals(target.getNodeMetaData("probe.named"))
                || !Boolean.TRUE.equals(target.getNodeMetaData("probe.filtered")))
            throw new IllegalStateException("foreign transform ran before effective validation");
        if (target.getDeclaredMethods("probe").size() != 0)
            throw new IllegalStateException("foreign transform ran twice");
        target.addMethod("probe", Modifier.PUBLIC | Modifier.STATIC, ClassHelper.int_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new ReturnStatement(new ConstantExpression(1)));
    }
}
