package net.octyl.graalfudge.benchmark;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.InputStream;
import java.io.OutputStream;

@State(Scope.Thread)
public class GraalFudgeBenchmark {

    private static final String LANG_ID = "graalfudge";

    private static final String TEST_PROGRAM = """
        ++++[>+++++<-]>[<+++++>-]+<+[
            >[>+>+<<-]++>>[<<+>>-]>>>[-]++>[-]+
            >>>+[[-]++++++>>>]<<<[[<++++++++<++>>-]+<.<[>----<-]<]
            <<[>>>>>[>>>[-]+++++++++<[>-<-]+++++++++>[-[<->-]+[<<<]]<[>+<-]>]<<-]<<-
        ]
        """;

    Context context;
    Value value;

    @Setup
    public void compile() {
        context = Context.newBuilder(LANG_ID)
            .in(InputStream.nullInputStream())
            .out(OutputStream.nullOutputStream())
            .build();
        value = context.parse(LANG_ID, TEST_PROGRAM);
    }

    @TearDown
    public void tearDown() {
        value = null;
        context.close();
    }

    @Benchmark
    public Value execute() {
        return value.execute();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + GraalFudgeBenchmark.class.getSimpleName() + ".*")
            .warmupTime(TimeValue.valueOf("2s"))
            .warmupIterations(2)
            .measurementTime(TimeValue.valueOf("2s"))
            .measurementIterations(2)
            .forks(1)
            .jvmArgsAppend(
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+EnableJVMCI",
                "-XX:+UseJVMCICompiler",
                "-Dgraalvm.locatorDisabled=true"
            )
            .jvm("/usr/lib/jvm/java-16-graalvm/bin/java")
            .addProfiler(LinuxPerfAsmProfiler.class)
            .build();

        new Runner(opt).run();
    }
}
