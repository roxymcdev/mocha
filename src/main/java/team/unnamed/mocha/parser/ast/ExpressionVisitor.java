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
package team.unnamed.mocha.parser.ast;

import org.jspecify.annotations.Nullable;

/**
 * An {@link Expression} visitor. Provides a way to add
 * functionalities to the expression interface and all
 * of its implementations.
 *
 * <p>See the following example on visiting an expression:</p>
 * <pre>{@code
 *      Expression expr = ...;
 *      String str = expr.visit(new ToStringVisitor());
 * }</pre>
 *
 * <p>Please note that users MUST use {@link Expression#visit(ExpressionVisitor)}
 * and NOT ExpressionVisitor's {@link ExpressionVisitor#visit(Expression, Context)}, because
 * it will not work as intended.</p>
 *
 * @param <R> The visit result type
 * @since 3.0.0
 */
public interface ExpressionVisitor<R extends @Nullable Object, C extends ExpressionVisitor.Context> {

    /**
     * Evaluate for the given unknown expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    R visit(final Expression expression, final C ctx);

    /**
     * Evaluate for array access expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitArrayAccess(final ArrayAccessExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for double expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitDouble(final DoubleExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for string expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitString(final StringExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for identifier expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitIdentifier(final IdentifierExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for ternary conditional expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitTernaryConditional(final TernaryConditionalExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for unary expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitUnary(final UnaryExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for execution scope expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitExecutionScope(final ExecutionScopeExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for binary expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitBinary(final BinaryExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for access expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitAccess(final AccessExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for call expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitCall(final CallExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    /**
     * Evaluate for statement expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 4.0
     */
    default R visitStatement(final StatementExpression expression, final C ctx) {
        return visit(expression, ctx);
    }

    interface Context {
        static Context empty() {
            final class Holder {
                static final Context EMPTY = new Context() {
                };
            }

            return Holder.EMPTY;
        }
    }
}