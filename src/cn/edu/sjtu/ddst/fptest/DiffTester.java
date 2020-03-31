package cn.edu.sjtu.ddst.fptest;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class DiffTester {
    private DataGen gen;
    private TestRange range;
    private int len;

    public DiffTester(TestRange range, int programLength, long generatorSeed) {
        this.range = range;
        this.len = programLength;
        this.gen = new DataGen(generatorSeed);
    }

    private static final String OUTPUT_DIR = "./out";

    public void run(DiffTestConfig[] configs, int nRound) {
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
            for (DiffTestConfig config : configs)
                writer.write(config.toString() + '\n');
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot write test configurations");
        }

        // Test for specified number of rounds
        int diffCount = 0;
        for (int iRound = 0; iRound < nRound; iRound++) {
            // Print testing progress
            System.out.printf("\rProgress: %d/%d\r", iRound, nRound);

            // Generate program data
            ProgramData data = gen.generate(range, len);

            // Initialize result array
            String[][] outputs = new String[configs.length][len + 1];

            // Generate and run according to specified configuration
            for (int i = 0; i < configs.length; i++) {
                String name = String.format("Test%dConfig%d", iRound, i);
                Program program = ProgramBuilder.build(data, name, configs[i].strict);
                outputs[i] = runOneProgram(program, testDir, configs[i].run);
            }

            // Judge whether outputs of programs are all the same
            boolean allEquals = true;
            for (int iConfig = 1; iConfig < configs.length; iConfig++)
                allEquals &= Arrays.equals(outputs[0], outputs[iConfig]);
            if (allEquals) continue;
            diffCount++;

            // Record difference
            String detectStr = String.format("Differences detected in round %d.\n", iRound);
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

        System.out.printf("Count: %d/%d\n", diffCount, nRound);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] runOneProgram(Program program, Path testDir, RunConfig config) {
        // Run the generated program
        ArrayList<String> lines = ProgramRunner.run(program, testDir, config);

        // Check if output lines are valid
        int expectedLen = len + 1;
        if (lines.size() != expectedLen) {
            throw new RuntimeException(
                    String.format("Expect %d lines of outout, got %d.", expectedLen, lines.size())
            );
        }
        String[] out = new String[expectedLen];
        lines.toArray(out);

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
        for (int iState = 0; iState < outputs[0].length; iState++) {
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
