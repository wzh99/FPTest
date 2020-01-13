package cn.edu.sjtu.ddst.fptest.syntax;

public class Method {

    String name;
    Variable param; // only accept one parameter for each method
    Expression expr;

    public Method(String name, Variable param, Expression expr) {
        this.name = name;
        this.param = param;
        this.expr = expr;
    }

    public String getName() { return name; }

    public Variable getParameter() { return param; }

    public Expression getExpression() { return expr; }

    @Override
    public String toString() {
        return "Method{" +
                "name='" + name + '\'' +
                ", param=" + param +
                ", expr=" + expr + '}';
    }
}
