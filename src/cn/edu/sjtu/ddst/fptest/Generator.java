package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.syntax.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BinaryOperator;

public strictfp class Generator {

    // Float operators
    private static final List<String> binaryOpList = List.of("+", "-", "*", "/");
    private static final Map<String, BinaryOperator<Double>> binaryOpMap = Map.of(
            "+", Double::sum,
            "-", (Double a, Double b) -> a - b,
            "*", (Double a, Double b) -> a * b,
            "/", (Double a, Double b) -> a / b
    );

    // Math functions
    private static final List<String> unaryFuncList = List.of(
            "sin", "cos", "tan", "asin", "acos", "atan", "exp", "log", "log10", "sqrt", "cbrt",
            "ceil", "floor", "rint", "sinh", "cosh", "tanh", "expm1", "log1p", "nextUp"
    );

    private static final List<String> binaryFuncList = List.of(
            "IEEEremainder", "atan2", "pow", "hypot", "nextAfter"
    );

    private Random rng;

    public Generator(long seed) {
        this.rng = new Random(seed);
    }

    public Program generate(int length, int testRange, boolean strict, String name) {
        // Initialize program
        Program program = new Program();
        program.name = name;
        program.strict = strict;

        // Initialize counter for variables and methods
        int nVar = 0;
        int nMethod = 0;

        // Create start variable of program
        Variable lastVar = new Variable(String.format("t%d", nVar++));
        Constant initConst = nextConstant();
        double lastVal = initConst.getValue(); // estimate calculated value in strict mode
        program.statements.add(new Assignment(lastVar, initConst));
        program.statements.add(createPrintStatement(lastVar));

        // Create testing statements and methods
        while (nMethod < length) {
            // Select test target
            int target;
            if (testRange == Tester.TEST_OPERATOR || testRange == Tester.TEST_MATH_FUNC)
                target = testRange;
            else {
                float opProb = ((float) binaryOpMap.size()) /
                        (binaryOpMap.size() + unaryFuncList.size() + binaryFuncList.size());
                target = rng.nextFloat() < opProb ? Tester.TEST_OPERATOR : Tester.TEST_MATH_FUNC;
            }

            // Create expression in method
            Variable param = new Variable("x");
            Constant nextConst = nextConstant();
            Expression expr;
            double newVal;
            try {
                if (target == Tester.TEST_OPERATOR) { // create operator testing method
                    int opChoice = rng.nextInt(binaryOpList.size());
                    String op = binaryOpList.get(opChoice);
                    newVal = binaryOpMap.get(op).apply(lastVal, nextConst.getValue());
                    expr = new BinaryOperation(op, param, nextConst);
                } else { // create math function testing method
                    int funcChoice = rng.nextInt(unaryFuncList.size() + binaryFuncList.size());
                    if (funcChoice < unaryFuncList.size()) {
                        String func = unaryFuncList.get(funcChoice);
                        newVal = (double) Math.class.getMethod(func, double.class)
                                .invoke(null, lastVal);
                        expr = new MethodCall("Math." + func, param);
                    } else {
                        String func = binaryFuncList.get(funcChoice - unaryFuncList.size());
                        newVal = (double) Math.class.getMethod(func, double.class, double.class)
                                .invoke(null, lastVal, nextConst.getValue());
                        expr = new MethodCall("Math." + func, param, nextConst);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                continue;
            }

            // Validate the estimated value
            if (!Double.isFinite(newVal) || newVal == 0) continue;
            if (newVal == lastVal || newVal == nextConst.getValue()) continue;

            // Create method
            Method method = new Method(String.format("m%d", nMethod++), param, expr);
            program.methods.add(method);

            // Create statement
            Variable newVar = new Variable(String.format("t%d", nVar++));
            program.statements.add(new Assignment(newVar, new MethodCall(method.getName(), lastVar)));

            // Print result of this step
            program.statements.add(createPrintStatement(newVar));

            // Update variable and its estimate
            lastVar = newVar;
            lastVal = newVal;
        }

        return program;
    }

    private static MethodCall createPrintStatement(Variable var) {
        return new MethodCall("System.out.println",
                new MethodCall("Long.toHexString",
                        new MethodCall("Double.doubleToRawLongBits", var)));
    }

    private static final int MIN_DIGITS = 2;
    private static final int MAX_DIGITS = 16;
    private static final int MIN_EXPONENT = -308;
    private static final int MAX_EXPONENT = 308;

    private Constant nextConstant() {
        while (true) {
            // Generate mantissa and sign
            String mantissa = Long.toString(rng.nextLong());
            int digits = rng.nextInt(MAX_DIGITS - MIN_DIGITS) + MIN_DIGITS;
            if (mantissa.startsWith("-")) { // negative
                StringBuilder builder = new StringBuilder(mantissa.substring(0, digits + 1));
                builder.insert(2, '.');
                mantissa = builder.toString();
            } else { // positive
                StringBuilder builder = new StringBuilder(mantissa.substring(0, digits));
                builder.insert(1, '.');
                mantissa = builder.toString();
            }

            // Generate exponent
            int exponent = rng.nextInt(MAX_EXPONENT - MIN_EXPONENT) + MIN_EXPONENT;

            // Assemble the value
            String literal = String.format("%sE%d", mantissa, exponent);
            Constant value = new Constant(literal);
            if (Double.isFinite(value.getValue()) && value.getValue() != 0)
                return value;
        }
    }
}
