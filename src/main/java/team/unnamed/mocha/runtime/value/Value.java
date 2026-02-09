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

import java.util.Map;
import java.util.StringJoiner;

public sealed interface Value permits ArrayValue, Function, ObjectValue, SingleValue {
    static Value nil() {
        return DoubleValue.ZERO;
    }

    default double getAsNumber() {
        if (this instanceof DoubleValue num) {
            return num.doubleValue();
        } else {
            return 0;
        }
    }

    default boolean getAsBoolean() {
        return switch (this) {
            case DoubleValue num -> num.doubleValue() != 0D;
            case StringValue str -> !str.value().isEmpty();
            case ArrayValue arr -> arr.values().length != 0;
            case ObjectValue obj -> !obj.entries().isEmpty();
            default -> true;
        };
    }

    default boolean isString() {
        return this instanceof StringValueImpl;
    }

    default String getAsString() {
        return switch (this) {
            case StringValue str -> str.value();
            case DoubleValue num -> Double.toString(num.doubleValue());
            case ArrayValue arr -> {
                final Value[] values = arr.values();
                final StringJoiner joiner = new StringJoiner(", ", "[", "]");
                for (final Value value : values) {
                    joiner.add(value.getAsString());
                }
                yield joiner.toString();
            }
            case ObjectValue obj -> {
                final Map<String, ObjectProperty> values = obj.entries();
                final StringJoiner joiner = new StringJoiner(", ", "{", "}");
                for (final Map.Entry<String, ObjectProperty> entry : values.entrySet()) {
                    joiner.add(entry.getKey() + ": " + entry.getValue().value().getAsString());
                }
                yield joiner.toString();
            }
            case Function<?> $ -> "Function(" + this + ")";
            default -> throw new IllegalArgumentException("Unknown value type: " + this.getClass().getName());
        };
    }
}
