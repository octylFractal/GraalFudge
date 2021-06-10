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
