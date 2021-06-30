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

package net.octyl.graalfudge.language.bf;

import com.oracle.truffle.api.TruffleLanguage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public record GraalFudgeContext(
    GraalFudgeLanguage language,
    InputStream input,
    OutputStream output
) {
    public GraalFudgeContext(GraalFudgeLanguage language, TruffleLanguage.Env env) {
        this(
            language,
            new BufferedInputStream(env.in()),
            new BufferedOutputStream(env.out())
        );
    }
}
