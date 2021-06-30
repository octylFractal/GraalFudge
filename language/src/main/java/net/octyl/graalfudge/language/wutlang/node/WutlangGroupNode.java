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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.source.SourceSection;

/**
 * A "group" of statements -- Brainf*** doesn't have blocks.
 */
@GenerateWrapper
public class WutlangGroupNode extends WutlangStatementNode {
    private final boolean isRootTag;
    @Children
    private final WutlangStatementNode[] statementNodes;

    public WutlangGroupNode(SourceSection sourceSection, boolean isRootTag,
                            WutlangStatementNode[] statementNodes) {
        super(sourceSection);
        this.isRootTag = isRootTag;
        this.statementNodes = statementNodes;
    }

    public WutlangGroupNode(WutlangGroupNode node) {
        this(node.getSourceSection(), node.isRootTag, node.statementNodes);
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (isRootTag && (tag == StandardTags.RootTag.class || tag == StandardTags.RootBodyTag.class)) {
            return true;
        }
        return super.hasTag(tag);
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probeNode) {
        return new WutlangGroupNodeWrapper(this, this, probeNode);
    }

    @ExplodeLoop
    @Override
    public void execute(VirtualFrame frame) {
        for (var statementNode : statementNodes) {
            statementNode.execute(frame.materialize());
        }
    }
}
