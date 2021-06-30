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
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import net.octyl.graalfudge.language.bf.GraalFudgeContext;
import net.octyl.graalfudge.language.util.InfiniteTape;
import net.octyl.graalfudge.language.wutlang.WutlangContext;
import net.octyl.graalfudge.language.wutlang.WutlangDebugInfo;
import net.octyl.graalfudge.language.wutlang.WutlangLanguage;

@NodeInfo(language = WutlangLanguage.NAME, description = "The root of a Wutlang tree")
public class WutlangRootNode extends RootNode {
    private final InfiniteTape tape;
    @Child
    private WutlangGroupNode groupNode;
    private final boolean returnDebugInfo;
    @CompilerDirectives.CompilationFinal
    private TruffleLanguage.ContextReference<WutlangContext> context;

    public WutlangRootNode(WutlangLanguage language, FrameDescriptor frameDescriptor, InfiniteTape tape,
                           WutlangGroupNode groupNode, boolean returnDebugInfo) {
        super(language, frameDescriptor);
        this.tape = tape;
        this.groupNode = groupNode;
        this.returnDebugInfo = returnDebugInfo;
    }

    protected WutlangContext useContext() {
        if (context == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            context = lookupContextReference(WutlangLanguage.class);
        }
        return context.get();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        tape.initialize(frame);
        groupNode.execute(frame);
        if (returnDebugInfo) {
            var ctx = useContext();
            return ctx.env().asGuestValue(WutlangDebugInfo.getDebugInfo(tape, frame));
        }
        return tape.materialize(frame);
    }
}
