/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Net.server;

import Client.MapleClient;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdelProjectile {
    private int projectileType, ownerId, targetId, skillId, duration, startX, startY, delay, idk2, createDelay;
    private Point point;
    private List<Integer> points = new ArrayList<>();

    public AdelProjectile(int projectileType, int ownerId, int targetId, int skillId, int duration, int startX, int startY, Point point, List<Integer> points) {
        this.projectileType = projectileType;
        this.ownerId = ownerId;
        this.targetId = targetId;
        this.skillId = skillId;
        this.duration = duration;
        this.startX = startX;
        this.startY = startY;
        this.point = point;
        this.points = points;
    }

    public void sendSpawnData(MapleClient client) {
    }

    public void sendDestroyData(MapleClient client) {
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getProjectileType() {
        return projectileType;
    }

    public void setProjectileType(int projectileType) {
        this.projectileType = projectileType;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getIdk2() {
        return idk2;
    }

    public void setIdk2(int idk2) {
        this.idk2 = idk2;
    }

    public int getCreateDelay() {
        return createDelay;
    }

    public void setCreateDelay(int createDelay) {
        this.createDelay = createDelay;
    }
}
