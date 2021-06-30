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

package net.octyl.graalfudge.language.wutlang;


import net.octyl.graalfudge.language.TestLanguageRunner;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

// Derived from the original WutlangSpecTest at https://github.com/me4502/wutlang
// Licensed under the MIT license (LICENSE.wutlang.txt)
public class WutlangSpecTest {

    private static Context createContext() {
        return TestLanguageRunner.createContext(WutlangLanguage.ID)
            .option(WutlangLanguage.ID + ".testMode", "true")
            .build();
    }

    private static WutlangDebugInfo eval(String name, String source) throws IOException {
        try (var ctx = createContext()) {
            var value = ctx.eval(
                Source.newBuilder(WutlangLanguage.ID, source, name).build()
            );
            return value.as(WutlangDebugInfo.class);
        }
    }

    @Test
    void testHeap() throws IOException {
        var debug = eval("heap.wl", "++>+>+++>++++");
        assertArrayEquals(
            new byte[]{2, 1, 3, 4},
            debug.buffer()
        );
    }

    @Test
    void testHeap2() throws IOException {
        var debug = eval("heap2.wl", "++--+>+----->+>--+");
        assertArrayEquals(
            new byte[]{1, -4, 1, -1},
            debug.buffer()
        );
    }

    @Test
    void testForLoop() throws IOException {
        var debug = eval("forLoop.wl", "++++++++++[-]");
        assertArrayEquals(
            new byte[]{0},
            debug.buffer()
        );
    }

    @Test
    void testNestedForLoop() throws IOException {
        var debug = eval("nestedForLoop.wl", "+++>+++<[>[-]<-]");
        assertArrayEquals(
            new byte[]{0, 0},
            debug.buffer()
        );
    }

    @Test
    void testNestedForLoop2() throws IOException {
        var debug = eval("nestedForLoop2.wl", ">++>++>++[[-]<]");
        assertArrayEquals(
            new byte[]{0, 0, 0, 0},
            debug.buffer()
        );
    }
}
