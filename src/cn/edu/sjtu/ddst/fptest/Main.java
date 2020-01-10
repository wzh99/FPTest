package cn.edu.sjtu.ddst.fptest;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {

    private static final long GENERATOR_SEED = 1;
    private static final int PROGRAM_LENGTH = 100;
    private static final int MIN_EXPONENT = 0;
    private static final int MAX_EXPONENT = 4;

    private static final String HOTSPOT_HOME =
            "C:\\Program Files\\Java\\jdk-11.0.2";
    private static final String J9_HOME =
            "C:\\Program Files\\AdoptOpenJDK\\jre-11.0.5.10-openj9";
    private static final String OLD_HOTSPOT_HOME =
            "C:\\Program Files\\Java\\jre1.6.0";

    private static final int TEST_ROUNDS = 10;

    public static void main(String[] args) {
        Tester tester = new Tester(Tester.TEST_MATH_FUNC, PROGRAM_LENGTH, GENERATOR_SEED,
                MIN_EXPONENT, MAX_EXPONENT);
        tester.test(new TestConfig[]{
                new TestConfig(ModifierScope.NONE, HOTSPOT_HOME, "--release 6",
                        HOTSPOT_HOME, ""),
                new TestConfig(ModifierScope.NONE, HOTSPOT_HOME, "--release 6",
                        OLD_HOTSPOT_HOME, ""),
        }, TEST_ROUNDS);
    }

    private static void dynamicRun(Program program) {
        // Write source code to string
        StringWriter writer = new StringWriter();
        CodePrinter printer = new CodePrinter(writer);
        printer.print(program);
        String source = writer.toString();

        // Compile to bytecode and load compiled class
        DynamicCompiler compiler = new DynamicCompiler();
        Class<?> testClass = compiler.compile(source);

        // Run testing method
        try {
            Method testMethod = testClass.getMethod("main", String[].class);
            testMethod.invoke(null, (Object) new String[]{});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
