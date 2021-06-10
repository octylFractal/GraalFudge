package net.octyl.graalfudge.language.util;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.utilities.TriState;
import net.octyl.graalfudge.language.GraalFudgeLanguage;

// Copied from the simplelanguage Graal repository, which is licensed under the Universal Permissive License, 1.0.
@ExportLibrary(InteropLibrary.class)
public final class GraalFudgeNull implements TruffleObject {

    public static final GraalFudgeNull SINGLETON = new GraalFudgeNull();
    private static final int IDENTITY_HASH = System.identityHashCode(SINGLETON);

    private GraalFudgeNull() {
    }

    @Override
    public String toString() {
        return "NULL";
    }

    @ExportMessage
    boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    Class<? extends TruffleLanguage<?>> getLanguage() {
        return GraalFudgeLanguage.class;
    }

    @ExportMessage
    boolean isNull() {
        return true;
    }

    @ExportMessage
    static TriState isIdenticalOrUndefined(GraalFudgeNull receiver, Object other) {
        return TriState.valueOf(GraalFudgeNull.SINGLETON == other);
    }

    @ExportMessage
    static int identityHashCode(GraalFudgeNull receiver) {
        return IDENTITY_HASH;
    }

    @ExportMessage
    Object toDisplayString(boolean allowSideEffects) {
        return "NULL";
    }
}
