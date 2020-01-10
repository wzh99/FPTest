package cn.edu.sjtu.ddst.fptest;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicCompiler {

    private HashMap<String, BytecodeFileObject> bytecodeMap = new HashMap<>();

    public Class<?> compile(String source) throws RuntimeException {
        // Dynamically compile to bytecode
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        BytecodeManager manager = new BytecodeManager(compiler.getStandardFileManager(null,
                null, null));
        String fullName = getFullClassName(source);
        SourceFileObject fileObj = new SourceFileObject(fullName, source);
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null,
                null, null, Collections.singletonList(fileObj));
        if (!task.call())
            throw new RuntimeException("Failed to compile program.");

        // Load class from compiled source
        try {
            return new CompiledBytesClassLoader().findClass(fullName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load compiled class.");
        }
    }

    private static String getFullClassName(String sourceCode) {
        // Get package name
        String className = "";
        Pattern pattern = Pattern.compile("package\\s+\\S+\\s*;");
        Matcher matcher = pattern.matcher(sourceCode);
        if (matcher.find()) {
            className = matcher.group().replaceFirst("package", "")
                    .replace(";", "").trim() + ".";
        }

        // Get class name
        pattern = Pattern.compile("class\\s+\\S+\\s+\\{");
        matcher = pattern.matcher(sourceCode);
        if (matcher.find()) {
            className += matcher.group().replaceFirst("class", "")
                    .replace("{", "").trim();
        }
        return className;
    }

    private class BytecodeManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

        BytecodeManager(StandardJavaFileManager fileManager) { super(fileManager); }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) {
            BytecodeFileObject javaFileObject = new BytecodeFileObject(className, kind);
            bytecodeMap.put(className, javaFileObject);
            return javaFileObject;
        }
    }

    private static class SourceFileObject extends SimpleJavaFileObject {

        private String content;

        SourceFileObject(String fullName, String content) {
            super(URI.create("string:///" + fullName.replaceAll("\\.", "/") +
                    Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) { return content; }
    }

    private static class BytecodeFileObject extends SimpleJavaFileObject {

        private ByteArrayOutputStream out;

        public BytecodeFileObject(String className, Kind kind) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") +
                    Kind.SOURCE.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() {
            out = new ByteArrayOutputStream();
            return out;
        }

        public byte[] getCompiledBytes() { return out.toByteArray(); }
    }

    private class CompiledBytesClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            BytecodeFileObject fileObject = bytecodeMap.get(name);
            if (fileObject != null) {
                byte[] bytes = fileObject.getCompiledBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }
    }
}
