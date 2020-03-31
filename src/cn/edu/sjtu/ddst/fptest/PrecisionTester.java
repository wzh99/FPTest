package cn.edu.sjtu.ddst.fptest;

import java.util.Random;

// Test precision of math library functions on different platforms.
public class PrecisionTester {
    private DataGen gen;
    private int len;

    public PrecisionTester(int programLen, long generatorSeed) {
        this.len = programLen;
        this.gen = new DataGen(generatorSeed);
    }

    public void run(RunConfig[] configs, int nRound) {
        // Generate list of functions and operands
    }
}
