import java.util.List;
import java.util.Locale;

// 阶段4 开挖分组与几何的对应关系：分组名 -> 矩形范围 / 可读标签
class Stage4Geometry {
    static double[] range(String group) {
        String name = clean(group);
        if (name.isEmpty()) return null;
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            List<Double> xs = Util.calculateExcavationXList(e);
            if (xs.size() < 2) continue;
            if (name.equals(openGroup(e))) return rect(e, xs.get(0), xs.get(1));
            for (int i = 1; i < xs.size() - 1; i++) {
                if (name.equals("state_" + i + "_" + e.name)) {
                    return rect(e, xs.get(i), xs.get(i + 1));
                }
            }
        }
        return null;
    }

    static String label(String group) {
        String name = clean(group);
        if (name.isEmpty()) return "";
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (name.equals(openGroup(e))) return prefix(e.name) + "开切眼";
            List<Double> xs = Util.calculateExcavationXList(e);
            for (int i = 1; i < xs.size() - 1; i++) {
                if (name.equals("state_" + i + "_" + e.name)) {
                    return prefix(e.name) + "第 " + i + " 步";
                }
            }
        }
        return name;
    }

    static String describe(String group) {
        String name = clean(group);
        if (name.isEmpty()) return "未选择开挖区域";
        double[] r = range(name);
        String label = label(name);
        if (r == null) return label + "（未匹配到开挖区域）";
        return String.format(Locale.ROOT, "%s   x %.2f–%.2f   y %.2f–%.2f",
                label, r[0], r[1], r[2], r[3]);
    }

    static String openGroup(ExcavationLayer e) {
        return "openOffCut_" + e.name;
    }

    private static double[] rect(ExcavationLayer e, double xa, double xb) {
        return new double[]{
                Math.min(xa, xb), Math.max(xa, xb),
                Math.min(e.y1, e.y2), Math.max(e.y1, e.y2)
        };
    }

    private static String prefix(String layerName) {
        return layerName.matches("\\d+") ? layerName + "# " : layerName + " ";
    }

    private static String clean(String group) {
        return group == null ? "" : group.trim();
    }
}
