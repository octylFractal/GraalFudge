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
        record ProtoGroupNode(int start, List<GraalFudgeStatementNode> children) {
        }
        var groupNodeStack = new ArrayDeque<ProtoGroupNode>();
        groupNodeStack.addLast(new ProtoGroupNode(0, new ArrayList<>()));
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '>' -> groupNodeStack.getLast().children.add(new GraalFudgeNextCellNode(
                    source.createSection(i, 1)
                ));
                case '<' -> groupNodeStack.getLast().children.add(new GraalFudgePrevCellNode(
                    source.createSection(i, 1)
                ));
                case '+' -> groupNodeStack.getLast().children.add(new GraalFudgeIncrementCellNode(
                    source.createSection(i, 1)
                ));
                case '-' -> groupNodeStack.getLast().children.add(new GraalFudgeDecrementCellNode(
                    source.createSection(i, 1)
                ));
                case '.' -> groupNodeStack.getLast().children.add(new GraalFudgePrintCellNode(
                    source.createSection(i, 1)
                ));
                case ',' -> groupNodeStack.getLast().children.add(new GraalFudgeReadCellNode(
                    source.createSection(i, 1)
                ));
                // push onto stack
                case '[' -> groupNodeStack.addLast(new ProtoGroupNode(
                    i, new ArrayList<>()
                ));
                // pop off stack, add new loop node
                case ']' -> {
                    var loopBody = groupNodeStack.removeLast();
                    if (groupNodeStack.isEmpty()) {
                        throw new IllegalStateException("Missing '[' bracket");
                    }
                    var loopSourceSection = source.createSection(
                        loopBody.start, i - loopBody.start + 1
                    );
                    var loopBodySourceSection = source.createSection(
                        loopBody.start + 1, Math.max(i - loopBody.start - 1, 0)
                    );
                    groupNodeStack.getLast().children.add(
                        new GraalFudgeLoopNode(
                            loopSourceSection,
                            new GraalFudgeGroupNode(
                                loopBodySourceSection,
                                false,
                                loopBody.children.toArray(new GraalFudgeStatementNode[0])
                            )
                        )
                    );
                }
                default -> {
                    // unknown characters are skipped
                }
            }
        }
        var statements = groupNodeStack.removeLast().children.toArray(new GraalFudgeStatementNode[0]);
        int startOfAllStatements = statements[0].getSourceSection().getCharIndex();
        int endOfAllStatements = statements[statements.length - 1].getSourceSection().getCharEndIndex();
        var rootNode = new GraalFudgeRootNode(
            language,
            new GraalFudgeGroupNode(
                source.createSection(startOfAllStatements, endOfAllStatements - startOfAllStatements),
                true,
                statements
            )
        );
        if (!groupNodeStack.isEmpty()) {
            throw new IllegalStateException("Missing ']' bracket");
        }
        return rootNode;
    }
}
