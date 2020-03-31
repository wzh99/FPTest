package cn.edu.sjtu.ddst.fptest;

public class RunConfig {

    String jdkHome, compileOptions;
    String jreHome, runOptions;

    public RunConfig(String jdkHome, String compileOptions, String jreHome, String runOptions) {
        this.jdkHome = jdkHome;
        this.compileOptions = compileOptions;
        this.jreHome = jreHome;
        this.runOptions = runOptions;
    }

    @Override
    public String toString() {
        return "RunConfig{" +
                "jdkHome='" + jdkHome + '\'' +
                ", compileOptions='" + compileOptions + '\'' +
                ", jreHome='" + jreHome + '\'' +
                ", runOptions='" + runOptions + '\'' +
                '}';
    }
}
