package cn.edu.sjtu.ddst.fptest.ast;

public class BinaryOperation extends Expression {

    String operator;
    Expression lhs, rhs;

    public BinaryOperation(String operator, Expression lhs, Expression rhs) {
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String getOp() { return operator; }

    public Expression getLhs() { return lhs; }

    public Expression getRhs() { return rhs; }

    @Override
    public String toString() {
        return "BinaryOperation{" + "operator='" + operator + '\'' + ", lhs=" + lhs +
                ", rhs=" + rhs + '}';
    }
}
