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

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;

import java.io.InputStream;
import java.io.OutputStream;

public class TestLanguageRunner {
    public static Context.Builder createContext(String languageId) {
        return createContext(languageId, InputStream.nullInputStream(), OutputStream.nullOutputStream());
    }

    public static Context.Builder createContext(String languageId, InputStream in, OutputStream out) {
        if (!Engine.create().getLanguages().containsKey(languageId)) {
            throw new IllegalStateException("Missing " + languageId + " in GraalVM runtime");
        }
        return Context.newBuilder(languageId)
            .in(in)
            .out(out)
            .allowHostAccess(HostAccess.ALL);
    }
}
