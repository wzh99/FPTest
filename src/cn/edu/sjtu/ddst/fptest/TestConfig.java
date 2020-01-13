package cn.edu.sjtu.ddst.fptest;

public class TestConfig {

    boolean strict;
    String jdkHome, compileOptions;
    String jreHome, runOptions;

    public TestConfig(boolean useStrict, String jdkHome, String compileOptions,
                      String jreHome, String runOptions) {
        this.strict = useStrict;
        this.jdkHome = jdkHome;
        this.compileOptions = compileOptions;
        this.jreHome = jreHome;
        this.runOptions = runOptions;
    }

    @Override
    public String toString() {
        return "TestConfig{" +
                "jdkHome='" + jdkHome + '\'' +
                ", compileOptions='" + compileOptions + '\'' +
                ", jreHome='" + jreHome + '\'' +
                ", runOptions='" + runOptions + '\'' +
                '}';
    }
}
