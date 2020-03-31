package cn.edu.sjtu.ddst.fptest;

import java.util.List;

public class MathFuncSpec {
    // Math functions
    public static final List<String> UNARY_LIST = List.of(
            "sin", "cos", "tan", "asin", "acos", "atan", "exp", "log", "log10", "sqrt", "cbrt",
            "ceil", "floor", "rint", "sinh", "cosh", "tanh", "expm1", "log1p", "nextUp"
    );

    public static final List<String> BINARY_LIST = List.of(
            "IEEEremainder", "atan2", "pow", "hypot", "nextAfter"
    );
}
