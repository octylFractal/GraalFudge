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
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "graalfudge",
    mixinStandardHelpOptions = true,
    version = "0.1.0-SNAPSHOT",
    description = "Runs a brainf*** file in the GraalVM"
)
public class GraalFudgeLauncher implements Callable<Integer> {
    private static final String LANG_ID = "graalfudge";

    public static void main(String[] args) {
        System.exit(
            new CommandLine(new GraalFudgeLauncher()).execute(args)
        );
    }

    @CommandLine.Parameters(index = "0", description = "The file to run.")
    private Path file;

    @Override
    public Integer call() throws IOException {
        if (!Engine.create().getLanguages().containsKey(LANG_ID)) {
            throw new IllegalStateException("Missing " + LANG_ID + " in GraalVM runtime");
        }
        var source = Source.newBuilder(LANG_ID, file.toFile()).build();
        try (var ctx = Context.newBuilder(LANG_ID)
            .in(System.in)
            .out(System.out)
            .build()) {
            ctx.eval(source);
        }
        return 0;
    }
}
