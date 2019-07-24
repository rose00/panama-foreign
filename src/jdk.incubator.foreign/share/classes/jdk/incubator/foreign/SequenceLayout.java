/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.incubator.foreign;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A sequence layout. A sequence layout is used to denote a repetition of a given layout, also called the sequence layout's <em>element layout</em>.
 * The repetition count, where it exists (e.g. for <em>finite</em> sequence layouts) is said to be the the sequence layout's <em>element count</em>.
 * A finite sequence layout can be thought of as a group layout where the sequence layout's element layout is repeated a number of times
 * that is equal to the sequence layout's element count. In other words this layout:
 *
 * <pre>{@code
MemoryLayout.ofSequence(3, MemoryLayout.ofValueBits(32));
 * }</pre>
 *
 * is equivalent to the following layout:
 *
 * <pre>{@code
MemoryLayout.ofStruct(
    MemoryLayout.ofValueBits(32),
    MemoryLayout.ofValueBits(32),
    MemoryLayout.ofValueBits(32));
 * }</pre>
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code SequenceLayout} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 */
public class SequenceLayout extends AbstractLayout {

    private final OptionalLong size;
    private final MemoryLayout elementLayout;

    SequenceLayout(OptionalLong size, MemoryLayout elementLayout, OptionalLong alignment, Map<String, Constable> annotations) {
        super(alignment, annotations);
        this.size = size;
        this.elementLayout = elementLayout;
    }

    /**
     * Computes the layout size, in bits. Since not all sequences have a finite size, this method can throw an exception.
     * @return the layout size (where defined).
     * @throws UnsupportedOperationException if the sequence is unbounded in size (see {@link SequenceLayout#elementsCount()}).
     */
    @Override
    public long bitSize() throws UnsupportedOperationException {
        if (size.isPresent()) {
            return elementLayout.bitSize() * size.getAsLong();
        } else {
            throw new UnsupportedOperationException("Cannot compute size of unbounded sequence");
        }
    }

    @Override
    long naturalAlignmentBits() {
        return elementLayout().bitAlignment();
    }

    /**
     * The element layout associated with this sequence layout.
     * @return The element layout associated with this sequence layout.
     */
    public MemoryLayout elementLayout() {
        return elementLayout;
    }

    /**
     * Returns the element count of this sequence layout (if any).
     * @return the element count of this sequence layout (if any).
     */
    public OptionalLong elementsCount() {
        return size;
    }

    @Override
    public String toString() {
        return decorateLayoutString(String.format("[%s:%s]",
                size.isPresent() ? size.getAsLong() : "", elementLayout));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof SequenceLayout)) {
            return false;
        }
        SequenceLayout s = (SequenceLayout)other;
        return size.equals(s.size) && elementLayout.equals(s.elementLayout);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ size.hashCode() ^ elementLayout.hashCode();
    }

    @Override
    SequenceLayout dup(OptionalLong alignment, Map<String, Constable> annotations) {
        return new SequenceLayout(elementsCount(), elementLayout, alignment, annotations);
    }

    @Override
    public Optional<DynamicConstantDesc<SequenceLayout>> describeConstable() {
        return size.isPresent() ?
                Optional.of(DynamicConstantDesc.ofNamed(ConstantDescs.BSM_INVOKE, "value",
                        CD_SEQUENCE_LAYOUT, MH_SIZED_SEQUENCE, size.getAsLong(), elementLayout.describeConstable().get())) :
                Optional.of(DynamicConstantDesc.ofNamed(ConstantDescs.BSM_INVOKE, "value",
                        CD_SEQUENCE_LAYOUT, MH_UNSIZED_SEQUENCE, elementLayout.describeConstable().get()));
    }

    //hack: the declarations below are to make javadoc happy; we could have used generics in AbstractLayout
    //but that causes issues with javadoc, see JDK-8224052

    /**
     * {@inheritDoc}
     */
    @Override
    public SequenceLayout withName(String name) {
        return (SequenceLayout)super.withName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SequenceLayout withBitAlignment(long alignmentBits) throws IllegalArgumentException {
        return (SequenceLayout)super.withBitAlignment(alignmentBits);
    }
}
