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

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import java.util.Arrays;

public final class InfiniteTape {
    private final BranchProfile negativeTapeIndex = BranchProfile.create();
    private final ConditionProfile reallocateProfile = ConditionProfile.createCountingProfile();
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

    public byte[] buffer(VirtualFrame frame) {
        byte[] buffer;
        try {
            buffer = (byte[]) frame.getObject(bufferPointer);
        } catch (FrameSlotTypeException e) {
            throw new AssertionError("Expected object slot", e);
        }
        return buffer;
    }

    public int dataPointer(VirtualFrame frame) {
        try {
            return frame.getInt(dataPointer);
        } catch (FrameSlotTypeException e) {
            throw new AssertionError("Expected int slot", e);
        }
    }

    public void initialize(VirtualFrame frame) {
        if (frame.getArguments().length == 0) {
            initializeFrom(frame, new byte[1024], 0);
        } else {
            initializeFrom(frame, (byte[]) frame.getArguments()[0], (int) frame.getArguments()[1]);
        }
    }

    public void initializeFrom(VirtualFrame frame, byte[] buffer, int dataPointer) {
        frame.setObject(bufferPointer, buffer);
        setDataPointer(frame, dataPointer);
    }

    public void setDataPointer(VirtualFrame frame, int dataPointer) {
        frame.setInt(this.dataPointer, dataPointer);
    }

    public void changeCell(VirtualFrame frame, int amount) {
        buffer(frame)[dataPointer(frame)] += amount;
    }

    public void moveDataPointer(VirtualFrame frame, int amount) {
        int value = dataPointer(frame) + amount;
        frame.setInt(dataPointer, value);
        if (reallocateProfile.profile(value >= buffer(frame).length)) {
            reallocateBuffer(frame, value);
        } else if (value < 0) {
            negativeTapeIndex.enter();
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
