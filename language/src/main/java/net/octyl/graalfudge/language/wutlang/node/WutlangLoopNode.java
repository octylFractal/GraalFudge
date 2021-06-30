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

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.util.InfiniteTape;
import net.octyl.graalfudge.language.util.InfiniteTapeObject;

@NodeInfo(shortName = "loop")
public class WutlangLoopNode extends WutlangStatementNode {
    @Child
    private LoopNode loopNode;

    public WutlangLoopNode(SourceSection sourceSection, InfiniteTape tape, DirectCallNode bodyNode) {
        super(sourceSection);
        this.loopNode = Truffle.getRuntime().createLoopNode(new WutlangLoopNodeInternal(tape, bodyNode));
    }

    @Override
    public void execute(VirtualFrame frame) {
        loopNode.execute(frame);
    }

    private static final class WutlangLoopNodeInternal extends Node implements RepeatingNode {
        private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();
        private final InfiniteTape tape;
        @Child
        private DirectCallNode bodyNode;

        private WutlangLoopNodeInternal(InfiniteTape tape, DirectCallNode bodyNode) {
            this.tape = tape;
            this.bodyNode = bodyNode;
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            if (loopProfile.profile(tape.readCell(frame) == 0)) {
                return false;
            }
            var materialized = (InfiniteTapeObject) bodyNode.call(tape.buffer(frame), tape.materialize(frame));
            tape.updateFrom(frame, materialized);
            return true;
        }
    }
}
