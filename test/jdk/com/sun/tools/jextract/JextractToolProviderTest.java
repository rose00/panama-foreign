/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.memory.Pointer;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.spi.ToolProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.jextract
 * @bug 8225630
 * @build JextractToolRunner
 * @run testng/othervm -Duser.language=en JextractToolProviderTest
 */
public class JextractToolProviderTest extends JextractToolRunner {
    @Test
    public void testHelp() {
        run().checkFailure(); // no options
        run("--help").checkSuccess();
        run("-h").checkSuccess();
        run("-?").checkSuccess();
    }

    // error for non-existent header file
    @Test
    public void testNonExistentHeader() {
        run("--dry-run", getInputFilePath("non_existent.h").toString())
            .checkFailure()
            .checkContainsOutput("Cannot open header file");
    }

    @Test
    public void testDryRun() {
        // only dry-run, don't produce any output
        Path simpleJar = getOutputFilePath("simple.jar");
        deleteFile(simpleJar);
        run("--dry-run", getInputFilePath("simple.h").toString()).checkSuccess();
        try {
            assertFalse(Files.isRegularFile(simpleJar));
        } finally {
            deleteFile(simpleJar);
        }
    }

    @Test
    public void testJarOutputFileOption() {
        // simple jar output file check
        Path simpleJar = getOutputFilePath("simple.jar");
        deleteFile(simpleJar);
        run("-o", simpleJar.toString(),
            getInputFilePath("simple.h").toString()).checkSuccess();
        try {
            assertTrue(Files.isRegularFile(simpleJar));
        } finally {
            deleteFile(simpleJar);
        }
    }

    @Test
    public void testJModOutputFileOption() {
        // simple jmod output file check
        Path simpleJmod = getOutputFilePath("simple.jmod");
        deleteFile(simpleJmod);
        // --target-package is mandatory for jmod generation
        run("--target-package", "com.acme",
            "-o", simpleJmod.toString(),
            getInputFilePath("simple.h").toString()).checkSuccess();
        try {
            assertTrue(Files.isRegularFile(simpleJmod));
            ToolProvider jmod = ToolProvider.findFirst("jmod").get();
            int exitCode = jmod.run(System.out, System.err, "list",
                simpleJmod.toString());
            assertTrue(exitCode == 0);
        } finally {
            deleteFile(simpleJmod);
        }
    }

    @Test
    public void testOutputClass() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        run("-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check NativeHeader annotation
            NativeHeader header = cls.getAnnotation(NativeHeader.class);
            assertNotNull(header);
            assertEquals(header.path(), helloH.toString());

            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }

    private void testTargetPackage(String targetPkgOption) {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        run(targetPkgOption, "com.acme", "-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass("com.acme." + headerInterfaceName("hello.h"));
            // check NativeHeader annotation
            NativeHeader header = cls.getAnnotation(NativeHeader.class);
            assertNotNull(header);
            assertEquals(header.path(), helloH.toString());

            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void testTargetPackageOption() {
        testTargetPackage("-t");
    }

    @Test
    public void testTargetPackageLongOption() {
        testTargetPackage("--target-package");
    }

    @Test
    public void testPackageMapping() {
        Path worldJar = getOutputFilePath("world.jar");
        deleteFile(worldJar);
        Path worldH = getInputFilePath("world.h");
        Path include = getInputFilePath("include");
        // world.h include mytypes.h, use appropriate package for stuff from mytypes.h
        run("-I", include.toString(), "--package-map", include.toString() + "=com.acme",
                "-o", worldJar.toString(), worldH.toString()).checkSuccess();
        try(Loader loader = classLoader(worldJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("world.h"));
            Method m = findFirstMethod(cls, "distance");
            Class<?>[] params = m.getParameterTypes();
            assertEquals(params[0].getName(), "com.acme." + structInterfaceName("mytypes.h", "Point"));
        } finally {
            deleteFile(worldJar);
        }
    }

    @Test
    public void test_no_input_files() {
        run("-L", "foo")
                .checkContainsOutput("No input files")
                .checkFailure();
    }

    @Test
    public void test_option_L_without_l() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        Path linkDir = getInputFilePath("libs");
        String warning = "WARNING: -L option specified without any -l option";
        run("-L", linkDir.toString(), "-o", helloJar.toString(), helloH.toString())
            .checkContainsOutput(warning)
            .checkSuccess();
        deleteFile(helloJar);
    }

    @Test
    public void test_option_record_path_without_l() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        Path libDir = getInputFilePath("libs");
        String warning = "WARNING: --record-library-path option specified without any -l option";
        try {
            run("--record-library-path", "-L", libDir.toString(), "-o", helloJar.toString(), helloH.toString())
                    .checkContainsOutput(warning)
                    .checkSuccess();
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void test_option_record_path_no_libs() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        String warning = "WARNING: --record-library-path option specified without any -L option";
        try {
            run("--record-library-path",
                    "-o", helloJar.toString(), helloH.toString())
                .checkContainsOutput(warning)
                .checkSuccess();
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void test_option_l_no_crash_missing_lib() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        String warning = "WARNING: Some library names could not be resolved";
        try {
            run("-L", "nonExistent",
                    "-l", "nonExistent",
                    "-o", helloJar.toString(), helloH.toString())
                    .checkContainsOutput(warning)
                    .checkSuccess();
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void test_option_l() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        run("-l", "hello", "-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check that NativeHeader annotation captures -l value
            NativeHeader header = cls.getAnnotation(NativeHeader.class);
            assertNotNull(header);
            assertEquals(header.libraries().length, 1);
            assertEquals(header.libraries()[0], "hello");
            // no library paths set
            assertEquals(header.libraryPaths().length, 0);
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void test_option_l_and_libpath() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        Path libDir = getInputFilePath("libs");
        run("-l", "hello", "--record-library-path", "-L", libDir.toString(),
                "-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check that NativeHeader annotation captures -l and -L values
            NativeHeader header = cls.getAnnotation(NativeHeader.class);
            assertNotNull(header);
            assertEquals(header.libraries().length, 1);
            assertEquals(header.libraries()[0], "hello");
            assertEquals(header.libraryPaths().length, 1);
            assertEquals(header.libraryPaths()[0], libDir.toString());
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void testUnionDeclaration() {
        Path uniondeclJar = getOutputFilePath("uniondecl.jar");
        deleteFile(uniondeclJar);
        Path uniondeclH = getInputFilePath("uniondecl.h");
        run("-o", uniondeclJar.toString(), uniondeclH.toString()).checkSuccess();
        try(Loader loader = classLoader(uniondeclJar)) {
            Class<?> unionCls = loader.loadClass(headerInterfaceName("uniondecl.h"));
            assertNotNull(unionCls);
            boolean found = Arrays.stream(unionCls.getClasses()).
                map(Class::getSimpleName).
                filter(n -> n.equals("IntOrFloat")).
                findFirst().isPresent();
            assertTrue(found, "uniondecl.IntOrFloat not found");
        } finally {
            deleteFile(uniondeclJar);
        }
    }

    private void testEnumConstGetters(Class<?> enumCls, List<String> names) {
        for (String name : names) {
            if (findEnumConstGet(enumCls, name) == null) {
                throw new RuntimeException(enumCls.getName() + " misses " + name);
            }
        }
    }

    @Test
    public void testAnonymousEnum() {
        Path anonenumJar = getOutputFilePath("anonenum.jar");
        deleteFile(anonenumJar);
        Path anonenumH = getInputFilePath("anonenum.h");
        run("-o", anonenumJar.toString(), anonenumH.toString()).checkSuccess();
        try(Loader loader = classLoader(anonenumJar)) {
            Class<?> anonenumCls = loader.loadClass(headerInterfaceName("anonenum.h"));
            assertNotNull(anonenumCls);
            testEnumConstGetters(anonenumCls, List.of("RED", "GREEN", "BLUE"));
            testEnumConstGetters(anonenumCls, List.of(
                    "Java", "C", "CPP", "Python", "Ruby"));
            testEnumConstGetters(anonenumCls, List.of(
                    "XS", "S", "M", "L", "XL", "XXL"));
            testEnumConstGetters(anonenumCls, List.of(
                    "ONE", "TWO"));

            Class<?> enumClz[] = anonenumCls.getClasses();
            assert(enumClz.length >= 4);

            Class<?> enumCls = findClass(enumClz, "codetype_t");
            assertNotNull(enumCls);

            enumCls = findClass(enumClz, "SIZE");
            assertNotNull(enumCls);

            enumCls = findClass(enumClz, "temp");
            assertNotNull(enumCls);

            enumCls = findClass(enumClz, "temp_t");
            assertNotNull(enumCls);
        } finally {
            deleteFile(anonenumJar);
        }
    }

    @Test
    public void testExcludeSymbols() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        run("-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
            assertNotNull(findMethod(cls, "func2", Object[].class));
            assertNotNull(findMethod(cls, "func3", Object[].class));
            // check a method for "void junk()"
            assertNotNull(findMethod(cls, "junk", Object[].class));
            assertNotNull(findMethod(cls, "junk2", Object[].class));
            assertNotNull(findMethod(cls, "junk3", Object[].class));
        } finally {
            deleteFile(helloJar);
        }

        // try with --exclude-symbols" this time.
        run("--exclude-symbols", "junk.*", "-o", helloJar.toString(), helloH.toString())
                .checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
            assertNotNull(findMethod(cls, "func2", Object[].class));
            assertNotNull(findMethod(cls, "func3", Object[].class));
            // check a method for "void junk()"
            assertNull(findMethod(cls, "junk", Object[].class));
            assertNull(findMethod(cls, "junk2", Object[].class));
            assertNull(findMethod(cls, "junk3", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void testIncludeSymbols() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        run("-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
            assertNotNull(findMethod(cls, "func2", Object[].class));
            assertNotNull(findMethod(cls, "func3", Object[].class));
            // check a method for "void junk()"
            assertNotNull(findMethod(cls, "junk", Object[].class));
            assertNotNull(findMethod(cls, "junk2", Object[].class));
            assertNotNull(findMethod(cls, "junk3", Object[].class));
        } finally {
            deleteFile(helloJar);
        }

        // try with --include-symbols" this time.
        run("--include-symbols", "junk.*", "-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check a method for "void junk()"
            assertNotNull(findMethod(cls, "junk", Object[].class));
            assertNotNull(findMethod(cls, "junk2", Object[].class));
            assertNotNull(findMethod(cls, "junk3", Object[].class));
            // check a method for "void func()"
            assertNull(findMethod(cls, "func", Object[].class));
            assertNull(findMethod(cls, "func2", Object[].class));
            assertNull(findMethod(cls, "func3", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void testNoLocations() {
        Path simpleJar = getOutputFilePath("simple.jar");
        deleteFile(simpleJar);
        Path simpleH = getInputFilePath("simple.h");
        run("--no-locations", "-o", simpleJar.toString(), simpleH.toString()).checkSuccess();
        try(Loader loader = classLoader(simpleJar)) {
            Class<?> simpleCls = loader.loadClass(headerInterfaceName("simple.h"));
            Method func = findFirstMethod(simpleCls, "func");
            assertFalse(func.isAnnotationPresent(NativeLocation.class));
            Class<?> anonymousCls = loader.loadClass(structInterfaceName("simple.h", "anonymous"));
            assertFalse(simpleCls.isAnnotationPresent(NativeLocation.class));
        } finally{
            deleteFile(simpleJar);
        }
    }

    @Test
    public void testIncludeExcludeSymbols() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        run("-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
            assertNotNull(findMethod(cls, "func2", Object[].class));
            assertNotNull(findMethod(cls, "func3", Object[].class));
            // check a method for "void junk()"
            assertNotNull(findMethod(cls, "junk", Object[].class));
            assertNotNull(findMethod(cls, "junk2", Object[].class));
            assertNotNull(findMethod(cls, "junk3", Object[].class));
        } finally {
            deleteFile(helloJar);
        }

        // try with --include-symbols" this time.
        run("--include-symbols", "junk.*", "--exclude-symbols", "junk3",
                "-o", helloJar.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloJar)) {
            Class<?> cls = loader.loadClass(headerInterfaceName("hello.h"));
            // check a method for "void junk()"
            assertNotNull(findMethod(cls, "junk", Object[].class));
            assertNotNull(findMethod(cls, "junk2", Object[].class));
            // check a method for "void func()" - not included
            assertNull(findMethod(cls, "func", Object[].class));
            assertNull(findMethod(cls, "func2", Object[].class));
            assertNull(findMethod(cls, "func3", Object[].class));
            // excluded among the included set!
            assertNull(findMethod(cls, "junk3", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void testNestedStructsUnions() {
        Path nestedJar = getOutputFilePath("nested.jar");
        deleteFile(nestedJar);
        Path nestedH = getInputFilePath("nested.h");
        run("-o", nestedJar.toString(), nestedH.toString()).checkSuccess();
        try(Loader loader = classLoader(nestedJar)) {
            Class<?> headerCls = loader.loadClass(headerInterfaceName("nested.h"));
            assertNotNull(headerCls);

            Class<?> fooCls = loader.loadClass(structInterfaceName("nested.h", "Foo"));
            assertNotNull(fooCls);
            // struct Foo has no getters for "x", "y" etc.
            assertNull(findStructFieldGet(fooCls, "x"));
            assertNull(findStructFieldGet(fooCls, "y"));
            // struct Foo has getters for bar and color
            assertNotNull(findStructFieldGet(fooCls, "bar"));
            assertNotNull(findStructFieldGet(fooCls, "color"));
            // make sure nested types are handled without nested namespace!
            assertNotNull(loader.loadClass(structInterfaceName("nested.h", "Bar")));
            assertNotNull(loader.loadClass(enumInterfaceName("nested.h", "Color")));

            Class<?> uCls = loader.loadClass(structInterfaceName("nested.h", "U"));
            assertNotNull(uCls);
            // union U has no getters for "x", "y" etc.
            assertNull(findStructFieldGet(uCls, "x"));
            assertNull(findStructFieldGet(uCls, "y"));
            // union U has getters for point, rgb, i
            assertNotNull(findStructFieldGet(uCls, "point"));
            assertNotNull(findStructFieldGet(uCls, "rgb"));
            assertNotNull(findStructFieldGet(uCls, "i"));
            // make sure nested types are handled without nested namespace!
            assertNotNull(loader.loadClass(structInterfaceName("nested.h", "Point")));
            assertNotNull(loader.loadClass(enumInterfaceName("nested.h", "RGB")));

            Class<?> myStructCls = loader.loadClass(structInterfaceName("nested.h", "MyStruct"));
            assertNotNull(findStructFieldGet(myStructCls, "a"));
            assertNotNull(findStructFieldGet(myStructCls, "b"));
            assertNotNull(findStructFieldGet(myStructCls, "c"));
            assertNotNull(findStructFieldGet(myStructCls, "d"));
            // 'e' is named struct element - should not be in MyStruct
            assertNull(findStructFieldGet(myStructCls, "e"));
            assertNotNull(findStructFieldGet(myStructCls, "f"));
            assertNotNull(findStructFieldGet(myStructCls, "g"));
            assertNotNull(findStructFieldGet(myStructCls, "h"));
            // 'i' is named struct element - should not be in MyStruct
            assertNull(findStructFieldGet(myStructCls, "i"));
            // 'j' is named struct element - should not be in MyStruct
            assertNull(findStructFieldGet(myStructCls, "j"));
            assertNotNull(findStructFieldGet(myStructCls, "k"));
            // "X", "Y", "Z" are enum constants -should not be in MyStruct
            assertNull(findStructFieldGet(myStructCls, "X"));
            assertNull(findStructFieldGet(myStructCls, "Y"));
            assertNull(findStructFieldGet(myStructCls, "Z"));
            // anonymous enum constants are hoisted to containing scope
            assertNotNull(findEnumConstGet(headerCls, "X"));
            assertNotNull(findEnumConstGet(headerCls, "Y"));
            assertNotNull(findEnumConstGet(headerCls, "Z"));

            Class<?> myUnionCls = loader.loadClass(structInterfaceName("nested.h", "MyUnion"));
            assertNotNull(findStructFieldGet(myUnionCls, "a"));
            assertNotNull(findStructFieldGet(myUnionCls, "b"));
            assertNotNull(findStructFieldGet(myUnionCls, "c"));
            assertNotNull(findStructFieldGet(myUnionCls, "d"));
            // 'e' is named struct element - should not be in MyUnion
            assertNull(findStructFieldGet(myUnionCls, "e"));
            assertNotNull(findStructFieldGet(myUnionCls, "f"));
            assertNotNull(findStructFieldGet(myUnionCls, "g"));
            assertNotNull(findStructFieldGet(myUnionCls, "h"));
            // 'i' is named struct element - should not be in MyUnion
            assertNull(findStructFieldGet(myUnionCls, "i"));
            // 'j' is named struct element - should not be in MyUnion
            assertNull(findStructFieldGet(myUnionCls, "j"));
            assertNotNull(findStructFieldGet(myUnionCls, "k"));
            // "A", "B", "C" are enum constants -should not be in MyUnion
            assertNull(findStructFieldGet(myUnionCls, "A"));
            assertNull(findStructFieldGet(myUnionCls, "B"));
            assertNull(findStructFieldGet(myUnionCls, "C"));
            // anonymous enum constants are hoisted to containing scope
            assertNotNull(findEnumConstGet(headerCls, "A"));
            assertNotNull(findEnumConstGet(headerCls, "B"));
            assertNotNull(findEnumConstGet(headerCls, "C"));

            Class<?> xClass = loader.loadClass(structInterfaceName("nested.h", "X"));
            assertNotNull(findStructFieldGet(xClass, "Z"));
            Class<?> zClass = loader.loadClass(structInterfaceName("nested.h", "anon$nested_h$1977"));
            assertNotNull(findStructFieldGet(zClass, "y"));

            Class<?> x2Class = loader.loadClass(structInterfaceName("nested.h", "X2"));
            assertNotNull(findStructFieldGet(x2Class, "y"));
        } finally {
            deleteFile(nestedJar);
        }
    }

    @Test
    public void testAnonymousStructTypeGlobalVar() {
        Path elaboratedTypeJar = getOutputFilePath("elaboratedtype.jar");
        deleteFile(elaboratedTypeJar);
        Path elaboratedTypeH = getInputFilePath("elaboratedtype.h");
        run("-o", elaboratedTypeJar.toString(), elaboratedTypeH.toString()).checkSuccess();
        try(Loader loader = classLoader(elaboratedTypeJar)) {
            Class<?> headerCls = loader.loadClass(headerInterfaceName("elaboratedtype.h"));
            assertNotNull(findGlobalVariableGet(headerCls, "point"));
            assertNotNull(findGlobalVariableGet(headerCls, "long_or_int"));
            assertNotNull(findMethod(headerCls, "func", Pointer.class));
        } finally {
            deleteFile(elaboratedTypeJar);
        }
    }

    private void testBuiltinInclude(String name, BiConsumer<JextractResult, Path> validation)
            throws IOException {
        Path tmpSrc = Files.createTempFile("jet", ".h");
        Files.write(tmpSrc, List.of("#include <" + name + ">;"));
        Path fileJar = getOutputFilePath(name.replace(".", "_") + ".jar");
        deleteFile(fileJar);
        JextractResult result = run("-o", fileJar.toString(), tmpSrc.toString());
        validation.accept(result, fileJar);
        deleteFile(fileJar);
    }

    private final static BiConsumer<JextractResult, Path> checkSuccess = (result, jar) -> {
        result.checkSuccess();
    };

    @Test
    public void testBuiltinHeader() throws IOException {
        testBuiltinInclude("stdarg.h", checkSuccess);
        testBuiltinInclude("stdbool.h", checkSuccess);
        testBuiltinInclude("emmintrin.h", checkSuccess);
    }

    @Test
    public void testGlobalFuncPointerCallback() {
        Path globalFuncPointerJar = getOutputFilePath("globalFuncPointer.jar");
        deleteFile(globalFuncPointerJar);
        Path globalFuncPointerH = getInputFilePath("globalFuncPointer.h");
        run("-o", globalFuncPointerJar.toString(), globalFuncPointerH.toString()).checkSuccess();
        try(Loader loader = classLoader(globalFuncPointerJar)) {
            Class<?> callbackCls = loader.loadClass(callbackInterfaceName("globalFuncPointer.h", "FI1"));
            Method callback = findFirstMethod(callbackCls, "fn");
            assertNotNull(callback);
            assertTrue(callback.isVarArgs());
        } finally {
            deleteFile(globalFuncPointerJar);
        }
    }

    @Test
    public void testFuncPtrTypedef() {
        Path funcPtrTypedefJar = getOutputFilePath("funcPtrTypedef.jar");
        deleteFile(funcPtrTypedefJar);
        Path funcPtrTypedefH = getInputFilePath("funcPtrTypedef.h");
        run("-o", funcPtrTypedefJar.toString(), funcPtrTypedefH.toString()).checkSuccess();
        try(Loader loader = classLoader(funcPtrTypedefJar)) {
            // force parsing of class, method
            Class<?> headerCls = loader.loadClass(headerInterfaceName("funcPtrTypedef.h"));
            Method getter = findFirstMethod(headerCls, "my_function$get");
            assertNotNull(getter);
            assertNotNull(getter.getGenericParameterTypes());
        } finally {
            deleteFile(funcPtrTypedefJar);
        }
    }

    @Test
    public void testDuplicatedecls() {
        Path duplicatedeclsJar = getOutputFilePath("duplicatedecls.jar");
        deleteFile(duplicatedeclsJar);
        Path duplicatedeclsH = getInputFilePath("duplicatedecls.h");
        run("-o", duplicatedeclsJar.toString(), duplicatedeclsH.toString()).checkSuccess();
        try(Loader loader = classLoader(duplicatedeclsJar)) {
            // load the class to make sure no duplicate methods generated in it
            Class<?> headerCls = loader.loadClass(headerInterfaceName("duplicatedecls.h"));
            assertNotNull(headerCls);
        } finally {
            deleteFile(duplicatedeclsJar);
        }
    }
}
