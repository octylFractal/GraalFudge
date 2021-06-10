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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.GraalFudgeContext;
import net.octyl.graalfudge.language.GraalFudgeLanguage;

@NodeInfo(shortName = "loop")
public class GraalFudgeLoopNode extends GraalFudgeStatementNode {
    @Child
    private LoopNode loopNode;

    public GraalFudgeLoopNode(SourceSection sourceSection, GraalFudgeGroupNode bodyNode) {
        super(sourceSection);
        this.loopNode = Truffle.getRuntime().createLoopNode(new GraalFudgeLoopNodeInternal(bodyNode));
    }

    @Override
    public void execute(VirtualFrame frame) {
        loopNode.execute(frame);
    }

    private static final class GraalFudgeLoopNodeInternal extends Node implements RepeatingNode {
        private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();
        @Child
        private GraalFudgeGroupNode bodyNode;
        @CompilerDirectives.CompilationFinal
        private TruffleLanguage.ContextReference<GraalFudgeContext> context;

        private GraalFudgeLoopNodeInternal(GraalFudgeGroupNode bodyNode) {
            this.bodyNode = bodyNode;
        }

        protected GraalFudgeContext useContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = lookupContextReference(GraalFudgeLanguage.class);
            }
            return context.get();
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            if (loopProfile.profile(useContext().tape().readCell() == 0)) {
                return false;
            }
            bodyNode.execute(frame);
            return true;
        }
    }
}
