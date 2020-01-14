package cn.edu.sjtu.ddst.fptest;

import java.util.Random;

public class Main {

    private static final long GENERATOR_SEED = 1;
    private static final int PROGRAM_LENGTH = 20;
    private static final int TEST_ROUNDS = 100;

    private static final String HOTSPOT_HOME =
            "C:\\Program Files\\Java\\jdk-11.0.2";
    private static final String J9_HOME =
            "C:\\Program Files\\AdoptOpenJDK\\jre-11.0.5.10-openj9";
    private static final String OLD_HOTSPOT_HOME =
            "C:\\Program Files\\Java\\jre1.6.0";
    private static final String GRAAL_HOME =
            "D:\\graalvm-ce-java11-19.3.0.2";

    public static void main(String[] args) {
        Tester tester = new Tester(Tester.TEST_ALL, PROGRAM_LENGTH, GENERATOR_SEED);
        tester.test(new TestConfig[]{
                new TestConfig(false, HOTSPOT_HOME, "--release 6",
                        J9_HOME, ""),
                new TestConfig(false, HOTSPOT_HOME, "--release 6",
                        GRAAL_HOME, ""),
        }, TEST_ROUNDS);
    }
}
