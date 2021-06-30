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

package net.octyl.graalfudge.language.bf.parser;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.bf.node.GraalFudgeChangeCellNode;
import net.octyl.graalfudge.language.bf.node.GraalFudgeDeltaNode;
import net.octyl.graalfudge.language.bf.node.GraalFudgeMoveDataPointerNode;
import net.octyl.graalfudge.language.bf.node.GraalFudgeStatementNode;
import net.octyl.graalfudge.language.util.InfiniteTape;

import java.util.List;

class GraalFudgeOptimizer {
    private final Source source;
    private final InfiniteTape tape;
    private final List<GraalFudgeStatementNode> children;

    GraalFudgeOptimizer(Source source, InfiniteTape tape, List<GraalFudgeStatementNode> children) {
        this.source = source;
        this.tape = tape;
        this.children = children;
    }

    GraalFudgeStatementNode[] optimize() {
        squishDeltaNodes(GraalFudgeMoveDataPointerNode.class, GraalFudgeMoveDataPointerNode::new);
        squishDeltaNodes(GraalFudgeChangeCellNode.class, GraalFudgeChangeCellNode::new);
        return children.toArray(new GraalFudgeStatementNode[0]);
    }

    @FunctionalInterface
    private interface DeltaNodeConstructor<T extends GraalFudgeStatementNode & GraalFudgeDeltaNode> {
        T construct(SourceSection sourceSection, InfiniteTape tape, int amount);
    }

    private <T extends GraalFudgeStatementNode & GraalFudgeDeltaNode> void squishDeltaNodes(
        Class<T> nodeType, DeltaNodeConstructor<T> constructor
    ) {
        int runStart = -1;
        int counter = 0;
        int sourceStart = 0;
        int sourceEnd = 0;
        for (int i = 0; i < children.size(); i++) {
            var c = children.get(i);
            if (runStart == -1) {
                if (nodeType.isInstance(c)) {
                    runStart = i;
                    counter = nodeType.cast(c).amount();
                    sourceStart = nodeType.cast(c).getSourceSection().getCharIndex();
                }
            } else {
                if (nodeType.isInstance(c)) {
                    counter += nodeType.cast(c).amount();
                    sourceEnd = nodeType.cast(c).getSourceSection().getCharEndIndex();
                } else {
                    if (runStart + 1 < i) {
                        children.subList(runStart, i).clear();
                        i = runStart;
                        if (counter != 0) {
                            children.add(runStart, constructor.construct(
                                source.createSection(sourceStart, sourceEnd - sourceStart),
                                tape,
                                counter
                            ));
                            i++;
                        }
                    }
                    runStart = -1;
                }
            }
        }
    }
}
