/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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

/*
 * @test
  * @modules jdk.incubator.foreign/jdk.incubator.foreign.unsafe
 *          jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          java.base/sun.security.action
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm -Djdk.internal.foreign.UpcallHandler.FASTPATH=none TestUpcall
 * @run testng/othervm TestUpcall
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SystemABI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.testng.annotations.*;
import static org.testng.Assert.*;


public class TestUpcall extends CallGeneratorHelper {

    static LibraryLookup lib = LibraryLookup.ofLibrary(MethodHandles.lookup(), "TestDowncall");
    static SystemABI abi = SystemABI.getInstance();

    static MethodHandle DUMMY;

    static {
        try {
            DUMMY = MethodHandles.lookup().findStatic(TestUpcall.class, "dummy", MethodType.methodType(void.class));
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }


    @Test(dataProvider="functions", dataProviderClass=CallGeneratorHelper.class)
    public void testUpcalls(String fName, Ret ret, List<ParamType> paramTypes, List<StructFieldType> fields) throws Throwable {
        List<Consumer<Object>> checks = new ArrayList<>();
        MemoryAddress addr = lib.lookup(fName);
        MethodHandle mh = abi.downcallHandle(addr, methodType(ret, paramTypes, fields), function(ret, paramTypes, fields));
        Object[] args = makeArgs(paramTypes, fields, checks);
        mh = mh.asSpreader(Object[].class, paramTypes.size() + 1);
        Object res = mh.invoke(args);
        if (ret == Ret.NON_VOID) {
            checks.forEach(c -> c.accept(res));
        }
        for (Object arg : args) {
            cleanup(arg);
        }
    }

    static MethodType methodType(Ret ret, List<ParamType> params, List<StructFieldType> fields) {
        MethodType mt = ret == Ret.VOID ?
                MethodType.methodType(void.class) : MethodType.methodType(paramCarrier(params.get(0).layout(fields)));
        for (ParamType p : params) {
            mt = mt.appendParameterTypes(paramCarrier(p.layout(fields)));
        }
        mt = mt.appendParameterTypes(MemoryAddress.class); //the callback
        return mt;
    }

    static FunctionDescriptor function(Ret ret, List<ParamType> params, List<StructFieldType> fields) {
        List<MemoryLayout> paramLayouts = params.stream().map(p -> p.layout(fields)).collect(Collectors.toList());
        paramLayouts.add(C_POINTER); // the callback
        MemoryLayout[] layouts = paramLayouts.toArray(new MemoryLayout[0]);
        return ret == Ret.VOID ?
                FunctionDescriptor.ofVoid(false, layouts) :
                FunctionDescriptor.of(layouts[0], false, layouts);
    }

    static Object[] makeArgs(List<ParamType> params, List<StructFieldType> fields, List<Consumer<Object>> checks) throws ReflectiveOperationException {
        Object[] args = new Object[params.size() + 1];
        for (int i = 0 ; i < params.size() ; i++) {
            args[i] = makeArg(params.get(i).layout(fields), checks, i == 0);
        }
        args[params.size()] = makeCallback(params.size() > 0 ? params.get(0) : null, fields);
        return args;
    }

    @SuppressWarnings("unchecked")
    static MemoryAddress makeCallback(ParamType param, List<StructFieldType> fields) {
        MemoryLayout layout = null;
        if (param != null) {
            layout = param.layout(fields);
        }
        MethodHandle mh = layout != null ?
                MethodHandles.identity(paramCarrier(layout)) :
                DUMMY;
        FunctionDescriptor func = layout != null ?
                FunctionDescriptor.of(layout, false, layout) :
                FunctionDescriptor.ofVoid(false);
        return abi.upcallStub(mh, func);
    }

    static void dummy() {
        //do nothing
    }
}
