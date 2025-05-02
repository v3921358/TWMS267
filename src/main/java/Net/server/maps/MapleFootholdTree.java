package Net.server.maps;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MapleFootholdTree {

    private static final byte maxDepth = 8;
    private final List<MapleFoothold> footholds = new LinkedList<>();
    private final Point p1;
    private final Point p2;
    private final Point center;
    private MapleFootholdTree nw = null;
    private MapleFootholdTree ne = null;
    private MapleFootholdTree sw = null;
    private MapleFootholdTree se = null;
    private int depth = 0;
    private int maxDropX;
    private int minDropX;

    public MapleFootholdTree(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        center = new Point((p2.x - p1.x) / 2, (p2.y - p1.y) / 2);
    }

    public MapleFootholdTree(Point p1, Point p2, int depth) {
        this.p1 = p1;
        this.p2 = p2;
        this.depth = depth;
        center = new Point((p2.x - p1.x) / 2, (p2.y - p1.y) / 2);
    }

    public void insert(MapleFoothold f) {
        if (depth == 0) {
            if (f.getX1() > maxDropX) {
                maxDropX = f.getX1();
            }
            if (f.getX1() < minDropX) {
                minDropX = f.getX1();
            }
            if (f.getX2() > maxDropX) {
                maxDropX = f.getX2();
            }
            if (f.getX2() < minDropX) {
                minDropX = f.getX2();
            }
        }
        if (depth == maxDepth || (f.getX1() >= p1.x && f.getX2() <= p2.x && f.getY1() >= p1.y && f.getY2() <= p2.y)) {
            footholds.add(f);
        } else {
            if (nw == null) {
                nw = new MapleFootholdTree(p1, center, depth + 1);
                ne = new MapleFootholdTree(new Point(center.x, p1.y), new Point(p2.x, center.y), depth + 1);
                sw = new MapleFootholdTree(new Point(p1.x, center.y), new Point(center.x, p2.y), depth + 1);
                se = new MapleFootholdTree(center, p2, depth + 1);
            }
            if (f.getX2() <= center.x && f.getY2() <= center.y) {
                nw.insert(f);
            } else if (f.getX1() > center.x && f.getY2() <= center.y) {
                ne.insert(f);
            } else if (f.getX2() <= center.x && f.getY1() > center.y) {
                sw.insert(f);
            } else {
                se.insert(f);
            }
        }
    }

    public List<MapleFoothold> getAllRelevants() {
        return getAllRelevants(new LinkedList<>());
    }

    private List<MapleFoothold> getAllRelevants(List<MapleFoothold> list) {
        list.addAll(footholds);
        if (nw != null) {
            nw.getAllRelevants(list);
            ne.getAllRelevants(list);
            sw.getAllRelevants(list);
            se.getAllRelevants(list);
        }
        return list;
    }

    private List<MapleFoothold> getRelevants(Point p) {
        return getRelevants(p, new LinkedList<>());
    }

    private List<MapleFoothold> getRelevants(Point p, List<MapleFoothold> list) {
        list.addAll(footholds);
        if (nw != null) {
            if (p.x <= center.x && p.y <= center.y) {
                nw.getRelevants(p, list);
            } else if (p.x > center.x && p.y <= center.y) {
                ne.getRelevants(p, list);
            } else if (p.x <= center.x && p.y > center.y) {
                sw.getRelevants(p, list);
            } else {
                se.getRelevants(p, list);
            }
        }
        return list;
    }

    public MapleFoothold findBelow(Point p) {
        Point point = new Point(p.x, p.y - 1);
        List<MapleFoothold> relevants = getRelevants(point);
        List<MapleFoothold> xMatches = new LinkedList<>();
        for (MapleFoothold fh : relevants) {
            if (fh.getX1() <= point.x && fh.getX2() >= point.x) {
                xMatches.add(fh);
            }
        }
        Collections.sort(xMatches);
        for (MapleFoothold fh : xMatches) {
            if (fh.isWall()) {
                continue;
            }
            if (fh.getY1() != fh.getY2()) {
                int calcY;
                double s1 = Math.abs(fh.getY2() - fh.getY1());
                double s2 = Math.abs(fh.getX2() - fh.getX1());
                double s4 = Math.abs(point.x - fh.getX1());
                double alpha = Math.atan(s2 / s1);
                double beta = Math.atan(s1 / s2);
                double s5 = Math.cos(alpha) * (s4 / Math.cos(beta));
                if (fh.getY2() < fh.getY1()) {
                    calcY = fh.getY1() - (int) s5;
                } else {
                    calcY = fh.getY1() + (int) s5;
                }
                if (calcY >= point.y) {
                    return fh;
                }
            } else if (fh.getY1() >= point.y) {
                return fh;
            }
        }
        return null;
    }
}
