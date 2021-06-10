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

import java.util.Arrays;

public class InfiniteTape {
    private byte[] buffer = new byte[1_024];
    private int dataPointer = 0;

    private void reallocateBuffer(int minSize) {
        int newSize = Math.max(minSize, buffer.length * 3 / 2);
        this.buffer = Arrays.copyOf(this.buffer, newSize);
    }

    public void incrementCell() {
        buffer[dataPointer]++;
    }

    public void decrementCell() {
        buffer[dataPointer]--;
    }

    public void nextCell() {
        dataPointer++;
        if (dataPointer >= buffer.length) {
            reallocateBuffer(dataPointer);
        }
    }

    public void prevCell() {
        dataPointer--;
        if (dataPointer < 0) {
            throw new IllegalStateException("Data pointer moved below 0");
        }
    }

    public void writeCell(byte value) {
        buffer[dataPointer] = value;
    }

    public byte readCell() {
        return buffer[dataPointer];
    }
}
