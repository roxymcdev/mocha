/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
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
package team.unnamed.mocha.runtime.jvm;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeTokenMolangCompilerTest {
    @Test
    void test() {
        final MochaEngine<?> engine = MochaEngine.createStandard();

        final ScriptType<Number> script = engine.compile("1", new TypeToken<>() {});
        assertEquals(1, script.eval().intValue());

        final ScriptType<Object> script2 = engine.compile("'Hello, World!'", new TypeToken<>() {});
        assertEquals("Hello, World!", script2.eval());

        final ScriptType<Boolean> script3 = engine.compile("true", new TypeToken<>() {});
        assertEquals(true, script3.eval());
    }

    public interface ScriptType<T> extends MochaCompiledFunction {
        T eval();
    }
}
