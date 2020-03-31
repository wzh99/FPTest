package cn.edu.sjtu.ddst.fptest.ast;

public class Constant extends Expression {

    double value;
    String literal;

    public Constant(String literal) {
        this.literal = literal;
        this.value = Double.parseDouble(literal);
    }

    public Constant(double value) {
        this.value = value;
        this.literal = Double.toString(value);
    }

    public double getValue() { return value; }

    public String getLiteral() { return literal; }

    @Override
    public String toString() {
        return "Constant{" +
                "value=" + value +
                ", literal='" + literal + '\'' +
                '}';
    }
}
