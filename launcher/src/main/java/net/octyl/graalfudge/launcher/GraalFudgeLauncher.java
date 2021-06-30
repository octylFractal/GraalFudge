/*
 * Copyright (c) Octavia Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.octyl.graalfudge.launcher;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
    name = "graalfudge",
    mixinStandardHelpOptions = true,
    version = "0.1.0-SNAPSHOT",
    description = "Runs a Brainf*** or Wutlang file in the GraalVM"
)
public class GraalFudgeLauncher implements Callable<Integer> {
    public static void main(String[] args) {
        System.exit(
            new CommandLine(new GraalFudgeLauncher()).execute(args)
        );
    }

    @CommandLine.Parameters(index = "0", description = "The file to run.")
    private Path file;

    @CommandLine.Option(names = {"-p", "--profile"}, description = "Time the execution of the program")
    private boolean profile;

    @CommandLine.Option(names = {"-l", "--language"}, description = "Force language, will detect using the file by default")
    private String language;

    @CommandLine.Option(names = {"--experimental-options"}, description = "Allow Graal experimental options")
    private boolean experimentalOptions;

    @CommandLine.Parameters(index = "1..*", description = "Parameters for Graal")
    private List<String> remainingParams;

    @Override
    public Integer call() throws IOException {
        Map<String, String> options = new HashMap<>();
        for (String arg : Objects.requireNonNullElse(remainingParams, List.<String>of())) {
            if (!parseOption(options, arg)) {
                throw new IllegalStateException("Unrecognized argument: " + arg);
            }
        }
        var language = this.language != null ? this.language : Source.findLanguage(file.toFile());
        var source = Source.newBuilder(language, file.toFile()).build();
        try (var ctx = Context.newBuilder(language)
            .in(System.in)
            .out(System.out)
            .allowExperimentalOptions(experimentalOptions)
            .options(options)
            .build()) {
            Value v = ctx.parse(source);
            long startTime = 0L;
            if (profile) {
                startTime = System.nanoTime();
            }
            v.execute();
            if (profile) {
                long elapsed = System.nanoTime() - startTime;
                System.err.println("Took " + TimeUnit.NANOSECONDS.toMillis(elapsed) + "ms to run");
            }
        }
        return 0;
    }

    private static boolean parseOption(Map<String, String> options, String arg) {
        if (arg.length() <= 2 || !arg.startsWith("--")) {
            return false;
        }
        int eqIdx = arg.indexOf('=');
        String key;
        String value;
        if (eqIdx < 0) {
            key = arg.substring(2);
            value = null;
        } else {
            key = arg.substring(2, eqIdx);
            value = arg.substring(eqIdx + 1);
        }

        if (value == null) {
            value = "true";
        }
        options.put(key, value);
        return true;
    }
}
