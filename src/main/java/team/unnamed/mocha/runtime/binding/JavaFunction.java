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
package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import team.unnamed.mocha.runtime.ExecutionContext;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.Value;

import java.lang.reflect.Method;

@ApiStatus.Internal
public final class JavaFunction<T extends @Nullable Object> implements Function<T> {
    private final @Nullable Object object;
    private final @Nullable Method method;
    private final Function<T> function;
    private final boolean pure;

    JavaFunction(final @Nullable Object object, final @Nullable Method method, final @Nullable Function<T> function, final boolean pure) {
        this.object = object;
        this.method = method;
        this.function = evaluate(function);
        this.pure = pure;
    }

    private Function<T> evaluate(@Nullable Function<T> function) {
        if (function != null) {
            return function;
        }

        if (method == null) {
            throw new IllegalArgumentException("Either the method or a generic function must be given.");
        }

        // create the generic function from the method
        return new ReflectiveFunction<>(object, method);
    }

    public @Nullable Object object() {
        return object;
    }

    public @Nullable Method method() {
        return method;
    }

    @Override
    public @Nullable Value evaluate(final ExecutionContext<T> context, final Arguments arguments) {
        return function.evaluate(context, arguments);
    }

    @Override
    public boolean pure() {
        return pure;
    }
}
