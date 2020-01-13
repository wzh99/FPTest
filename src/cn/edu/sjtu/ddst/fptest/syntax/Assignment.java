package cn.edu.sjtu.ddst.fptest.syntax;

public class Assignment extends Statement {

    Variable lhs;
    Expression rhs;

    public Assignment(Variable lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Variable getLhs() { return lhs; }

    public Expression getRhs() { return rhs; }

    @Override
    public String toString() {
        return "Assignment{" + "lhs=" + lhs + ", rhs=" + rhs + '}';
    }
}
