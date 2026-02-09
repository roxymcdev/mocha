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
package team.unnamed.mocha;

import com.google.common.reflect.TypeToken;
import javassist.ClassPool;
import org.jspecify.annotations.Nullable;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.ExpressionInterpreter;
import team.unnamed.mocha.runtime.MolangCompiler;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.value.DoubleValue;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.Value;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.function.Consumer;

final class MochaEngineImpl<T extends @Nullable Object> implements MochaEngine<T> {
    private final Scope scope;
    private final T entity;
    private final MolangCompiler compiler;

    private boolean warnOnReflectiveFunctionUsage;

    public MochaEngineImpl(final T entity, final Consumer<Scope.Builder> scopeBuilder) {
        Scope.Builder builder = Scope.builder();
        scopeBuilder.accept(builder);
        this.scope = builder.build();
        this.entity = entity;
        this.compiler = new MolangCompiler(entity, scope);
    }

    @Override
    public List<Expression> parse(final Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

    @Override
    public Value eval(final List<Expression> expressions) {
        // create bindings that just apply for this evaluation
        final Scope local = scope.copy();
        {
            // create temp bindings
            MutableObjectBinding temp = new MutableObjectBinding();
            local.set("temp", temp);
            local.set("t", temp);
        }
        local.readOnly(true);

        ExpressionInterpreter<T> evaluator = new ExpressionInterpreter<>(entity, local);
        evaluator.warnOnReflectiveFunctionUsage(warnOnReflectiveFunctionUsage);

        Value lastValue = DoubleValue.ZERO;

        for (Expression expression : expressions) {
            lastValue = expression.visit(evaluator);
            Value returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastValue = returnValue;
                break;
            }
        }

        return lastValue;
    }

    @Override
    public <F extends MochaCompiledFunction> F compile(final Reader reader, final TypeToken<F> interfaceType) throws IOException {
        List<Expression> parsed = parse(reader);
        return compiler.compile(parsed, interfaceType);
    }

    @Override
    public ClassPool classPool() {
        return compiler.classPool();
    }

    @Override
    public Scope scope() {
        return scope;
    }

    @Override
    public void bind(final Class<?> clazz) {
        final JavaObjectBinding javaObjectBinding = JavaObjectBinding.of(clazz, null, null);
        for (final String name : javaObjectBinding.names()) {
            scope.set(name, javaObjectBinding);
        }
    }

    @Override
    public <B> void bindInstance(final Class<? super B> clazz, final B instance, final String name, final String... aliases) {
        final JavaObjectBinding javaObjectBinding = JavaObjectBinding.of(clazz, instance, null);
        scope.set(name, javaObjectBinding);
        for (final String alias : aliases) {
            scope.set(alias, javaObjectBinding);
        }
    }

    @Override
    public MochaEngine<T> warnOnReflectiveFunctionUsage(final boolean warnOnReflectiveFunctionUsage) {
        this.warnOnReflectiveFunctionUsage = warnOnReflectiveFunctionUsage;
        return this;
    }

    @Override
    public MochaEngine<T> postCompile(final @Nullable Consumer<byte[]> bytecodeConsumer) {
        compiler.postCompile(bytecodeConsumer);
        return this;
    }
}
