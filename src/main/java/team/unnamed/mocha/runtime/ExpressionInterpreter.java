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

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import team.unnamed.mocha.parser.ast.*;
import team.unnamed.mocha.runtime.binding.JavaFunction;
import team.unnamed.mocha.runtime.value.*;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class ExpressionInterpreter<T extends @Nullable Object> implements ExpressionVisitor<Value, ExpressionVisitor.Context>, ExecutionContext<T> {
    private static final List<Evaluator> BINARY_EVALUATORS = Arrays.asList(
            bool((a, b) -> a.eval() && b.eval()),
            bool((a, b) -> a.eval() || b.eval()),
            compare((a, b) -> a.eval() < b.eval()),
            compare((a, b) -> a.eval() <= b.eval()),
            compare((a, b) -> a.eval() > b.eval()),
            compare((a, b) -> a.eval() >= b.eval()),
            (evaluator, a, b) -> {
                final Value aVal = a.visit(evaluator);
                final Value bVal = b.visit(evaluator);
                // string concatenation is not supported in molang
                // if (aVal.isString() || bVal.isString()) {
                //     return StringValue.of(aVal.getAsString() + bVal.getAsString());
                // } else {
                return DoubleValue.of(aVal.getAsNumber() + bVal.getAsNumber());
                // }
            },
            arithmetic((a, b) -> a.eval() - b.eval()),
            arithmetic((a, b) -> a.eval() * b.eval()),
            arithmetic((a, b) -> {
                // Molang allows division by zero,
                // which is always equal to 0
                final double dividend = a.eval();
                final double divisor = b.eval();
                if (divisor == 0) return 0;
                else return dividend / divisor;
            }),
            (evaluator, a, b) -> { // arrow
                final Value value = a.visit(evaluator);

                if (value instanceof JavaValue(Object theValue)) {
                    return b.visit(evaluator.createChild(theValue));
                }

                return DoubleValue.ZERO;
            },
            (evaluator, a, b) -> { // null coalesce
                final Value value = a.visit(evaluator);
                return value.getAsBoolean() ? value : b.visit(evaluator);
            },
            (evaluator, a, b) -> { // assignation
                final Value value = b.visit(evaluator);

                // we can only assign to values that are accessed
                // like:
                //      temp.x = 1
                //      t.location.world = 'world'
                // but not:
                //      x = 1
                //      i = 2
                if (a instanceof AccessExpression access) {
                    final Value objectValue = access.object().visit(evaluator);

                    if (objectValue instanceof MutableObjectBinding objectBinding) {
                        objectBinding.set(access.property(), value);
                    }
                }

                return value;
            },
            (evaluator, a, b) -> { // conditional
                final Value conditionValue = a.visit(evaluator);
                if (conditionValue.getAsBoolean()) {
                    final Value predicateVal = b.visit(evaluator);
                    if (predicateVal instanceof Function) {
                        return ((Function) predicateVal).evaluate(evaluator);
                    } else {
                        return predicateVal;
                    }
                }
                return DoubleValue.ZERO;
            },
            arithmetic((a, b) -> ((a.eval() == b.eval()) ? 1.0F : 0.0F)), // eq
            arithmetic((a, b) -> ((a.eval() != b.eval()) ? 1.0F : 0.0F))  // neq
    );

    private final T entity;
    private final Scope scope;
    private @Nullable Object flag;
    private @Nullable Value returnValue;

    private boolean warnOnReflectiveFunctionUsage;

    public ExpressionInterpreter(final T entity, final Scope scope) {
        this.entity = entity;
        this.scope = requireNonNull(scope, "scope");
    }

    private static Evaluator bool(BooleanOperator op) {
        return (evaluator, a, b) -> DoubleValue.of(op.operate(
                () -> a.visit(evaluator).getAsBoolean(),
                () -> b.visit(evaluator).getAsBoolean()
        ));
    }

    private static Evaluator compare(Comparator comp) {
        return (evaluator, a, b) -> DoubleValue.of(comp.compare(
                () -> a.visit(evaluator).getAsNumber(),
                () -> b.visit(evaluator).getAsNumber()
        ));
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (evaluator, a, b) -> DoubleValue.of(op.operate(
                () -> a.visit(evaluator).getAsNumber(),
                () -> b.visit(evaluator).getAsNumber()
        ));
    }

    public void warnOnReflectiveFunctionUsage(final boolean warnOnReflectiveFunctionUsage) {
        this.warnOnReflectiveFunctionUsage = warnOnReflectiveFunctionUsage;
    }

    @Override
    public @Nullable Object flag() {
        return flag;
    }

    @Override
    public void flag(final @Nullable Object flag) {
        this.flag = flag;
    }

    @Override
    public T entity() {
        return entity;
    }

    @Override
    public Value eval(final Expression expression) {
        return expression.visit(this);
    }

    public <R extends @Nullable Object> ExpressionInterpreter<R> createChild(final @Nullable R entity) {
        return new ExpressionInterpreter<>(entity, this.scope);
    }

    public ExpressionInterpreter<T> createChild() {
        // Note that it will have its own returnValue, but same bindings
        // (Should we create new bindings?)
        return new ExpressionInterpreter<>(this.entity, this.scope);
    }

    public Scope bindings() {
        return scope;
    }

    public @Nullable Value popReturnValue() {
        final Value val = this.returnValue;
        this.returnValue = null;
        return val;
    }

    @Override
    public Value visitArrayAccess(final ArrayAccessExpression expression, final Context ctx) {
        final Value array = expression.array().visit(this);
        final Value index = expression.index().visit(this);
        if (!(array instanceof ArrayValue)) {
            return Value.nil();
        } else {
            final Value[] values = ((ArrayValue) array).values();
            final int validIndex = Math.max(0, (int) index.getAsNumber()) % values.length;
            return values[validIndex];
        }
    }

    @Override
    public Value visitAccess(final AccessExpression expression, final Context ctx) {
        final Value objectValue = expression.object().visit(this);
        if (objectValue instanceof ObjectValue) {
            return ((ObjectValue) objectValue).get(expression.property());
        }
        return DoubleValue.ZERO;
    }

    @Override
    public @Nullable Value visitCall(final CallExpression expression, final Context ctx) {
        final List<Expression> argumentsExpressions = expression.arguments();
        final Function.Argument[] arguments = new Function.Argument[argumentsExpressions.size()];
        for (int i = 0; i < argumentsExpressions.size(); i++) {
            arguments[i] = new FunctionArgumentImpl(argumentsExpressions.get(i));
        }
        final Function.Arguments args = new FunctionArguments(arguments);

        final Expression functionExpr = expression.function();
        if (functionExpr instanceof IdentifierExpression) {
            final String identifierName = ((IdentifierExpression) functionExpr).name();
            if ("loop".equals(identifierName)) {
                // loop built-in function
                // Parameters:
                // - double:           How many times should we loop
                // - CallableBinding:  The looped expressions

                Value timesValue = args.next().eval();
                if (!(timesValue instanceof DoubleValue)) {
                    return DoubleValue.ZERO;
                }

                int times = Math.round((float) timesValue.getAsNumber());

                Value expr = args.next().eval();

                if (!(expr instanceof Function callable)) {
                    return DoubleValue.ZERO;
                }

                for (int i = 0; i < times; i++) {
                    final ExpressionInterpreter<T> interpreter = createChild();

                    callable.evaluate(interpreter);

                    if (interpreter.returnValue != null) {
                        return interpreter.returnValue;
                    } else if (interpreter.flag == StatementExpression.Op.BREAK) {
                        break;
                    }
                }

                return DoubleValue.ZERO;
            } else if ("for_each".equals(identifierName)) {
                // for each built-in function
                // Parameters:
                // - any:              Variable
                // - array:            Any array
                // - CallableBinding:  The looped expressions
                final Expression variableExpr = args.next().expression();
                if (!(variableExpr instanceof AccessExpression variableAccess)) {
                    // first argument must be an access expression,
                    // e.g. 'variable.test', 'v.pig', 't.entity' or
                    // 't.entity.location.world'
                    return DoubleValue.ZERO;
                }
                final Expression objectExpr = variableAccess.object();
                final String propertyName = variableAccess.property();

                final Value array = args.next().eval();
                final Iterable<Value> arrayIterable;
                if (array instanceof ArrayValue) {
                    arrayIterable = (ArrayValue) array;
                } else {
                    // second argument must be an array or iterable
                    return DoubleValue.ZERO;
                }

                final Value expr = args.next().eval();

                if (!(expr instanceof Function callable)) {
                    return DoubleValue.ZERO;
                }

                for (final Value val : arrayIterable) {
                    final ExpressionInterpreter<T> interpreter = createChild();

                    final Value evaluatedObjectValue = eval(objectExpr);
                    if (evaluatedObjectValue instanceof MutableObjectBinding objectBinding) {
                        objectBinding.set(propertyName, val);
                    }

                    callable.evaluate(interpreter);

                    if (interpreter.returnValue != null) {
                        return interpreter.returnValue;
                    } else if (interpreter.flag == StatementExpression.Op.BREAK) {
                        break;
                    }
                }
                return DoubleValue.ZERO;
            }
        }

        final Value function = functionExpr.visit(this);
        if (!(function instanceof Function)) {
            return Value.nil();
        }

        if (warnOnReflectiveFunctionUsage && function instanceof JavaFunction) {
            final JavaFunction<?> javaFunction = (JavaFunction<?>) function;
            System.err.println("Warning: Reflective function usage detected for method: " + javaFunction.method());
        }

        return ((Function<T>) function).evaluate(this, args);
    }

    @Override
    public Value visitDouble(final DoubleExpression expression, final Context ctx) {
        return DoubleValue.of(expression.value());
    }

    @Override
    public Value visitExecutionScope(final ExecutionScopeExpression executionScope, final Context ctx) {
        List<Expression> expressions = executionScope.expressions();
        return (Function<T>) (context, arguments) -> {
            for (Expression expression : expressions) {
                // eval expression, ignore result
                context.eval(expression);

                // check for return values
                if (context.flag() != null) {
                    break;
                }
            }
            return DoubleValue.ZERO;
        };
    }

    @Override
    public Value visitIdentifier(final IdentifierExpression expression, final Context ctx) {
        return scope.get(expression.name());
    }

    @Override
    public Value visitBinary(BinaryExpression expression, final Context ctx) {
        return BINARY_EVALUATORS.get(expression.op().ordinal()).eval(
                this,
                expression.left(),
                expression.right()
        );
    }

    @Override
    public Value visitUnary(final UnaryExpression expression, final Context ctx) {
        final Value value = expression.expression().visit(this);

        return switch (expression.op()) {
            case LOGICAL_NEGATION -> DoubleValue.of(!value.getAsBoolean());
            case ARITHMETICAL_NEGATION -> DoubleValue.of(-value.getAsNumber());
            case RETURN -> {
                this.returnValue = value;
                yield DoubleValue.ZERO;
            }
        };
    }

    @Override
    public Value visitStatement(final StatementExpression expression, final Context ctx) {
        this.flag = switch (expression.op()) {
            case BREAK -> StatementExpression.Op.BREAK;
            case CONTINUE -> StatementExpression.Op.CONTINUE;
        };

        return DoubleValue.ZERO;
    }

    @Override
    public Value visitString(final StringExpression expression, final Context ctx) {
        return StringValue.of(expression.value());
    }

    @Override
    public Value visitTernaryConditional(TernaryConditionalExpression expression, final Context ctx) {
        final Value conditionResult = expression.condition().visit(this);
        return conditionResult.getAsBoolean()
                ? expression.trueExpression().visit(this)
                : expression.falseExpression().visit(this);
    }

    @Override
    public Value visit(final Expression expression, final Context ctx) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }

    private interface Evaluator {
        Value eval(ExpressionInterpreter<?> evaluator, Expression a, Expression b);
    }

    private interface BooleanOperator {
        boolean operate(LazyEvaluableBoolean a, LazyEvaluableBoolean b);
    }

    interface LazyEvaluableBoolean {
        boolean eval();
    }

    interface LazyEvaluableDouble {
        double eval();
    }

    private interface Comparator {
        boolean compare(LazyEvaluableDouble a, LazyEvaluableDouble b);

    }

    private interface ArithmeticOperator {
        double operate(LazyEvaluableDouble a, LazyEvaluableDouble b);
    }

    public static class FunctionArguments implements Function.Arguments {
        public static final Function.Arguments EMPTY = new FunctionArguments(new Function.Argument[0]);

        private final Function.Argument[] arguments;
        private int next;

        FunctionArguments(final Function.Argument [] arguments) {
            this.arguments = requireNonNull(arguments, "arguments");
        }

        @Override
        public Function.Argument next() {
            if (next < arguments.length) {
                return arguments[next++];
            } else {
                return EmptyFunctionArgument.EMPTY;
            }
        }

        @Override
        public int length() {
            return arguments.length;
        }
    }

    private static class EmptyFunctionArgument implements Function.Argument {
        static final Function.Argument EMPTY = new EmptyFunctionArgument();

        @Override
        public @Nullable Expression expression() {
            return null;
        }

        @Override
        public @Nullable Value eval() {
            return DoubleValue.ZERO;
        }
    }

    private class FunctionArgumentImpl implements Function.Argument {
        private final Expression expression;

        FunctionArgumentImpl(final Expression expression) {
            this.expression = expression;
        }

        @Override
        public Expression expression() {
            return expression;
        }

        @Override
        public @Nullable Value eval() {
            return expression.visit(ExpressionInterpreter.this);
        }
    }
}
