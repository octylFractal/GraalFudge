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

package net.octyl.graalfudge.language.node;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.octyl.graalfudge.language.GraalFudgeContext;

import java.io.IOException;
import java.io.UncheckedIOException;

@NodeInfo(shortName = "printCell")
public class GraalFudgePrintCellNode extends GraalFudgeBuiltInNode {
    @Override
    public void execute(VirtualFrame frame) {
        var context = useContext();
        byte b = context.tape().readCell();
        writeToOutput(context, b);
    }

    @CompilerDirectives.TruffleBoundary
    private void writeToOutput(GraalFudgeContext context, byte b) {
        try {
            context.output().write(b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
