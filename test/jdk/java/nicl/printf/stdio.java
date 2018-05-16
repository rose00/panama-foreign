/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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


import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeType;
import java.nicl.types.Pointer;

@NativeHeader(headerPath="/usr/include/stdio.h")
public interface stdio {
    @C(file="/usr/include/stdio.h", line=47, column=11, USR="c:@F@getpid")
    @NativeType(layout="(p:c*)i", ctype="int (const char*, ...)", size=1)
    @CallingConvention(value=1)
    int getpid();

    @C(file="/usr/include/stdio.h", line=47, column=11, USR="c:@F@printf")
    @NativeType(layout="(p:c*)i", ctype="int (const char*, ...)", size=1)
    @CallingConvention(value=1)
    int printf(Pointer<Byte> fmt, Object... args);

    @C(file="/usr/include/stdio.h", line=47, column=11, USR="c:@F@fprintf")
    @NativeType(layout="(pp:c*)i", ctype="int (FILE*, const char*, ...)", size=1)
    @CallingConvention(value=1)
    int fprintf(Pointer<Void> strm, Pointer<Byte> fmt, Object... args);

    @C(file="/usr/include/stdio.h", line=47, column=11, USR="c:@F@fflush")
    @NativeType(layout="(p)i", ctype="int (FILE *stream)", size=1)
    @CallingConvention(value=1)
    int fflush(Pointer<Void> stream);

    @C(file="/usr/include/stdio.h", line=47, column=11, USR="c:@F@fdopen")
    @NativeType(layout="(ip:c)p", ctype="FILE* (int fd, const char* mode)", size=1)
    @CallingConvention(value=1)
    Pointer<Void> fdopen(int fd, Pointer<Byte> mode);
}
