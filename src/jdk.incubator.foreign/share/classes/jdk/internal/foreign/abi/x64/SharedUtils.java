/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.internal.foreign.abi.x64;

import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.Storage;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;

public class SharedUtils {
    public static final int INTEGER_REGISTER_SIZE = 8;
    public static final int VECTOR_REGISTER_SIZE = 64; // (maximum) vector size is 512 bits
    public static final int X87_REGISTER_SIZE = 16; // x87 register is 128 bits

    public static final int STACK_SLOT_SIZE = 8;

    /**
     * Align the specified type from a given address
     * @return The address the data should be at based on alignment requirement
     */
    public static long align(MemoryLayout t, boolean isVar, long addr) {
        return alignUp(addr, alignment(t, isVar));
    }

    public static long alignUp(long addr, long alignment) {
        return ((addr - 1) | (alignment - 1)) + 1;
    }

    public static long alignDown(long addr, long alignment) {
        return addr & ~(alignment - 1);
    }

    /**
     * The alignment requirement for a given type
     * @param isVar indicate if the type is a standalone variable. This change how
     * array is aligned. for example.
     */
    public static long alignment(MemoryLayout t, boolean isVar) {
        if (t instanceof ValueLayout) {
            return alignmentOfScalar((ValueLayout) t);
        } else if (t instanceof SequenceLayout) {
            // when array is used alone
            return alignmentOfArray((SequenceLayout) t, isVar);
        } else if (t instanceof GroupLayout) {
            return alignmentOfContainer((GroupLayout) t);
        } else if (Utils.isPadding(t)) {
            return 1;
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    private static long alignmentOfScalar(ValueLayout st) {
        return st.byteSize();
    }

    private static long alignmentOfArray(SequenceLayout ar, boolean isVar) {
        if (ar.elementsCount().getAsLong() == 0) {
            // VLA or incomplete
            return 16;
        } else if ((ar.byteSize()) >= 16 && isVar) {
            return 16;
        } else {
            // align as element type
            MemoryLayout elementType = ar.elementLayout();
            return alignment(elementType, false);
        }
    }

    private static long alignmentOfContainer(GroupLayout ct) {
        // Most strict member
        return ct.memberLayouts().stream().mapToLong(t -> alignment(t, false)).max().orElse(1);
    }

    public static class StorageDebugHelper {

        private final String[] integerArgumentRegisterNames;
        private final String[] integerReturnRegisterNames;
        private final String[] x87ReturnRegisterNames;
        private final int maxVectorArgRegisters;
        private final int maxVectorReturnRegisters;

        public StorageDebugHelper(String[] integerArgumentRegisterNames, String[] integerReturnRegisterNames,
                                         String[]  x87ReturnRegisterNames, int maxVectorArgRegisters, int maxVectorReturnRegisters) {
            this.integerArgumentRegisterNames = integerArgumentRegisterNames;
            this.integerReturnRegisterNames = integerReturnRegisterNames;
            this.x87ReturnRegisterNames = x87ReturnRegisterNames;
            this.maxVectorArgRegisters = maxVectorArgRegisters;
            this.maxVectorReturnRegisters = maxVectorReturnRegisters;
        }


        private static String getVectorRegisterName(long index, long size) {
            switch ((int)size) {
                case 8: return "xmm" + index + "_8";
                case 16: return "xmm" + index;
                case 32: return "ymm" + index;
                case 64: return "zmm" + index;
                default: throw new IllegalArgumentException("Illegal vector size: " + size);
            }
        }

        public String getStorageName(Storage storage) {
            switch (storage.getStorageClass()) {
                case INTEGER_ARGUMENT_REGISTER:
                    if (storage.getStorageIndex() > integerArgumentRegisterNames.length) {
                        throw new IllegalArgumentException("Illegal storage: " + storage);
                    }
                    return integerArgumentRegisterNames[(int) storage.getStorageIndex()];

                case VECTOR_ARGUMENT_REGISTER:
                    if (storage.getStorageIndex() > maxVectorArgRegisters) {
                        throw new IllegalArgumentException("Illegal storage: " + storage);
                    }
                    return getVectorRegisterName(storage.getStorageIndex(), storage.getSize());

                case INTEGER_RETURN_REGISTER:
                    if (storage.getStorageIndex() > integerReturnRegisterNames.length) {
                        throw new IllegalArgumentException("Illegal storage: " + storage);
                    }

                    return integerReturnRegisterNames[(int) storage.getStorageIndex()];

                case VECTOR_RETURN_REGISTER:
                    if (storage.getStorageIndex() > maxVectorReturnRegisters) {
                        throw new IllegalArgumentException("Illegal storage: " + storage);
                    }
                    return getVectorRegisterName(storage.getStorageIndex(), storage.getSize());

                case X87_RETURN_REGISTER:
                    if (storage.getStorageIndex() > x87ReturnRegisterNames.length) {
                        throw new IllegalArgumentException("Illegal storage: " + storage);
                    }
                    return x87ReturnRegisterNames[(int) storage.getStorageIndex()];

                case STACK_ARGUMENT_SLOT: return "[sp + " + Long.toHexString(8 * storage.getStorageIndex()) + "]";
            }

            throw new IllegalArgumentException("Unhandled storage type: " + storage.getStorageClass());
        }
    }
}
