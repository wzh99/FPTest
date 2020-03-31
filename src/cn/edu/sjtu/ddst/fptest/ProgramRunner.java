package cn.edu.sjtu.ddst.fptest;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class ProgramRunner {
    // Runs a program and returns its output lines.
    public static ArrayList<String> run(Program program, Path testDir, RunConfig config) {
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
        ArrayList<String> lines = new ArrayList<>();
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

        // Clean up
        if (!testDir.resolve(program.name + ".class").toFile().delete())
            System.err.println("Failed to delete class file");

        return lines;
    }
}
