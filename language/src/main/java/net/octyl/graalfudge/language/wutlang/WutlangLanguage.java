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

package net.octyl.graalfudge.language.wutlang;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Option;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import net.octyl.graalfudge.language.wutlang.parser.WutlangParser;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionStability;

import java.io.IOException;
import java.io.UncheckedIOException;

@TruffleLanguage.Registration(
    id = WutlangLanguage.ID,
    name = WutlangLanguage.NAME,
    defaultMimeType = WutlangLanguage.MIME_TYPE,
    characterMimeTypes = WutlangLanguage.MIME_TYPE,
    contextPolicy = TruffleLanguage.ContextPolicy.EXCLUSIVE,
    fileTypeDetectors = WutlangFileDetector.class
)
@ProvidedTags({
    StandardTags.RootTag.class,
    StandardTags.RootBodyTag.class,
    StandardTags.StatementTag.class,
    StandardTags.ExpressionTag.class
})
public class WutlangLanguage extends TruffleLanguage<WutlangContext> {
    public static final String ID = "wutlang";
    public static final String NAME = "Wutlang";
    public static final String MIME_TYPE = "application/x-wutlang";
    @Option(
        name = "testMode",
        help = "Enables test mode.",
        category = OptionCategory.INTERNAL,
        stability = OptionStability.STABLE
    )
    static final OptionKey<Boolean> TEST_MODE = new OptionKey<>(false);

    @Override
    protected WutlangContext createContext(Env env) {
        return new WutlangContext(this, env);
    }

    @Override
    protected void disposeContext(WutlangContext context) {
        try (context) {
            context.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new WutlangLanguageOptionDescriptors();
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        var ctx = getCurrentContext(getClass());
        var rootNode = new WutlangParser(
            this,
            request.getSource(),
            ctx.env().getOptions().get(TEST_MODE)
        ).parse();
        return Truffle.getRuntime().createCallTarget(rootNode);
    }
}

