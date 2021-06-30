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

package net.octyl.graalfudge.language.wutlang.node;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.util.InfiniteTape;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

@NodeInfo(shortName = "printCell")
public class WutlangPrintCellNode extends WutlangBuiltInNode {
    public WutlangPrintCellNode(SourceSection sourceSection, InfiniteTape tape) {
        super(sourceSection, tape);
    }

    @Override
    public void execute(VirtualFrame frame) {
         byte b = tape.readCell(frame);
         writeToOutput(useContext().output(), b);
    }

    @CompilerDirectives.TruffleBoundary
    private void writeToOutput(OutputStream stream, byte b) {
        try {
            stream.write(b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
