package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.ast.*;

class ProgramBuilder {
    // Build Java source program from given data
    public static Program build(ProgramData data, String name, boolean strict) {
        // Initialize program
        Program program = new Program();
        program.name = name;
        program.strict = strict;

        // Initialize temporary counter
        int nVar = 0;

        // Create start variable of program
        Variable lastVar = new Variable(String.format("t%d", nVar++));
        program.statements.add(new Assignment(lastVar, data.init));
        program.statements.add(createPrintStatement(lastVar));

        // Create statement for each computation
        for (Computation comp : data.compList) {
            // Create variable computed by this statement
            Variable newVar = new Variable(String.format("t%d", nVar++));

            // Create statement
            Expression expr;
            switch (comp.op) {
                case OPERATOR:
                    expr = new BinaryOperation(comp.name, lastVar, comp.num);
                    break;
                case UNARY_FUNC:
                    expr = new MethodCall("Math." + comp.name, lastVar);
                    break;
                case BINARY_FUNC:
                    expr = new MethodCall("Math." + comp.name, lastVar, comp.num);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + comp.op);
            }
            program.statements.add(new Assignment(newVar, expr));

            // Print result of this step
            program.statements.add(createPrintStatement(newVar));

            // Update variable
            lastVar = newVar;
        }

        return program;
    }

    private static MethodCall createPrintStatement(Variable var) {
        return new MethodCall("System.out.println",
                new MethodCall("Long.toHexString",
                        new MethodCall("Double.doubleToRawLongBits", var)));
    }
}
