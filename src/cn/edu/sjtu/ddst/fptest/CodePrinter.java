package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.grammar.*;

import java.io.*;
import java.util.ArrayList;

public class CodePrinter {

    private static final String strictTag = "@STRICTFP";
    private static final String statementTag = "@STATEMENTS";
    private static final String methodTag = "@METHODS";

    private static final String template =
            "public " + strictTag + "class Test {\n" +
            "\tpublic static void main(String[] args) {\n" +
            statementTag +
            "\t}\n\n" +
            methodTag +
            "}\n";

    private PrintWriter writer; // where to output program

    public CodePrinter(Writer out) {
        writer = new PrintWriter(out);
    }

    public void print(Program program) {
        // Create program string from template
        String outStr = template.replace(strictTag, program.strict ? "strictfp " : "");

        // Print statements
        StringBuilder builder = new StringBuilder();
        for (Statement s : program.statements)
            builder.append(visit(s));
        outStr = outStr.replace(statementTag, builder.toString());

        // Print methods
        builder = new StringBuilder();
        for (Method m : program.methods)
            builder.append(visit(m));
        outStr = outStr.replace(methodTag, builder.toString());

        // Write to output writer
        writer.print(outStr);
        writer.flush();
    }

    private String visit(Statement statement) {
        String result = "";
        if (statement instanceof Assignment)
            result = visit((Assignment) statement);
        else if (statement instanceof Expression)
            result = visit((Expression) statement);
        return "\t\t" + result + ";\n";
    }

    private String visit(Assignment assignment) {
        return String.format("double %s = %s", visit(assignment.getLhs()),
                visit(assignment.getRhs()));
    }

    private String visit(Method method) {
        return String.format(
                "\tprivate static %sdouble %s(double %s) {\n\t\treturn %s;\n\t}\n\n",
                method.isStrict() ? "strictfp " : "", method.getName(), visit(method.getParameter()),
                visit(method.getExpression()));
    }

    private String visit(Expression expr) {
        if (expr instanceof Constant)
            return visit((Constant) expr);
        else if (expr instanceof Variable)
            return visit((Variable) expr);
        else if (expr instanceof BinaryOperation)
            return visit((BinaryOperation) expr);
        else if (expr instanceof MethodCall)
            return visit((MethodCall) expr);
        return "";
    }

    private String visit(Constant constant) { return Double.toString(constant.getValue()); }

    private String visit(Variable var) { return var.getName(); }

    private String visit(BinaryOperation binary) {
        return String.format("%s %s %s", visit(binary.getLeftOperand()), binary.getOperator(),
                visit(binary.getRightOperand()));
    }

    private String visit(MethodCall call) {
        StringBuilder argStr = new StringBuilder();
        ArrayList<Expression> argList = call.getArguments();
        for (int i = 0; i < argList.size(); i++) {
            if (i != 0) argStr.append(", ");
            argStr.append(visit(argList.get(i)));
        }
        return String.format("%s(%s)", call.getMethod(), argStr.toString());
    }

}
