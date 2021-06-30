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
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.util.InfiniteTape;

@NodeInfo(shortName = "changeCell")
public class GraalFudgeChangeCellNode extends GraalFudgeBuiltInNode implements GraalFudgeDeltaNode {
    private final int amount;

    public GraalFudgeChangeCellNode(SourceSection sourceSection, InfiniteTape tape, int amount) {
        super(sourceSection, tape);
        this.amount = amount;
    }

    @Override
    public int amount() {
        return amount;
    }

    @Override
    public void execute(VirtualFrame frame) {
        tape.changeCell(frame, amount);
    }

    @Override
    public String toString() {
        return "GraalFudgeChangeCellNode{" +
            "amount=" + amount +
            '}';
    }
}
