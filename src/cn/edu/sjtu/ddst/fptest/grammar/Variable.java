package cn.edu.sjtu.ddst.fptest.grammar;

public class Variable extends Expression {

    String name;

    public Variable(String name) { this.name = name; }

    public String getName() { return name; }

    @Override
    public String toString() {
        return "Variable{" + "name='" + name + '\'' + '}';
    }
}
