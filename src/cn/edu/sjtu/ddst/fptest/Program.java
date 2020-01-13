package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.syntax.Method;
import cn.edu.sjtu.ddst.fptest.syntax.Statement;

import java.util.ArrayList;

public class Program {

    String name;
    ArrayList<Statement> statements = new ArrayList<>();
    ArrayList<Method> methods = new ArrayList<>();
    boolean strict = false;

    @Override
    public String toString() {
        return "Program{" + "statements=" + statements + ", methods=" + methods +
                ", strict=" + strict + '}';
    }
}
