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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import java.util.Arrays;

public class InfiniteTape {
    private final FrameDescriptor frameDescriptor;
    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private FrameSlot[] slots;
    private final FrameSlot dataPointer;
    @CompilerDirectives.CompilationFinal
    private Assumption slotsUnmodified = Truffle.getRuntime().createAssumption("slotsUnmodified");

    public InfiniteTape(FrameDescriptor frameDescriptor) {
        this.frameDescriptor = frameDescriptor;
        this.slots = new FrameSlot[1_024];
        for (int i = 0; i < this.slots.length; i++) {
            this.slots[i] = frameDescriptor.addFrameSlot(i, FrameSlotKind.Byte);
        }
        this.dataPointer = frameDescriptor.addFrameSlot("dataPointer", FrameSlotKind.Int);
    }


    private void reallocateBuffer(int minSize) {
        int oldSize = slots.length;
        int newSize = Math.max(minSize, slots.length * 3 / 2);
        this.slots = Arrays.copyOf(this.slots, newSize);
        for (int i = oldSize; i < newSize; i++) {
            this.slots[i] = frameDescriptor.addFrameSlot(i, FrameSlotKind.Byte);
        }
        if (oldSize != newSize) {
            slotsUnmodified.invalidate("Slots were reallocated");
            // Re-create assumption for newly compiled code
            this.slotsUnmodified = Truffle.getRuntime().createAssumption("slotsUnmodified");
        }
    }

    private void checkSlotsUnmodified() {
        if (!slotsUnmodified.isValid()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
        }
    }

    private int dataPointer(VirtualFrame frame) {
        try {
            return frame.getInt(dataPointer);
        } catch (FrameSlotTypeException e) {
            throw new AssertionError("Expected int slot", e);
        }
    }

    private byte getByteUnchecked(VirtualFrame frame, FrameSlot slot) {
        try {
            return frame.getByte(slot);
        } catch (FrameSlotTypeException e) {
            throw new AssertionError("Expected byte slot", e);
        }
    }

    public void incrementCell(VirtualFrame frame) {
        checkSlotsUnmodified();
        var slot = slots[dataPointer(frame)];
        frame.setByte(slot, (byte) (getByteUnchecked(frame, slot) + 1));
    }

    public void decrementCell(VirtualFrame frame) {
        checkSlotsUnmodified();
        var slot = slots[dataPointer(frame)];
        frame.setByte(slot, (byte) (getByteUnchecked(frame, slot) - 1));
    }

    public void nextCell(VirtualFrame frame) {
        int value = dataPointer(frame) + 1;
        frame.setInt(dataPointer, value);
        checkSlotsUnmodified();
        if (value >= slots.length) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            reallocateBuffer(value);
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
        checkSlotsUnmodified();
        frame.setByte(slots[dataPointer(frame)], value);
    }

    public byte readCell(VirtualFrame frame) {
        checkSlotsUnmodified();
        return getByteUnchecked(frame, slots[dataPointer(frame)]);
    }
}
