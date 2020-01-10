package cn.edu.sjtu.ddst.fptest.grammar;

public class Constant extends Expression {

    double value;

    public Constant(String literal) {
        this.value = Double.parseDouble(literal);
    }

    public Constant(double value) {
        this.value = value;
    }

    public double getValue() { return value; }

    @Override
    public String toString() {
        return "Constant{" + "value=" + value + '}';
    }
}
