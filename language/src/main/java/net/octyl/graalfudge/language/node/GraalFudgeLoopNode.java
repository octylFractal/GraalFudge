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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

@NodeInfo(shortName = "loop")
public class GraalFudgeLoopNode extends GraalFudgeStatementNode {
    private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();
    @Child
    private GraalFudgeGroupNode bodyNode;

    public GraalFudgeLoopNode(GraalFudgeGroupNode bodyNode) {
        this.bodyNode = bodyNode;
    }

    @Override
    public void execute(VirtualFrame frame) {
        var tape = useContext().tape();
        while (loopProfile.profile(tape.readCell() != 0)) {
            bodyNode.execute(frame);
        }
    }
}
