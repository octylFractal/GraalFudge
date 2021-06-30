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

import com.oracle.truffle.api.frame.VirtualFrame;
import net.octyl.graalfudge.language.util.InfiniteTape;

import java.util.Arrays;

public record WutlangDebugInfo(
    byte[] buffer
) {
    public static WutlangDebugInfo getDebugInfo(InfiniteTape tape, VirtualFrame frame) {
        return new WutlangDebugInfo(
            Arrays.copyOf(tape.buffer(frame), tape.maximumIndexUsed(frame) + 1)
        );
    }
}
