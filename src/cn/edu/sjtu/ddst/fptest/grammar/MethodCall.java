package cn.edu.sjtu.ddst.fptest.grammar;

import java.util.ArrayList;
import java.util.Arrays;

public class MethodCall extends Expression {

    String method;
    ArrayList<Expression> args = new ArrayList<>();

    public MethodCall(String method, Expression... args) {
        this.method = method;
        this.args.addAll(Arrays.asList(args));
    }

    public String getMethod() { return method; }

    public ArrayList<Expression> getArguments() { return args; }

    @Override
    public String toString() {
        return "MethodCall{" + "method=" + method + ", arg=" + args + '}';
    }
}
