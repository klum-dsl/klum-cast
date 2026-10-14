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

import java.util.List;
import java.util.Objects;

/**
 * One complete, immutable effective-annotation addition batch from a contributor.
 *
 * <p>Every annotation type in this batch must be absent from the declaration and from earlier batch entries. KlumCast
 * rejects duplicate types instead of resolving them from member values or provenance.</p>
 */
public final class EffectiveDeclarationContribution {

    private static final EffectiveDeclarationContribution NONE = new EffectiveDeclarationContribution(List.of());

    private final List<EffectiveAnnotation> annotations;

    /**
     * @param annotations complete ordered addition batch
     */
    public EffectiveDeclarationContribution(List<? extends EffectiveAnnotation> annotations) {
        this.annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations"));
    }

    /** @return a contribution that adds no annotations */
    public static EffectiveDeclarationContribution none() { return NONE; }

    /** @return every annotation added by this complete batch, in provider-selected order */
    public List<EffectiveAnnotation> getAnnotations() { return annotations; }
}
