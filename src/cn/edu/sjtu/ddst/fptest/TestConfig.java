package cn.edu.sjtu.ddst.fptest;

public class TestConfig {

    ModifierScope scope;
    String jdkHome, compileOptions;
    String jreHome, runOptions;

    public TestConfig(ModifierScope scope, String jdkHome, String compileOptions,
                      String jreHome, String runOptions) {
        this.scope = scope;
        this.jdkHome = jdkHome;
        this.compileOptions = compileOptions;
        this.jreHome = jreHome;
        this.runOptions = runOptions;
    }
}
