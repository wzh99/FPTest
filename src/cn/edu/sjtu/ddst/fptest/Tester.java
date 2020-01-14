package cn.edu.sjtu.ddst.fptest;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Tester {

    // Testing range
    public static final int TEST_OPERATOR = 1;
    public static final int TEST_MATH_FUNC = 1 << 1;
    public static final int TEST_ALL = TEST_OPERATOR | TEST_MATH_FUNC;

    private int range;
    private long genSeed;
    private int programLen;

    public Tester(int testRange, int programLength, long generatorSeed) {
        this.range = testRange;
        this.programLen = programLength;
        this.genSeed = generatorSeed;
    }

    private static final String OUTPUT_DIR = "./out";

    public void test(TestConfig[] configs, int nRounds) {
        // Initialize generator list
        if (configs.length == 0)
            throw new RuntimeException("Must specify at least one testing configuration");
        Generator[] generators = new Generator[configs.length];
        for (int i = 0; i < configs.length; i++)
            generators[i] = new Generator(genSeed);

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
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot write test configurations");
        }

        // Test for specified number of rounds
        int diffCount = 0;
        for (int iRound = 0; iRound < nRounds; iRound++) {
            System.out.printf("\rProgress: %d/%d\r", iRound, nRounds);

            // Initialize result array
            String[][] outputs = new String[configs.length][programLen + 1];

            // Generate and run according to specified configuration
            for (int iConfig = 0; iConfig < configs.length; iConfig++) {
                String name = String.format("Test%dConfig%d", iRound, iConfig);
                Program program = generators[iConfig].generate(programLen, range,
                        configs[iConfig].strict, name);
                outputs[iConfig] = runOneProgram(program, testDir, configs[iConfig]);
            }

            // Judge whether outputs of programs are all the same
            boolean allEquals = true;
            for (int iConfig = 1; iConfig < configs.length; iConfig++)
                allEquals &= Arrays.equals(outputs[0], outputs[iConfig]);
            if (allEquals) continue;
            diffCount++;

            // Record difference
            String detectStr = String.format("Difference detected in round %d.\n", iRound);
            System.out.print(detectStr);
            try {
                writer.write(detectStr);
                writer.write("Details: \n");
                writer.write(printDetailedDifference(outputs));
                writer.write('\n');
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot write difference details.");
            }
        }

        System.out.printf("Count: %d/%d\n", diffCount, nRounds);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] runOneProgram(Program program, Path testDir, TestConfig config) {
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
        ArrayList<String> lines = new ArrayList<>(programLen + 1);
        try {
            String command = String.format("%s %s %s", jvmPath.toString(), config.runOptions,
                    program.name);
            Process proc = Runtime.getRuntime().exec(command, null, testDir.toFile());
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run compiled class.");
        }

        // Check if output lines are valid
        if (lines.size() != programLen + 1)
            throw new RuntimeException(
                    String.format("Expect %d lines of outout, got %d.", programLen + 1, lines.size())
            );
        String[] out = new String[programLen + 1];
        lines.toArray(out);

        // Clean up
        if (!testDir.resolve(program.name + ".class").toFile().delete())
            System.err.println("Failed to delete class file");

        return out;
    }

    private static String printDetailedDifference(String[][] outputs) {
        // Find where the difference begins
        boolean[] configEq = new boolean[outputs[0].length];
        int begin = -1;
        for (int iState = 0; iState < outputs[0].length; iState++) {
            boolean allEq = true;
            for (int iConfig = 1; iConfig < outputs.length; iConfig++) {
                allEq &= outputs[0][iState].equals(outputs[iConfig][iState]);
            }
            configEq[iState] = allEq;
            if (!allEq && begin < 0) begin = iState;
        }

        // Tabulate all the results from the where the difference begins
        StringBuilder builder = new StringBuilder();
        for (int iState = Math.max(0, begin - 1); iState < outputs[0].length; iState++) {
            StringBuilder line = new StringBuilder(String.format("%1$8d\t", iState));
            for (String[] output : outputs) {
                String result = output[iState];
                double value = Double.longBitsToDouble(Long.parseUnsignedLong(result, 16));
                line.append(String.format("%1$16s(%2$24s)\t", result, value));
            }
            builder.append(configEq[iState] ? ' ' : '>').append(line).append('\n');
        }

        return builder.toString();
    }
}
