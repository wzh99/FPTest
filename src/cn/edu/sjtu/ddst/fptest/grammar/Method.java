package cn.edu.sjtu.ddst.fptest.grammar;

public class Method {

    String name;
    Variable param; // only accept one parameter for each method
    Expression expr;
    boolean strict;

    public Method(String name, Variable param, Expression expr, boolean strict) {
        this.name = name;
        this.param = param;
        this.expr = expr;
        this.strict = strict;
    }

    public String getName() { return name; }

    public Variable getParameter() { return param; }

    public Expression getExpression() { return expr; }

    public boolean isStrict() { return strict; }

    @Override
    public String toString() {
        return "Method{" + "name='" + name + '\'' + ", param=" + param + ", expr=" + expr +
                ", strict=" + strict + '}';
    }
}
