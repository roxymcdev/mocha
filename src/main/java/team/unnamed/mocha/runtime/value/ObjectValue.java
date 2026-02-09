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
package team.unnamed.mocha.runtime.value;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public non-sealed interface ObjectValue extends Value {
    /**
     * Returns the property for the given name ignoring
     * its case. This method is CASE-INSENSITIVE.
     *
     * @param name The name of the property
     * @return The property for the given name
     * @since 3.0.0
     */
    @Nullable ObjectProperty getProperty(final String name);

    /**
     * Gets the value of the specified property,
     * ignoring its casing. This method is
     * CASE-INSENSITIVE.
     *
     * @param name The name of the property
     * @return The value of the property
     * @since 3.0.0
     */
    default Value get(final String name) {
        final ObjectProperty property = getProperty(name);
        if (property == null) {
            return Value.nil();
        } else {
            return property.value();
        }
    }

    default boolean set(final String name, final @Nullable Value value) {
        return false;
    }

    default Map<String, ObjectProperty> entries() {
        return Collections.emptyMap();
    }

    // :) overloads
    default void setFunction(final String name, final DoubleFunction1 function) {
        set(name, (Function<?>) (ctx, args) -> DoubleValue.of(function.apply(
                requireNonNull(args.next().eval(), "arg 0").getAsNumber())
        ));
    }

    default void setFunction(final String name, final DoubleFunction2 function) {
        set(name, (Function<?>) (ctx, args) -> DoubleValue.of(function.apply(
                requireNonNull(args.next().eval(), "arg 0").getAsNumber(),
                requireNonNull(args.next().eval(), "arg 1").getAsNumber())
        ));
    }

    default void setFunction(final String name, final DoubleFunction3 function) {
        set(name, (Function<?>) (ctx, args) -> DoubleValue.of(function.apply(
                requireNonNull(args.next().eval(), "arg 0").getAsNumber(),
                requireNonNull(args.next().eval(), "arg 1").getAsNumber(),
                requireNonNull(args.next().eval(), "arg 2").getAsNumber())
        ));
    }

    interface DoubleFunction1 {
        double apply(double n);
    }

    interface DoubleFunction2 {
        double apply(double n1, double n2);
    }

    interface DoubleFunction3 {
        double apply(double n1, double n2, double n3);
    }
}
