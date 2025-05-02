/*
 * NeroMS MapleStory server emulator written in Java
 * Copyright (C) 2017-2018  Jackson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author Jackson 
 */
if (player.getJob() == 2000 && !"o".equals(player.getQuestRecordEx(21002, "hire"))) {
        player.updateQuestRecordEx(21002, "hire", "o");
        player.showHireTutorMsg("好了戰神，再往那邊走一點就有城鎮了，我有事先走了！ 以後的路要靠你自己了！");
}
portal.abortWarp();