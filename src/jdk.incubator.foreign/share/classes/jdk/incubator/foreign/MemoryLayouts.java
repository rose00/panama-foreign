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

import jdk.internal.foreign.abi.ArgumentClass;
import jdk.internal.foreign.abi.x64.ArgumentClassImpl;

import java.nio.ByteOrder;

/**
 * This class defines useful layout constants. Some of the constants defined in this class are explicit in both
 * size and byte order (see {@link #BITS_64_BE}), and can therefore be used to specify the contents of a memory
 * segment in a fully explicit, unambiguous way. Other constants make an implicit byte order assumptions (see
 * {@link #JAVA_INT}); as such, these constants make it easy to interoperate with other serialization-centric APIs,
 * such as {@link java.nio.ByteBuffer}.
 */
public final class MemoryLayouts {

    private MemoryLayouts() {
        //just the one, please
    }

    /**
     * A value layout constant with size of one byte, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_8_LE = MemoryLayout.ofValueBits(8, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of two bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_16_LE = MemoryLayout.ofValueBits(16, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of four bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_32_LE = MemoryLayout.ofValueBits(32, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of eight bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_64_LE = MemoryLayout.ofValueBits(64, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of one byte, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_8_BE = MemoryLayout.ofValueBits(8, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of two bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_16_BE = MemoryLayout.ofValueBits(16, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of four bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_32_BE = MemoryLayout.ofValueBits(32, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of eight bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_64_BE = MemoryLayout.ofValueBits(64, ByteOrder.BIG_ENDIAN);
    
    /**
     * A padding layout constant with size of one byte.
     */
    public static final MemoryLayout PAD_8 = MemoryLayout.ofPaddingBits(8);
    
    /**
     * A padding layout constant with size of two bytes.
     */
    public static final MemoryLayout PAD_16 = MemoryLayout.ofPaddingBits(16);
    
    /**
     * A padding layout constant with size of four bytes.
     */
    public static final MemoryLayout PAD_32 = MemoryLayout.ofPaddingBits(32);
    
    /**
     * A padding layout constant with size of eight bytes.
     */
    public static final MemoryLayout PAD_64 = MemoryLayout.ofPaddingBits(64);

    /**
     * A value layout constant whose size is the same as that of a Java {@code byte}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_BYTE = BITS_8_BE;

    /**
     * A value layout constant whose size is the same as that of a Java {@code char}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_CHAR = BITS_16_BE;

    /**
     * A value layout constant whose size is the same as that of a Java {@code short}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_SHORT = BITS_16_BE;

    /**
     * A value layout constant whose size is the same as that of a Java {@code int}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_INT = BITS_32_BE;

    /**
     * A value layout constant whose size is the same as that of a Java {@code long}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_LONG = BITS_64_BE;

    /**
     * A value layout constant whose size is the same as that of a Java {@code float}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_FLOAT = BITS_32_BE;

    /**
     * A value layout constant whose size is the same as that of a Java {@code double}, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout JAVA_DOUBLE = BITS_64_BE;

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 SystemV ABI.
     */
    public static final class SysV {
        private SysV() {
            //just the one
        }

        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);


        /**
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);


        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);


        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = C_SCHAR;

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);


        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.SSE);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.SSE);

        /**
         * The {@code long double} native type.
         */
        public static final ValueLayout C_LONGDOUBLE = MemoryLayout.ofValueBits(128, ByteOrder.LITTLE_ENDIAN)
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.X87);

        /**
         * The {@code complex long double} native type.
         */
        public static final GroupLayout C_COMPLEX_LONGDOUBLE = MemoryLayout.ofStruct(C_LONGDOUBLE, C_LONGDOUBLE)
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.COMPLEX_X87);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.POINTER);
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 Windows ABI.
     */
    public static final class WinABI {
        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);
        
        /**
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.INTEGER);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.SSE);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.SSE);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAnnotation(ArgumentClass.ABI_CLASS, ArgumentClassImpl.POINTER);
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the AArch64 ABI.
     */
    public static final class AArch64ABI {
        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_64_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.INTEGER);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.VECTOR);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.VECTOR);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_BE
                .withAnnotation(ArgumentClass.ABI_CLASS, jdk.internal.foreign.abi.aarch64.ArgumentClassImpl.POINTER);
    }
}
