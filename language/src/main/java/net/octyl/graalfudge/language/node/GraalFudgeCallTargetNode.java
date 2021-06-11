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

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.util.InfiniteTape;

/**
 * An indirection to introduce a new root node at this location.
 */
@NodeInfo(shortName = "Call target")
@GenerateWrapper
public class GraalFudgeCallTargetNode extends GraalFudgeStatementNode {
    private final InfiniteTape tape;
    private final RootCallTarget callTarget;

    public GraalFudgeCallTargetNode(SourceSection sourceSection, InfiniteTape tape, RootCallTarget callTarget) {
        super(sourceSection);
        this.tape = tape;
        this.callTarget = callTarget;
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probeNode) {
        return new GraalFudgeCallTargetNodeWrapper(getSourceSection(), tape, callTarget, this, probeNode);
    }

    @Override
    public void execute(VirtualFrame frame) {
        var dataPointer = (int) callTarget.call(tape.buffer(frame), tape.dataPointer(frame));
        tape.setDataPointer(frame, dataPointer);
    }
}
