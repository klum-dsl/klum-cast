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
package com.blackbuild.klum.cast.compiler;

/**
 * Supplies an authorized, complete annotation-addition batch before KlumCast validates one declaration.
 *
 * <p>Register providers with Java's {@link java.util.ServiceLoader} in the AST-transformation class path (the ordinary
 * Groovy compile class path in Gradle). KlumCast discovers providers with the compilation unit's transform loader so
 * that the provider and compiler share this interface's class identity. KlumCast invokes each provider while visiting
 * a declaration during semantic analysis, before it discovers or evaluates constraints.
 * A provider returns additions only; KlumCast attaches them, owns the resulting effective declaration, and performs
 * validation, diagnostic aggregation, and rendering. Providers must not validate annotations themselves, schedule
 * compiler work, or mutate the declaration directly or after this method returns.</p>
 *
 * <p>KlumCast invokes providers once for a declaration's handoff. It attaches every returned batch and validates the
 * resulting declaration once. Later visits of the unchanged declaration do nothing. After that finalization, changing
 * the declaration's annotations is unsupported and rejected rather than reconciled incrementally.</p>
 *
 * <p>Each returned annotation type must be absent from the declaration and from all earlier additions in the handoff.
 * KlumCast rejects duplicates at this boundary, regardless of provider or member values; it never silently merges,
 * replaces, or ignores an addition.</p>
 *
 * <p>This is a deliberately narrow compiler-integration seam. It is not a general transformation scheduler or an
 * observer API.</p>
 */
@FunctionalInterface
public interface EffectiveDeclarationContributor {

    /**
     * Returns the complete addition batch for one declaration handoff.
     *
     * @param declaration read-only view of the declaration being visited
     * @return additions and their origins, or {@link EffectiveDeclarationContribution#none()}
     */
    EffectiveDeclarationContribution contribute(EffectiveDeclaration declaration);
}
