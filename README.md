# Java 虚拟机浮点运算差异自动测试

## 简介

本项目实现了对 Java 虚拟机（JVM）浮点运算差异的自动测试，其可以自动生成测试程序并调用给定的 JVM 运行测试程序，给出差异报告。

## 运行环境

编译并运行该测试工具需要 JDK 9 及以上版本，待测试的 JVM 需要支持 Java SE 6 及以上标准。

## 使用说明

1. 新建一个 Java 源文件，导入 `cn.edu.sjtu.ddst.fptest.*`；

2. 初始化一个 `Tester` 对象，其构造函数为：

    ```java
    public Tester(int testRange, int programLength, long generatorSeed);
    ```

    - `testRange` 指定了测试的范围，可以为 `Tester.TEST_OPERATOR` （测试运算符）、`Tester.TEST_MATH_FUNC`（测试标准库中数学函数）或 `Tester.TEST_ALL` （测试运算符和数学函数）；
    - `programLength` 指定了单个测试程序中的语句数，每一行语句执行一次运算符或数学函数的运算；
    - `generatorSeed` 指定了代码生成器的种子。

3. 在 `Tester` 对象上调用 `test` 方法：

    ```java
    public void test(TestConfig[] configs, int rounds);
    ```

    - `configs` 指定了测试的配置列表，在每轮测试中会为每个配置生成测试程序并执行，`TestConfig` 的构造函数为

        ```java
        public TestConfig(boolean useStrict, String jdkHome, String compileOptions,String jreHome, String runOptions)
        ```

        - `useStrict` 指定是否需要为测试程序加上 `strictfp` 修饰符；
        - `jdkHome` 指定了用于编译测试程序的 JDK 主目录；
        - `compileOptions` 指定了编译测试程序的命令行参数选项；
        - `jreHome` 指定了用于运行测试程序的 JRE 主目录；
        - `runOptions` 指定了运行测试程序的命令行参数选项。

    - `rounds` 指定了测试的轮数。

4. 在 `out/${时间戳}` 目录下存储了此次测试的若干文件：

    - `test.log` 记录了本次测试的配置信息及所检测到的输出结果差异；
    - `Test${测试序号}Config${配置序号}.java` 记录了在该轮测试的该项配置下所生成的 Java 源文件，共有测试轮数×配置数个源文件。

[Main.java](src/cn/edu/sjtu/ddst/fptest/Main.java) 中展示了一个使用示例可供参考。