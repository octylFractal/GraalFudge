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

package net.octyl.graalfudge.language.bf.node;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;
import net.octyl.graalfudge.language.bf.GraalFudgeContext;
import net.octyl.graalfudge.language.bf.GraalFudgeLanguage;

@GenerateWrapper
@ReportPolymorphism
public abstract class GraalFudgeStatementNode extends Node implements InstrumentableNode {
    private final SourceSection sourceSection;
    @CompilerDirectives.CompilationFinal
    private TruffleLanguage.ContextReference<GraalFudgeContext> context;

    protected GraalFudgeStatementNode(SourceSection sourceSection) {
        this.sourceSection = sourceSection;
    }

    protected GraalFudgeContext useContext() {
        if (context == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            context = lookupContextReference(GraalFudgeLanguage.class);
        }
        return context.get();
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.ExpressionTag.class || tag == StandardTags.StatementTag.class) {
            return true;
        }
        return InstrumentableNode.super.hasTag(tag);
    }

    @Override
    public boolean isInstrumentable() {
        return true;
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probeNode) {
        return new GraalFudgeStatementNodeWrapper(this.sourceSection, this, probeNode);
    }

    @Override
    public SourceSection getSourceSection() {
        return this.sourceSection;
    }

    public abstract void execute(VirtualFrame frame);
}
