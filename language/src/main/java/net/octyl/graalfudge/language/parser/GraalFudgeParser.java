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

package net.octyl.graalfudge.language.parser;

import com.oracle.truffle.api.source.Source;
import net.octyl.graalfudge.language.GraalFudgeLanguage;
import net.octyl.graalfudge.language.node.GraalFudgeDecrementCellNode;
import net.octyl.graalfudge.language.node.GraalFudgeGroupNode;
import net.octyl.graalfudge.language.node.GraalFudgeIncrementCellNode;
import net.octyl.graalfudge.language.node.GraalFudgeLoopNode;
import net.octyl.graalfudge.language.node.GraalFudgeNextCellNode;
import net.octyl.graalfudge.language.node.GraalFudgePrevCellNode;
import net.octyl.graalfudge.language.node.GraalFudgePrintCellNode;
import net.octyl.graalfudge.language.node.GraalFudgeReadCellNode;
import net.octyl.graalfudge.language.node.GraalFudgeRootNode;
import net.octyl.graalfudge.language.node.GraalFudgeStatementNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class GraalFudgeParser {
    private final GraalFudgeLanguage language;
    private final Source source;

    public GraalFudgeParser(GraalFudgeLanguage language, Source source) {
        this.language = language;
        this.source = source;
    }

    public GraalFudgeRootNode parse() {
        var text = source.getCharacters();
        // stack of group nodes, represented as a list of statements
        var groupNodeStack = new ArrayDeque<List<GraalFudgeStatementNode>>();
        groupNodeStack.addLast(new ArrayList<>());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '>' -> groupNodeStack.getLast().add(new GraalFudgeNextCellNode());
                case '<' -> groupNodeStack.getLast().add(new GraalFudgePrevCellNode());
                case '+' -> groupNodeStack.getLast().add(new GraalFudgeIncrementCellNode());
                case '-' -> groupNodeStack.getLast().add(new GraalFudgeDecrementCellNode());
                case '.' -> groupNodeStack.getLast().add(new GraalFudgePrintCellNode());
                case ',' -> groupNodeStack.getLast().add(new GraalFudgeReadCellNode());
                // push onto stack
                case '[' -> groupNodeStack.addLast(new ArrayList<>());
                // pop off stack, add new loop node
                case ']' -> {
                    var loopBody = groupNodeStack.removeLast();
                    if (groupNodeStack.isEmpty()) {
                        throw new IllegalStateException("Missing '[' bracket");
                    }
                    groupNodeStack.getLast().add(
                        new GraalFudgeLoopNode(new GraalFudgeGroupNode(
                            loopBody.toArray(new GraalFudgeStatementNode[0])
                        ))
                    );
                }
                default -> {
                    // unknown characters are skipped
                }
            }
        }
        var rootNode = new GraalFudgeRootNode(
            language,
            new GraalFudgeGroupNode(groupNodeStack.removeLast().toArray(new GraalFudgeStatementNode[0]))
        );
        if (!groupNodeStack.isEmpty()) {
            throw new IllegalStateException("Missing ']' bracket");
        }
        return rootNode;
    }
}
