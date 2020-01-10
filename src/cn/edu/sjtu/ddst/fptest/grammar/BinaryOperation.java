package cn.edu.sjtu.ddst.fptest.grammar;

public class BinaryOperation extends Expression {

    String operator;
    Expression left, right;

    public BinaryOperation(String operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public String getOperator() { return operator; }

    public Expression getLeftOperand() { return left; }

    public Expression getRightOperand() { return right; }

    @Override
    public String toString() {
        return "BinaryOperation{" + "operator='" + operator + '\'' + ", left=" + left +
                ", right=" + right + '}';
    }
}
