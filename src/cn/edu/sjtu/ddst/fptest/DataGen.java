package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.ast.Constant;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

public class DataGen {
    private Random rng;

    public DataGen(long seed) {
        this.rng = new Random(seed);
    }

    public ProgramData generate(TestRange range, int length) {
        // Initialize counter for expressions
        int nExpr = 0;

        // Create start variable of program
        Constant initConst = nextConst();
        double lastEst = initConst.getValue(); // estimate calculated value in strict mode

        // Create computation list
        ArrayList<Computation> compList = new ArrayList<>(length);

        // Create testing statements and methods
        while (nExpr < length) {
            // Select test target
            int funcLen = MathFuncSpec.UNARY_LIST.size() + MathFuncSpec.BINARY_LIST.size();
            int totalLen = funcLen + OperatorSpec.BINARY_LIST.size();
            TestRange target;
            if (range == TestRange.ALL) {
                float opProb = ((float) OperatorSpec.BINARY_MAP.size()) / totalLen;
                target = rng.nextFloat() < opProb ? TestRange.OPERATOR
                        : TestRange.MATH_FUNC;
            } else {
                target = range;
            }

            // Create expression in method
            Constant nextConst = nextConst();
            Computation comp;
            double newEst;
            try {
                if (target == TestRange.OPERATOR) { // create operator testing method
                    int opChoice = rng.nextInt(OperatorSpec.BINARY_LIST.size());
                    String op = OperatorSpec.BINARY_LIST.get(opChoice);
                    newEst = OperatorSpec.BINARY_MAP.get(op).apply(lastEst, nextConst.getValue());
                    comp = new Computation(Operation.OPERATOR, op, nextConst);
                } else { // create math function testing method
                    int funcChoice = rng.nextInt(funcLen);
                    if (funcChoice < MathFuncSpec.UNARY_LIST.size()) {
                        String func = MathFuncSpec.UNARY_LIST.get(funcChoice);
                        newEst = (double) Math.class.getMethod(func, double.class)
                                .invoke(null, lastEst);
                        comp = new Computation(Operation.UNARY_FUNC, func, nextConst);
                    } else {
                        String func = MathFuncSpec.BINARY_LIST.get(funcChoice
                                - MathFuncSpec.UNARY_LIST.size());
                        newEst = (double) Math.class.getMethod(func, double.class, double.class)
                                .invoke(null, lastEst, nextConst.getValue());
                        comp = new Computation(Operation.BINARY_FUNC, func, nextConst);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                continue;
            }

            // Validate the estimated value
            // Result cannot be infinite nor zero
            if (!Double.isFinite(newEst) || newEst == 0) continue;
            // Result cannot be equal to either operand
            if (newEst == lastEst || newEst == nextConst.getValue()) continue;

            // Add computation to list
            compList.add(comp);

            // Update estimate of computation
            lastEst = newEst;
            nExpr++;
        }

        // Return program data
        return new ProgramData(initConst, compList);
    }

    private static final int MIN_DIGITS = 2;
    private static final int MAX_DIGITS = 16;
    private static final int MIN_EXPONENT = -308;
    private static final int MAX_EXPONENT = 308;

    private Constant nextConst() {
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
