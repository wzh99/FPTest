package cn.edu.sjtu.ddst.fptest;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Tester {

    // Testing range
    public static final int TEST_OPERATOR = 1;
    public static final int TEST_MATH_FUNC = 1 << 1;
    public static final int TEST_ALL = TEST_OPERATOR | TEST_MATH_FUNC;

    private int range;
    private long seed;
    private int length;

    public Tester(int testRange, int programLength, long generatorSeed) {
        this.range = testRange;
        this.length = programLength;
        this.seed = generatorSeed;
    }

    private static final String OUTPUT_DIR = "./out";

    public void test(TestConfig[] configs, int rounds) {
        // Initialize generator list
        if (configs.length == 0)
            throw new RuntimeException("Must specify at least one testing configuration");
        Generator[] generators = new Generator[configs.length];
        for (int i = 0; i < configs.length; i++)
            generators[i] = new Generator(seed);

        // Create output directory
        Path outPath = Path.of(OUTPUT_DIR);
        if (!outPath.toFile().exists()) {
            boolean success = outPath.toFile().mkdir();
            if (!success)
                throw new RuntimeException("Cannot create output directory " + OUTPUT_DIR);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Path testDir = outPath.resolve(dateFormat.format(new Date()));
        if (!testDir.toFile().exists()) {
            boolean success = testDir.toFile().mkdir();
            if (!success)
                throw new RuntimeException("Cannot create test directory " + testDir.toString());
        }

        // Write test log
        FileWriter writer;
        Path logPath = testDir.resolve("test.log");
        try {
            writer = new FileWriter(logPath.toString());
            for (TestConfig config : configs)
                writer.write(config.toString() + '\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot write test configurations");
        }

        // Test for specified number of rounds
        int diffCount = 0;
        for (int i = 0; i < rounds; i++) {
            System.out.printf("\rProgress: %d/%d\r", i, rounds);

            // Initialize result array
            String[] outputs = new String[configs.length];

            // Generate and run according to specified configuration
            for (int j = 0; j < configs.length; j++) {
                String name = String.format("Test%dConfig%d", i, j);
                Program program = generators[j].generate(length, range, configs[j].strict, name);
                String result = runOneProgram(program, testDir, configs[j]);
                if (result.isEmpty())
                    throw new RuntimeException("Empty output from test program");
                outputs[j] = result;
            }

            // Judge whether outputs of programs are all the same
            boolean equals = true;
            for (int j = 1; j < configs.length; j++)
                equals &= outputs[j].equals(outputs[0]);
            if (!equals) {
                // Print difference in long bits
                diffCount++;
                double[] fpRes = new double[configs.length];
                for (int j = 0; j < configs.length; j++)
                    fpRes[j] = Double.longBitsToDouble(Long.parseUnsignedLong(outputs[j], 16));
                String diffStr = String.format("Difference detected in round %d: %s (%s)\n",
                        i, Arrays.toString(outputs), Arrays.toString(fpRes));
                System.out.print(diffStr);
                try {
                    writer.write(diffStr);
                    writer.flush();
                } catch (IOException e) {
                    System.err.println("Cannot write to log file " + logPath.toString());
                    e.printStackTrace();
                }
            }
        }

        System.out.printf("Count: %d/%d\n", diffCount, rounds);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String runOneProgram(Program program, Path testDir, TestConfig config) {
        // Write source code to file
        String srcName = program.name + ".java";
        File srcFile = testDir.resolve(srcName).toFile();
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
                    srcName);
            Process proc = Runtime.getRuntime().exec(command, null, testDir.toFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to compile source file.");
        }

        // Run compiled class in specified JVM
        Path jvmPath =  Path.of(config.jreHome, "bin" , "java");
        StringBuilder output = new StringBuilder();
        try {
            String command = String.format("%s %s %s", jvmPath.toString(), config.runOptions,
                    program.name);
            Process proc = Runtime.getRuntime().exec(command, null, testDir.toFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                output.append(line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run compiled class.");
        }

        // Clean up
        if (!testDir.resolve(program.name + ".class").toFile().delete())
            System.err.println("Failed to delete class file");

        return output.toString();
    }
}
