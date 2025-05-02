function spawnBoss(map){
    let MobX = -400 + Math.floor(Math.random() * 17) * 50;
    let ss = map.getId() - 925070000;
    let sId = parseInt(ss / 100);
    let mobIds = [9305600, MobX, 7];
    let MobHp = 5200000;
    switch (sId) {
            case 1:
                    MobHp = 5200000;
                    mobIds = [9305600, MobX, 7];
                    break;
            case 2:
                    MobHp = 5740800;
                    mobIds = [9305601, MobX, 7];
                    break;
            case 3:
                    MobHp = 6307200;
                    mobIds = [9305602, MobX, 7];
                    break;
            case 4:
                    MobHp = 6930000;
                    mobIds = [9305603, MobX, 7];
                    break;
            case 5:
                    MobHp = 7549200;
                    mobIds = [9305604, MobX, 7];
                    break;
            case 6:
                    MobHp = 12342000;
                    mobIds = [9305605, MobX, 7];
                    break;
            case 7:
                    MobHp = 13923000;
                    mobIds = [9305606, MobX, 7];
                    break;
            case 8:
                    MobHp = 15105000;
                    mobIds = [9305607, MobX, 7];
                    break;
            case 9:
                    MobHp = 16846000;
                    mobIds = [9305608, MobX, 7];
                    break;
            case 10:
                    MobHp = 100000000;
                    mobIds = [9305619, MobX, 7];
                    break;
            case 11:
                    MobHp = 40824000;
                    mobIds = [9305610, MobX, 7];
                    break;
            case 12:
                    MobHp = 45404550;
                    mobIds = [9305617, MobX, 7];
                    break;
            case 13:
                    MobHp = 48593250;
                    mobIds = [9305612, MobX, 7];
                    break;
            case 14:
                    MobHp = 55350000;
                    mobIds = [9305611, MobX, 7];
                    break;
            case 15:
                    MobHp = 61600500;
                    mobIds = [9305628, MobX, 7];
                    break;
            case 16:
                    MobHp = 68121000;
                    mobIds = [9305682, MobX, 7];
                    break;
            case 17:
                    MobHp = 78840000;
                    mobIds = [9305683, MobX, 7];
                    break;
            case 18:
                    MobHp = 90011250;
                    mobIds = [9305614, MobX, 7];
                    break;
            case 19:
                    MobHp = 97902000;
                    mobIds = [9305620, MobX, 7];
                    break;
            case 20:
                    MobHp = 1500000000;
                    mobIds = [9305609, MobX, 7];
                    break;
            case 21:
                    MobHp = 130536000;
                    mobIds = [9305623, 0, 7];
                    map.spawnMob(9305644, 150, 7);
                    map.spawnMob(9305644, 380, 7);
                    map.spawnMob(9305644, -325, 7);
                    map.spawnMob(9305644, -315, 7);
                    map.spawnMob(9305644, -333, 7);
                    break;
            case 22:
                    MobHp = 159138000;
                    mobIds = [9305625, -100, 7];
                    map.spawnMob(9305646, -50, 7);
                    map.spawnMob(9305646, 280, 7);
                    map.spawnMob(9305646, -125, 7);
                    map.spawnMob(9305646, 75, 7);
                    map.spawnMob(9305646, 222, 7);
                    break;
            case 23:
                    MobHp = 190350000;
                    mobIds = [9305624, -100, 7];
                    map.spawnMob(9305645, 50, 7);
                    map.spawnMob(9305645, 80, 7);
                    map.spawnMob(9305645, 325, 7);
                    map.spawnMob(9305645, 255, 7);
                    map.spawnMob(9305645, 42, 7);
                    break;
            case 24:
                    MobHp = 242424000;
                    mobIds = [9305684, 0, 7];
                    map.spawnMob(9305685, -50, 7);
                    map.spawnMob(9305685, 180, 7);
                    map.spawnMob(9305685, -125, 7);
                    map.spawnMob(9305685, 0, 7);
                    map.spawnMob(9305685, 42, 7);
                    break;
            case 25:
                    MobHp = 405504000;
                    mobIds = [9305658, -200, 7];
                    map.spawnMob(9305686, -350, 7);
                    map.spawnMob(9305686, 180, 7);
                    map.spawnMob(9305686, 125, 7);
                    map.spawnMob(9305686, 365, 7);
                    map.spawnMob(9305686, -222, 7);
                    break;
            case 26:
                    MobHp = 497040000;
                    mobIds = [9305687, -400, 7];
                    map.spawnMob(9305688, -350, 7);
                    map.spawnMob(9305688, 80, 7);
                    map.spawnMob(9305688, 25, 7);
                    map.spawnMob(9305688, 365, 7);
                    map.spawnMob(9305688, 111, 7);
                    break;
            case 27:
                    MobHp = 596496000;
                    mobIds = [9305616, 100, 7];
                    map.spawnMob(9305689, -350, 7);
                    map.spawnMob(9305689, 180, 7);
                    map.spawnMob(9305689, 325, 7);
                    map.spawnMob(9305689, 115, 7);
                    map.spawnMob(9305689, 222, 7);
                    break;
            case 28:
                    MobHp = 706176000;
                    mobIds = [9305690, 200, 7];
                    map.spawnMob(9305691, -50, 7);
                    map.spawnMob(9305691, 180, 7);
                    map.spawnMob(9305691, -125, 7);
                    map.spawnMob(9305691, -75, 7);
                    map.spawnMob(9305691, -333, 7);
                    break;
            case 29:
                    MobHp = 824256000;
                    mobIds = [9305692, 200, 7];
                    map.spawnMob(9305693, -50, 7);
                    map.spawnMob(9305693, 380, 7);
                    map.spawnMob(9305693, 25, 7);
                    map.spawnMob(9305693, -275, 7);
                    map.spawnMob(9305693, 333, 7);
                    break;
            case 30:
                    MobHp = 3000000000;
                    mobIds = [9305629, MobX, 7];
                    break;
            case 31://打死後隔2秒重新召喚第二只
                    MobHp = 2108240000;
                    mobIds = [9305630, MobX, 7];
                    break;
            case 32://打死後隔2秒重新召喚第二只
                    MobHp = 2526520000;
                    mobIds = [9305631, MobX, 7];
                    break;
            case 33://打死後隔2秒重新召喚第二只
                    MobHp = 2976000000;
                    mobIds = [9305659, MobX, 7];
                    break;
            case 34://打死後隔2秒重新召喚第二只
                    MobHp = 3464920000;
                    mobIds = [9305633, MobX, 7];
                    break;
            case 35://打死後隔2秒重新召喚第二只
                    MobHp = 3986640000;
                    mobIds = [9305621, MobX, 7];
                    break;
            case 36://打死後隔2秒重新召喚第二只
                    MobHp = 4551000000;
                    mobIds = [9305632, MobX, 7];
                    break;
            case 37://打死後隔2秒重新召喚第二只
                    MobHp = 5149760000;
                    mobIds = [9305694, MobX, 7];
                    break;
            case 38://打死後每隔2秒重新召喚第二只和第三隻
                    MobHp = 6474960000;
                    mobIds = [9305634, MobX, 7];
                    break;
            case 39://打死後每隔2秒重新召喚第二只和第三隻
                    MobHp = 7971840000;
                    mobIds = [9305656, MobX, 7];
                    break;
            case 40:
                    MobHp = 8000000000;
                    mobIds = [9305639, MobX, 7];
                    break;
            case 41:
                    MobHp = 42000000000;
                    mobIds = [9305660, MobX, 7];
                    break;
            case 42:
                    MobHp = 63000000000;
                    mobIds = [9305661, MobX, 7];
                    break;
            case 43:
                    MobHp = 84000000000;
                    mobIds = [9305627, MobX, 7];
                    break;
            case 44:
                    MobHp = 105000000000;
                    mobIds = [9305622, MobX, 7];
                    break;
            case 45:
                    MobHp = 105000000000;
                    mobIds = [9305662, MobX, 7];
                    break;
            case 46:
                    MobHp = 210000000000;
                    mobIds = [9305635, MobX, 7];
                    break;
            case 47:
                    MobHp = 315000000000;
                    mobIds = [9305636, MobX, 7];
                    break;
            case 48:
                    MobHp = 420000000000;
                    mobIds = [9305637, MobX, 7];
                    break;
            case 49:
                    MobHp = 525000000000;
                    mobIds = [9305638, MobX, 7];
                    break;
            case 50:
                    MobHp = 525000000000;
                    mobIds = [9305695, MobX, 7];
                    break;
            case 51:
                    MobHp = 630000000000;
                    mobIds = [9305696, MobX, 7];
                    break;
            case 52:
                    MobHp = 735000000000;
                    mobIds = [9305663, MobX, 7];
                    break;
            case 53:
                    MobHp = 840000000000;
                    mobIds = [9305664, MobX, 7];
                    break;
            case 54:
                    MobHp = 945000000000;
                    mobIds = [9305665, MobX, 7];
                    break;
            case 55:
                    MobHp = 1050000000000;
                    mobIds = [9305666, MobX, 7];
                    break;
            case 56:
                    MobHp = 1155000000000;
                    mobIds = [9305667, MobX, 7];
                    break;
            case 57:
                    MobHp = 1260000000000;
                    mobIds = [9305668, MobX, 7];
                    break;
            case 58:
                    MobHp = 1365000000000;
                    mobIds = [9305669, MobX, 7];
                    break;
            case 59:
                    MobHp = 1470000000000;
                    mobIds = [9305670, MobX, 7];
                    break;
            case 60:
                    MobHp = 1575000000000;
                    mobIds = [9305671, MobX, 7];
                    break;
            case 61:
                    MobHp = 1680000000000;
                    mobIds = [9305697, 305, 7];
                    break;
            case 62:
                    MobHp = 1785000000000;
                    mobIds = [9305698, MobX, 7];
                    break;
            case 63:
                    MobHp = 1890000000000;
                    mobIds = [9305699, MobX, 7];
                    break;
            case 64:
                    MobHp = 1911000000000;
                    mobIds = [9305700, MobX, 7];
                    break;
            case 65:
                    MobHp = 1932000000000;
                    mobIds = [9305701, MobX, 7];
                    break;
            case 66:
                    MobHp = 1953000000000;
                    mobIds = [9305657, MobX, 7];
                    break;
            case 67:
                    MobHp = 1974000000000;
                    mobIds = [9305702, MobX, 7];
                    break;
            case 68:
                    MobHp = 1995000000000;
                    mobIds = [9305703, 200, 7];
                    break;
            case 69:
                    MobHp = 2016000000000;
                    mobIds = [9305704, MobX, 7];
                    break;
            case 70:
                    MobHp = 2100000000000;
                    mobIds = [9305705, MobX, 7];
                    break;
            case 71:
                    MobHp = 2310000000000;
                    mobIds = [9305706, MobX, 7];
                    break;
            case 72:
                    MobHp = 2625000000000;
                    mobIds = [9305707, MobX, 7];
                    break;
            case 73:
                    MobHp = 2940000000000;
                    mobIds = [9305708, MobX, 7];
                    break;
            case 74:
                    MobHp = 3255000000000;
                    mobIds = [9305672, MobX, 7];
                    break;
            case 75:
                    MobHp = 3570000000000;
                    mobIds = [9305673, MobX, 7];
                    break;
            case 76:
                    MobHp = 3915000000000;
                    mobIds = [9305674, MobX, 7];
                    break;
            case 77:
                    MobHp = 4210000000000;
                    mobIds = [9305675, MobX, 7];
                    break;
            case 78:
                    MobHp = 4535000000000;
                    mobIds = [9305676, MobX, 7];
                    break;
            case 79:
                    MobHp = 4857000000000;
                    mobIds = [9305677, MobX, 7];
                    break;
            case 80:
                    MobHp = 5257300000000;
                    mobIds = [9305640, MobX, 7];
                    break;
            case 81:
                    MobHp = 5790900000000;
                    mobIds = [9305709, MobX, 7];
                    break;
            case 82:
                    MobHp = 6099800000000;
                    mobIds = [9305710, MobX, 7];
                    break;
            case 83:
                    MobHp = 6400000000000;
                    mobIds = [9305711, MobX, 7];
                    break;
            case 84:
                    MobHp = 6800000000000;
                    mobIds = [9305712, MobX, 7];
                    break;
            case 85:
                    MobHp = 7600000000000;
                    mobIds = [9305713, MobX, 7];
                    break;
            case 86:
                    MobHp = 8000000000000;
                    mobIds = [9305714, MobX, 7];
                    break;
            case 87:
                    MobHp = 8400000000000;
                    mobIds = [9305715, MobX, 7];
                    break;
            case 88:
                    MobHp = 8800000000000;
                    mobIds = [9305716, MobX, 7];
                    break;
            case 89:
                    MobHp = 9500000000000;
                    mobIds = [9305717, MobX, 7];
                    break;
            case 90:
                    MobHp = 10300000000000;
                    mobIds = [9305718, MobX, 7];
                    break;
            case 91:
                    MobHp = 11300000000000;
                    mobIds = [9305719, MobX, 7];
                    break;
            case 92:
                    MobHp = 12600000000000;
                    mobIds = [9305720, MobX, 7];
                    break;
            case 93:
                    MobHp = 13800000000000;
                    mobIds = [9305721, MobX, 7];
                    break;
            case 94:
                    MobHp = 15100000000000;
                    mobIds = [9305722, MobX, 7];
                    break;
            case 95:
                    MobHp = 16300000000000;
                    mobIds = [9305723, MobX, 7];
                    break;
            case 96:
                    MobHp = 17600000000000;
                    mobIds = [9305724, MobX, 7];
                    break;
            case 97:
                    MobHp = 18800000000000;
                    mobIds = [9305725, MobX, 7];
                    break;
            case 98:
                    MobHp = 20100000000000;
                    mobIds = [9305726, MobX, 7];
                    break;
            case 99:
                    MobHp = 21300000000000;
                    mobIds = [9305727, MobX, 7];
                    break;
            case 100:
                    MobHp = 25000000000000;
                    mobIds = [9305728, MobX, 7];
                    break;
    }
    let mob = map.makeMob(mobIds[0]);
    mob.setAppearType(-2);
    mob.changeBaseHp(MobHp);
    map.spawnMob(mob, mobIds[1], mobIds[2]);
}

export { spawnBoss }