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

package net.octyl.graalfudge.language;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import net.octyl.graalfudge.language.parser.GraalFudgeParser;

import java.io.IOException;
import java.io.UncheckedIOException;

@TruffleLanguage.Registration(
    id = GraalFudgeLanguage.ID,
    name = GraalFudgeLanguage.NAME,
    defaultMimeType = GraalFudgeLanguage.MIME_TYPE,
    characterMimeTypes = GraalFudgeLanguage.MIME_TYPE,
    contextPolicy = TruffleLanguage.ContextPolicy.EXCLUSIVE,
    fileTypeDetectors = GraalFudgeFileDetector.class
)
@ProvidedTags({
    StandardTags.RootTag.class,
    StandardTags.RootBodyTag.class,
    StandardTags.StatementTag.class,
    StandardTags.ExpressionTag.class
})
public class GraalFudgeLanguage extends TruffleLanguage<GraalFudgeContext> {
    public static final String ID = "graalfudge";
    public static final String NAME = "GraalFudge";
    public static final String MIME_TYPE = "application/x-brainfuck";

    @Override
    protected GraalFudgeContext createContext(Env env) {
        return new GraalFudgeContext(this, env);
    }

    @Override
    protected void disposeContext(GraalFudgeContext context) {
        try {
            context.output().flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        var rootNode = new GraalFudgeParser(this, request.getSource()).parse();
        return Truffle.getRuntime().createCallTarget(rootNode);
    }
}
