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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import java.util.Arrays;

public class InfiniteTape {
    private final FrameSlot bufferPointer;
    private final FrameSlot dataPointer;

    public InfiniteTape(FrameDescriptor frameDescriptor) {
        this.bufferPointer = frameDescriptor.addFrameSlot("bufferPointer", FrameSlotKind.Object);
        this.dataPointer = frameDescriptor.addFrameSlot("dataPointer", FrameSlotKind.Int);
    }

    private void reallocateBuffer(VirtualFrame frame, int minSize) {
        byte[] buffer = buffer(frame);
        int newSize = Math.max(minSize, buffer.length * 3 / 2);
        buffer = Arrays.copyOf(buffer, newSize);
        frame.setObject(bufferPointer, buffer);
    }

    private byte[] buffer(VirtualFrame frame) {
        byte[] buffer;
        try {
            buffer = (byte[]) frame.getObject(bufferPointer);
        } catch (FrameSlotTypeException e) {
            throw new AssertionError("Expected object slot", e);
        }
        if (buffer == null) {
            buffer = new byte[1_024];
            frame.setObject(bufferPointer, buffer);
        }
        return buffer;
    }

    private int dataPointer(VirtualFrame frame) {
        try {
            return frame.getInt(dataPointer);
        } catch (FrameSlotTypeException e) {
            throw new AssertionError("Expected int slot", e);
        }
    }

    public void initialize(VirtualFrame frame) {
        frame.setObject(bufferPointer, new byte[1024]);
        frame.setInt(dataPointer, 0);
    }

    public void incrementCell(VirtualFrame frame) {
        buffer(frame)[dataPointer(frame)]++;
    }

    public void decrementCell(VirtualFrame frame) {
        buffer(frame)[dataPointer(frame)]--;
    }

    public void nextCell(VirtualFrame frame) {
        int value = dataPointer(frame) + 1;
        frame.setInt(dataPointer, value);
        if (value >= buffer(frame).length) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            reallocateBuffer(frame, value);
        }
    }

    public void prevCell(VirtualFrame frame) {
        int value = dataPointer(frame) - 1;
        frame.setInt(dataPointer, value);
        if (value < 0) {
            throw new IllegalStateException("Data pointer moved below 0");
        }
    }

    public void writeCell(VirtualFrame frame, byte value) {
        buffer(frame)[dataPointer(frame)] = value;
    }

    public byte readCell(VirtualFrame frame) {
        return buffer(frame)[dataPointer(frame)];
    }
}
