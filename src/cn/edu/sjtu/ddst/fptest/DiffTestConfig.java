package cn.edu.sjtu.ddst.fptest;

public class DiffTestConfig {

    boolean strict;
    RunConfig run;

    public DiffTestConfig(boolean useStrict, String jdkHome, String compileOptions,
                          String jreHome, String runOptions) {
        this.strict = useStrict;
        this.run = new RunConfig(jdkHome, compileOptions, jreHome, runOptions);
    }

    @Override
    public String toString() {
        return "DiffTestConfig{" +
                "strict=" + strict +
                ", run=" + run +
                '}';
    }
}
