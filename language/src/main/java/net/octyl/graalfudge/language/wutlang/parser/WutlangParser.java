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

package net.octyl.graalfudge.language.wutlang.parser;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.source.Source;
import net.octyl.graalfudge.language.util.InfiniteTape;
import net.octyl.graalfudge.language.wutlang.WutlangLanguage;
import net.octyl.graalfudge.language.wutlang.node.WutlangChangeCellNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangGroupNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangLoopNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangMoveDataPointerNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangPrintCellNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangReadCellNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangRootNode;
import net.octyl.graalfudge.language.wutlang.node.WutlangStatementNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class WutlangParser {
    private final WutlangLanguage language;
    private final Source source;
    private final boolean testMode;

    public WutlangParser(WutlangLanguage language, Source source, boolean testMode) {
        this.language = language;
        this.source = source;
        this.testMode = testMode;
    }

    private record ProtoGroupNode(int start, List<WutlangStatementNode> children) {
    }

    public WutlangRootNode parse() {
        var lines = source.getCharacters().toString().lines().toList();
        var frameDescriptor = new FrameDescriptor();
        var tape = new InfiniteTape(frameDescriptor);
        var groupNodeStack = new ArrayDeque<ProtoGroupNode>();
        groupNodeStack.addLast(new ProtoGroupNode(0, new ArrayList<>()));
        for (var line : lines) {
            for (int i = 0; i < line.length(); i++) {
                if (!handleChar(frameDescriptor, tape, groupNodeStack, line, i)) {
                    break;
                }
            }
        }
        var statements = new WutlangOptimizer(source, tape, groupNodeStack.removeLast().children).optimize();
        int startOfAllStatements = statements[0].getSourceSection().getCharIndex();
        int endOfAllStatements = statements[statements.length - 1].getSourceSection().getCharEndIndex();
        var rootNode = new WutlangRootNode(
            language,
            frameDescriptor,
            tape,
            new WutlangGroupNode(
                source.createSection(startOfAllStatements, endOfAllStatements - startOfAllStatements),
                true,
                statements
            ),
            testMode
        );
        if (!groupNodeStack.isEmpty()) {
            throw new IllegalStateException("Missing ']' bracket");
        }
        return rootNode;
    }

    private boolean handleChar(FrameDescriptor frameDescriptor, InfiniteTape tape,
                               ArrayDeque<ProtoGroupNode> groupNodeStack, String line, int i) {
        char c = line.charAt(i);
        switch (c) {
            case '>' -> groupNodeStack.getLast().children.add(new WutlangMoveDataPointerNode(
                source.createSection(i, 1), tape, 1
            ));
            case '<' -> groupNodeStack.getLast().children.add(new WutlangMoveDataPointerNode(
                source.createSection(i, 1), tape, -1
            ));
            case '+' -> groupNodeStack.getLast().children.add(new WutlangChangeCellNode(
                source.createSection(i, 1), tape, 1
            ));
            case '-' -> groupNodeStack.getLast().children.add(new WutlangChangeCellNode(
                source.createSection(i, 1), tape, -1
            ));
            case '.' -> groupNodeStack.getLast().children.add(new WutlangPrintCellNode(
                source.createSection(i, 1), tape
            ));
            case ',' -> groupNodeStack.getLast().children.add(new WutlangReadCellNode(
                source.createSection(i, 1), tape
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
                var groupNode = new WutlangGroupNode(
                    loopBodySourceSection,
                    false,
                    new WutlangOptimizer(source, tape, loopBody.children).optimize()
                );
                DirectCallNode loopBodyNode = Truffle.getRuntime().createDirectCallNode(
                    Truffle.getRuntime().createCallTarget(
                        new WutlangRootNode(
                            language,
                            frameDescriptor,
                            tape,
                            groupNode,
                            false
                        )
                    )
                );
                groupNodeStack.getLast().children.add(
                    new WutlangLoopNode(loopSourceSection, tape, loopBodyNode)
                );
            }
            case '#' -> {
                // Comment, eat to end of line
                return false;
            }
            case ' ' -> {
                // ignored
            }
            default -> throw new IllegalStateException("Unknown character outside of comment: " + ensurePrintable(c));
        }
        return true;
    }

    private String ensurePrintable(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        boolean printable = !(Character.isISOControl(c) || Character.isWhitespace(c))
            && block != null
            && block != Character.UnicodeBlock.SPECIALS;
        return printable ? String.valueOf(c) : "\\u%04x".formatted((int) c);
    }

}
