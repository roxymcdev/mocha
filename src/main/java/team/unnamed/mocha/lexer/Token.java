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
package team.unnamed.mocha.lexer;

import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Class representing a Molang token. Each token has some
 * information set by the lexer (i.e. start/end position,
 * token kind and optional value)
 *
 * @since 3.0.0
 */
public record Token(TokenKind kind, @Nullable String value, int start, int end) {
    public Token {
        // verify state, token kinds that have HAS_VALUE tag, must have a non-null value
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE) && value == null) {
            throw new IllegalArgumentException("A token with kind " + kind + " must have a non-null value");
        }
    }

    /**
     * Gets the token value. Null if this kind
     * of tokens doesn't allow values.
     *
     * @return The token value
     * @since 3.0.0
     */
    public @Nullable String valueOrNull() {
        return value;
    }

    @Override
    public String value() {
        return requireNonNull(value, "Token of kind %s doesn't provide a value".formatted(kind));
    }

    @Override
    public String toString() {
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE)) {
            return kind + "(" + value + ")";
        }

        return kind.toString();
    }
}
