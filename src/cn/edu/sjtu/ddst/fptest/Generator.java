package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.syntax.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BinaryOperator;

public strictfp class Generator {

    // Float operators
    private static final List<String> binaryOpList = List.of("+","-","*","/");
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
    private int minExp;
    private int modExp;

    public Generator(long seed, int minExp, int maxExp) {
        this.rng = new Random(seed);
        if (minExp < -1023)
            throw new RuntimeException("Minimum exponent must be no less than -1023");
        this.minExp = minExp;
        if (maxExp > 1024)
            throw new RuntimeException("Maximum exponent must be no more than 1024");
        if (maxExp <= minExp)
            throw new RuntimeException("Maximum exponent must be greater than minimum");
        this.modExp = maxExp - minExp + 1;
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
            double newVal = Double.POSITIVE_INFINITY;
            if (target == Tester.TEST_OPERATOR) { // create operator testing method
                int opChoice = rng.nextInt(binaryOpList.size());
                String op = binaryOpList.get(opChoice);
                newVal = binaryOpMap.get(op).apply(lastVal, nextConst.getValue());
                expr = new BinaryOperation(op, param, nextConst);
            } else { // create math function testing method
                int funcChoice = rng.nextInt(unaryFuncList.size() + binaryFuncList.size());
                if (funcChoice < unaryFuncList.size()) {
                    String func = unaryFuncList.get(funcChoice);
                    try {
                        newVal = (double) Math.class.getMethod(func, double.class)
                                .invoke(null, lastVal);
                    } catch (NoSuchMethodException | IllegalAccessException |
                            InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    expr = new MethodCall("Math." + func, param);
                } else {
                    String func = binaryFuncList.get(funcChoice - unaryFuncList.size());
                    try {
                        newVal = (double) Math.class.getMethod(func, double.class, double.class)
                                .invoke(null, lastVal, nextConst.getValue());
                    } catch (IllegalAccessException | InvocationTargetException
                            | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    expr = new MethodCall("Math." + func, param, nextConst);
                }
            }

            // Ensure this expression produces finite result
            boolean rndBool = rng.nextBoolean(); // ensure same sequence under any modifier choice
            if (!Double.isFinite(newVal) || newVal == 0) continue; // reject this expression

            // Create method
            Method method = new Method(String.format("m%d", nMethod++), param, expr);
            program.methods.add(method);

            // Create statement
            Variable newVar = new Variable(String.format("t%d", nVar++));
            program.statements.add(new Assignment(newVar, new MethodCall(method.getName(), lastVar)));

            // Update variable and its estimate
            lastVar = newVar;
            lastVal = newVal;
        }

        // Print final variable
        program.statements.add(
                new MethodCall("System.out.println",
                new MethodCall("Long.toHexString",
                new MethodCall("Double.doubleToRawLongBits", lastVar)))
        );

        return program;
    }

    private static final long EXPONENT_MASK = ((1L << 11) - 1) << 52;

    private Constant nextConstant() {
        while (true) {
            // Generate random 64 bits
            long bits = rng.nextLong();
            // Apply modulo to exponent bits
            long exponent = ((bits & EXPONENT_MASK) >> 52);
            exponent %= modExp;
            bits = (bits & ~EXPONENT_MASK) | ((exponent + minExp + 1023) << 52);
            // Create double from long bits
            double value = Double.longBitsToDouble(bits);
            // Validate generated double
            if (Double.isFinite(value) && value != 0)
                return new Constant(value);
        }
    }
}
