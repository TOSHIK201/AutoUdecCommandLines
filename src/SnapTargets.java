import java.util.ArrayList;
import java.util.List;

class SnapTargets {
    static List<Double> xTargets(double y, double tol) {
        List<Double> xs = new ArrayList<>();
        xs.add(0.0);
        xs.add(ParameterList.modelLength);
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
}
