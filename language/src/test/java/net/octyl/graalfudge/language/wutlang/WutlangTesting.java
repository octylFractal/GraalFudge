package net.octyl.graalfudge.language.wutlang;

import net.octyl.graalfudge.language.TestLanguageRunner;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;

public class WutlangTesting {
    public static Context createContext() {
        return TestLanguageRunner.createContext(WutlangLanguage.ID)
            .option(WutlangLanguage.ID + ".testMode", "true")
            .build();
    }

    public static WutlangDebugInfo eval(String name, String source) throws IOException {
        try (var ctx = createContext()) {
            var value = ctx.eval(
                Source.newBuilder(WutlangLanguage.ID, source, name).build()
            );
            return value.as(WutlangDebugInfo.class);
        }
    }
}
