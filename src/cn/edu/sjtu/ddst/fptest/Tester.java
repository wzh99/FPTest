package cn.edu.sjtu.ddst.fptest;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;

public class Tester {

    // Testing range
    public static final int TEST_OPERATOR = 1;
    public static final int TEST_MATH_FUNC = 1 << 1;
    public static final int TEST_ALL = TEST_OPERATOR | TEST_MATH_FUNC;

    private int range;
    private long seed;
    private int length;
    private int minExp;
    private int maxExp;

    public Tester(int testRange, int programLength, long generatorSeed, int minExponent,
                  int maxExponent) {
        this.range = testRange;
        this.length = programLength;
        this.seed = generatorSeed;
        this.minExp = minExponent;
        this.maxExp = maxExponent;
    }

    private static final String OUTPUT_PATH = "./out";

    public void test(TestConfig[] configs, int rounds) {
        // Initialize generator list
        if (configs.length == 0)
            throw new RuntimeException("Must specify at least one testing configuration");
        Generator[] generators = new Generator[configs.length];
        for (int i = 0; i < configs.length; i++)
            generators[i] = new Generator(seed, minExp, maxExp);

        // Test for specified number of rounds
        int eqCount = 0;
        System.out.println();
        for (int i = 0; i < rounds; i++) {
            System.out.printf("\rProgress: %d/%d\r", i, rounds);

            // Initialize result array
            String[] outputs = new String[configs.length];

            // Generate and run according to specified configuration
            Program sampleProgram = null;
            for (int j = 0; j < configs.length; j++) {
                Program program = generators[j].generate(length, range, configs[j].scope);
                if (j == 0) sampleProgram = program;
                String result = runOneProgram(program, configs[j]);
                if (result.isEmpty())
                    throw new RuntimeException("Empty output from test program");
                outputs[j] = result;
            }

            // Judge whether outputs of programs are all the same
            boolean equals = true;
            for (int j = 1; j < configs.length; j++)
                equals &= outputs[j].equals(outputs[0]);
            if (equals)
                eqCount++;
            else {
                // Print difference in long bits
                double[] fpRes = new double[configs.length];
                for (int j = 0; j < configs.length; j++)
                    fpRes[j] = Double.longBitsToDouble(Long.parseUnsignedLong(outputs[j], 16));
                System.out.printf("Difference found: %s (%s)\n", Arrays.toString(outputs),
                        Arrays.toString(fpRes));

                // Write program with difference to file
                String srcPath = Path.of(OUTPUT_PATH, String.format("Test$%d.java", i)).toString();
                try {
                    FileWriter writer = new FileWriter(srcPath);
                    CodePrinter printer = new CodePrinter(writer);
                    printer.print(sampleProgram);
                    writer.close();
                    System.out.println("Test program saved to " + srcPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create file " + srcPath);
                }

            }
        }

        System.out.printf("Result: %d/%d\n", eqCount, rounds);
    }

    private static String runOneProgram(Program program, TestConfig config) {
        // Write source code to file
        String srcPath = Path.of(OUTPUT_PATH, "Test.java").toString();
        File srcFile = new File(srcPath);
        try {
            FileWriter writer = new FileWriter(srcFile);
            CodePrinter printer = new CodePrinter(writer);
            printer.print(program);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to write source code to file.");
        }

        // Compile with default Java compiler (javac)
        Path compilerPath = Path.of(config.jdkHome, "bin", "javac");
        try {
            String command = String.format("%s %s %s", compilerPath.toString(), config.compileOptions,
                    "Test.java");
            Process proc = Runtime.getRuntime().exec(command, null, new File(OUTPUT_PATH));
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                System.err.println(line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to compile source file.");
        }

        // Run compiled class in specified JVM
        Path jvmPath =  Path.of(config.jreHome, "bin" , "java");
        StringBuilder output = new StringBuilder();
        try {
            String command = String.format("%s %s %s", jvmPath.toString(), config.runOptions,
                    "Test");
            Process proc = Runtime.getRuntime().exec(command, null, new File(OUTPUT_PATH));
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                output.append(line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run compiled class.");
        }

        // Clean up
        if (!srcFile.delete())
            System.err.println("Failed to delete source file");
        if (!new File(Path.of(OUTPUT_PATH, "Test.class").toString()).delete())
            System.err.println("Failed to delete class file");

        return output.toString();
    }
}
