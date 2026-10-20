import java.util.ArrayList;
import java.util.List;

class SnapTargets {
    static List<Double> xTargets(double y, double tol) {
        List<Double> xs = new ArrayList<>();
        xs.add(0.0);
        xs.add(ParameterList.modelLength);
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            int idx = e.layerIndex;
            if (idx < 0 || idx + 1 >= ParameterList.heightList.length) continue;
            double bottom = Util.excavationBottom(e, idx);
            double top = bottom + Util.excavationHeight(e, idx);
            if (y < bottom - tol || y > top + tol) continue;
            xs.addAll(Util.calculateExcavationXList(e));
        }
        return xs;
    }

    static List<Double> yTargets(double x, double tol) {
        List<Double> ys = new ArrayList<>(ParameterList.heights());
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            int idx = e.layerIndex;
            if (idx < 0 || idx + 1 >= ParameterList.heightList.length) continue;
            double bottom = Util.excavationBottom(e, idx);
            double top = bottom + Util.excavationHeight(e, idx);
            List<Double> xs = Util.calculateExcavationXList(e);
            double xMin = xs.get(0);
            double xMax = xs.get(0);
            for (double cx : xs) {
                xMin = Math.min(xMin, cx);
                xMax = Math.max(xMax, cx);
            }
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
