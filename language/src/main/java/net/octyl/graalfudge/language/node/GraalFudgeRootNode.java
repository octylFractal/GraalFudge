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

package net.octyl.graalfudge.language.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import net.octyl.graalfudge.language.GraalFudgeLanguage;
import net.octyl.graalfudge.language.util.GraalFudgeNull;

@NodeInfo(language = GraalFudgeLanguage.NAME, description = "The root of a GraalFudge tree")
public class GraalFudgeRootNode extends RootNode {
    @Child
    private GraalFudgeGroupNode groupNode;

    public GraalFudgeRootNode(GraalFudgeLanguage language, GraalFudgeGroupNode groupNode) {
        super(language);
        this.groupNode = groupNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        groupNode.execute();
        return GraalFudgeNull.SINGLETON;
    }
}
