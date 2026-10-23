import java.util.ArrayList;
import java.util.List;

class SnapTargets {
    static List<Double> xTargets(double y, double tol) {
        List<Double> xs = new ArrayList<>();
        xs.add(0.0);
        xs.add(ParameterList.modelLength);
        for (BoltGroup g : ParameterList.boltGroups) {
            addX(xs, g.x1, g.y1, y, tol);
            addX(xs, g.x2, g.y2, y, tol);
            for (Bolt b : g.bolts) {
                addX(xs, b.sx, b.sy, y, tol);
                addX(xs, b.ex(), b.ey(), y, tol);
            }
        }
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            double bottom = Util.excavationBottom(e, 0);
            double top = bottom + Util.excavationHeight(e, 0);
            if (y < bottom - tol || y > top + tol) continue;
            xs.add(Math.min(e.x1, e.x2));
            xs.add(Math.max(e.x1, e.x2));
            xs.addAll(Util.calculateExcavationXList(e));
        }
        return xs;
    }

    static List<Double> yTargets(double x, double tol) {
        List<Double> ys = new ArrayList<>(ParameterList.heights());
        for (BoltGroup g : ParameterList.boltGroups) {
            addY(ys, g.x1, g.y1, x, tol);
            addY(ys, g.x2, g.y2, x, tol);
            for (Bolt b : g.bolts) {
                addY(ys, b.sx, b.sy, x, tol);
                addY(ys, b.ex(), b.ey(), x, tol);
            }
        }
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            double bottom = Util.excavationBottom(e, 0);
            double top = bottom + Util.excavationHeight(e, 0);
            double xMin = Math.min(e.x1, e.x2);
            double xMax = Math.max(e.x1, e.x2);
            if (x < xMin - tol || x > xMax + tol) continue;
            ys.add(bottom);
            ys.add(top);
        }
        return ys;
    }

    static Double nearest(double value, List<Double> targets, double tol) {
        Double best = null;
        double bestDist = tol;
        for (double target : targets) {
            double dist = Math.abs(value - target);
            if (dist <= bestDist) {
                bestDist = dist;
                best = target;
            }
        }
        return best;
    }

    private static void addX(List<Double> xs, double px, double py, double y, double tol) {
        if (Math.abs(py - y) <= tol) xs.add(px);
    }

    private static void addY(List<Double> ys, double px, double py, double x, double tol) {
        if (Math.abs(px - x) <= tol) ys.add(py);
    }
}
