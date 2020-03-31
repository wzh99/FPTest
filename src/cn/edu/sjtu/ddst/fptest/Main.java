package cn.edu.sjtu.ddst.fptest;

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

    public static void main(String[] args) {
        DiffTester tester = new DiffTester(TestRange.ALL, PROGRAM_LENGTH, GENERATOR_SEED);
        tester.run(new DiffTestConfig[]{
                new DiffTestConfig(true, HOTSPOT_HOME, "--release 6",
                        HOTSPOT_HOME, ""),
                new DiffTestConfig(false, HOTSPOT_HOME, "--release 6",
                        HOTSPOT_HOME, ""),
                new DiffTestConfig(true, HOTSPOT_HOME, "--release 6",
                        J9_HOME, ""),
                new DiffTestConfig(false, HOTSPOT_HOME, "--release 6",
                        J9_HOME, ""),
                new DiffTestConfig(true, HOTSPOT_HOME, "--release 6",
                        OLD_HOTSPOT_HOME, ""),
                new DiffTestConfig(false, HOTSPOT_HOME, "--release 6",
                        OLD_HOTSPOT_HOME, ""),
        }, TEST_ROUNDS);
        /*tester.run(new DiffTestConfig[] {
                new DiffTestConfig(true, HOTSPOT_HOME, "--release 6",
                        HOTSPOT_HOME, "")
        }, 1);*/
    }
}
