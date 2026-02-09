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
package team.unnamed.mocha.runtime;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.Bytecode;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.util.Map;

import static java.util.Objects.requireNonNull;

final class FunctionCompileState {
    private final MolangCompiler compiler;

    private final ClassPool classPool;
    private final CtClass ctClass;
    private final Bytecode bytecode;

    private final Map<String, CtClass> parametersCtTypes;

    private final Map<String, Object> requirements = new CaseInsensitiveStringHashMap<>();
    private final Scope scope;

    private int maxLocals = 0;

    FunctionCompileState(
            MolangCompiler compiler,
            ClassPool classPool,
            CtClass ctClass,
            Bytecode bytecode,
            Map<String, CtClass> parametersCtTypes,
            Scope scope
    ) {
        this.compiler = requireNonNull(compiler, "compiler");

        this.classPool = requireNonNull(classPool, "classPool");
        this.ctClass = requireNonNull(ctClass, "ctClass");
        this.bytecode = requireNonNull(bytecode, "bytecode");

        this.parametersCtTypes = requireNonNull(parametersCtTypes, "parametersCtTypes");

        this.scope = requireNonNull(scope, "scope");
    }

    public MolangCompiler compiler() {
        return compiler;
    }

    public ClassPool classPool() {
        return classPool;
    }

    public CtClass type() {
        return ctClass;
    }

    public Bytecode bytecode() {
        return bytecode;
    }

    public Map<String, CtClass> parametersCtTypes() {
        return parametersCtTypes;
    }

    public Map<String, Object> requirements() {
        return requirements;
    }

    public Scope scope() {
        return scope;
    }

    public int maxLocals() {
        return maxLocals;
    }

    public void maxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }
}
