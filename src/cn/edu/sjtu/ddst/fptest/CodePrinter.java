package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.ast.*;

import java.io.*;
import java.util.ArrayList;

public class CodePrinter {

    private static final String NAME_TAG = "@NAME";
    private static final String STRICT_TAG = "@STRICTFP";
    private static final String STATEMENT_TAG = "@STATEMENTS";
    private static final String METHOD_TAG = "@METHODS";

    private static final String template =
            "public " + STRICT_TAG + "class " + NAME_TAG + " {\n"
                    + "\tpublic static void main(String[] args) {\n"
                    + STATEMENT_TAG
                    + "\t}\n\n"
                    + METHOD_TAG
                    + "}\n";

    private PrintWriter writer; // where to output program

    public CodePrinter(Writer out) {
        writer = new PrintWriter(out);
    }

    public void print(Program program) {
        // Create program string from template
        String outStr = template.replace(NAME_TAG, program.name)
                .replace(STRICT_TAG, program.strict ? "strictfp " : "");

        // Print statements
        StringBuilder builder = new StringBuilder();
        for (Statement s : program.statements)
            builder.append(visit(s));
        outStr = outStr.replace(STATEMENT_TAG, builder.toString());

        // Print methods
        builder = new StringBuilder();
        for (Method m : program.methods)
            builder.append(visit(m));
        outStr = outStr.replace(METHOD_TAG, builder.toString());

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
                "\tprivate static double %s(double %s) {\n\t\treturn %s;\n\t}\n\n",
                method.getName(), visit(method.getParam()), visit(method.getExpr()));
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

    private String visit(Constant constant) {
        return constant.getLiteral();
    }

    private String visit(Variable var) {
        return var.getName();
    }

    private String visit(BinaryOperation binary) {
        return String.format("%s %s %s", visit(binary.getLhs()), binary.getOp(),
                visit(binary.getRhs()));
    }

    private String visit(MethodCall call) {
        StringBuilder argStr = new StringBuilder();
        ArrayList<Expression> argList = call.getArgs();
        for (int i = 0; i < argList.size(); i++) {
            if (i != 0) argStr.append(", ");
            argStr.append(visit(argList.get(i)));
        }
        return String.format("%s(%s)", call.getMethod(), argStr.toString());
    }

}
