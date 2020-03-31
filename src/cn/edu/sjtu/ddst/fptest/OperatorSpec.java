package cn.edu.sjtu.ddst.fptest;

import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

public class OperatorSpec {
    // Float operators
    public static final List<String> BINARY_LIST = List.of("+", "-", "*", "/");
    public static final Map<String, BinaryOperator<Double>> BINARY_MAP = Map.of(
            "+", Double::sum,
            "-", (Double a, Double b) -> a - b,
            "*", (Double a, Double b) -> a * b,
            "/", (Double a, Double b) -> a / b
    );
}
