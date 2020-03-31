package cn.edu.sjtu.ddst.fptest;

import cn.edu.sjtu.ddst.fptest.ast.Constant;

import java.util.ArrayList;

class ProgramData {
    Constant init;
    ArrayList<Computation> compList;

    public ProgramData(Constant init, ArrayList<Computation> computations) {
        this.init = init;
        this.compList = computations;
    }

    @Override
    public String toString() {
        return "ProgramData{" +
                "start=" + init +
                ", computations=" + compList +
                '}';
    }
}

class Computation {
    Operation op;
    String name;
    Constant num;

    public Computation(Operation op, String name, Constant num) {
        this.op = op;
        this.name = name;
        this.num = num;
    }

    @Override
    public String toString() {
        return "Computation{" +
                "op=" + op +
                ", name='" + name + '\'' +
                ", num=" + num +
                '}';
    }
}

enum Operation {
    OPERATOR,
    UNARY_FUNC,
    BINARY_FUNC
}
