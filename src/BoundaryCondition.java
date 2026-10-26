import java.util.Locale;

public class BoundaryCondition {
    public enum Edge {
        NONE, LEFT, RIGHT, BOTTOM, TOP
    }

    public enum Type {
        VELOCITY_X("速度边界 X"),
        VELOCITY_Y("速度边界 Y"),
        DISPLACE_X("位移边界 X"),
        DISPLACE_Y("位移边界 Y"),
        STRESS("应力边界");

        public final String label;

        Type(String label) {
            this.label = label;
        }

        public boolean vertical() {
            return this == VELOCITY_X || this == DISPLACE_X;
        }

        public boolean displacement() {
            return this == DISPLACE_X || this == DISPLACE_Y;
        }

        public boolean stress() {
            return this == STRESS;
        }
    }

    public static Type[] uiTypes() {
        return new Type[]{Type.VELOCITY_X, Type.VELOCITY_Y, Type.STRESS};
    }

    public Type type;
    public double value;
    public double sxx, sxy, syy;
    public double sxxx, sxyx, syyx;
    public double sxxy, sxyy, syyy;
    public boolean useXGrad, useYGrad;
    public double sx, sy, ex, ey;
    public double width;
    public String name;
    public Edge edge = Edge.NONE;

    public BoundaryCondition() {
        this(Type.VELOCITY_X, 0, 0, 0, 0, ParameterList.boundaryWidth);
    }

    public BoundaryCondition(Type type, double sx, double sy, double ex, double ey, double width) {
        this.type = type;
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
        this.width = width;
        this.value = 0.0;
        this.name = type.label;
    }

    public boolean verticalLine() {
        return Math.abs(ey - sy) > Math.abs(ex - sx);
    }

    public boolean diagonal() {
        double dx = Math.abs(ex - sx);
        double dy = Math.abs(ey - sy);
        double major = Math.max(dx, dy);
        double minor = Math.min(dx, dy);
        return minor > 1e-6 && minor > major * 0.05;
    }

    // 线两侧按宽度展开成 UDEC range 矩形：竖向线展开 x，横向线展开 y。
    public double[] range() {
        if (verticalLine()) {
            double x = (sx + ex) / 2.0;
            return new double[]{x - width / 2.0, x + width / 2.0,
                    Math.min(sy, ey), Math.max(sy, ey)};
        }
        double y = (sy + ey) / 2.0;
        double y1 = y - width / 2.0;
        double y2 = y + width / 2.0;
        if (edge == Edge.TOP) {
            y2 = Math.min(y2, ParameterList.heightList[0]);
            y1 = y2 - width;
        }
        return new double[]{Math.min(sx, ex), Math.max(sx, ex),
                y1, y2};
    }

    public boolean hit(double wx, double wy, double tol) {
        double[] r = range();
        return wx >= r[0] - tol && wx <= r[1] + tol
                && wy >= r[2] - tol && wy <= r[3] + tol;
    }

    public String command() {
        double[] r = range();
        String range = String.format(Locale.ROOT, "range %.2f %.2f %.2f %.2f",
                r[0], r[1], r[2], r[3]);
        if (type.stress()) {
            StringBuilder command = new StringBuilder(String.format(Locale.ROOT,
                    "bound stress %.4e %.4e %.4e", sxx, sxy, syy));
            if (useXGrad) {
                command.append(String.format(Locale.ROOT, " xgrad %.4e %.4e %.4e",
                        sxxx, sxyx, syyx));
            }
            if (useYGrad) {
                command.append(String.format(Locale.ROOT, " ygrad %.4e %.4e %.4e",
                        sxxy, sxyy, syyy));
            }
            return command.append(' ').append(range).toString();
        }
        String key = type.vertical() ? "xvel" : "yvel";
        double v = type.displacement() ? 0.0 : value;
        return "bound " + key + " " + fmt(v) + " " + range;
    }

    public String label() {
        double[] r = range();
        if (type.stress()) {
            String gradient = (useXGrad ? " xgrad" : "") + (useYGrad ? " ygrad" : "");
            return String.format(Locale.ROOT,
                    "%s%s σxx=%.3e σxy=%.3e σyy=%.3e  宽 %.2f  x[%.2f,%.2f] y[%.2f,%.2f]",
                    type.label, gradient, sxx, sxy, syy, width, r[0], r[1], r[2], r[3]);
        }
        double shown = type.displacement() ? 0.0 : value;
        return String.format(Locale.ROOT, "%s = %s  宽 %.2f  x[%.2f,%.2f] y[%.2f,%.2f]",
                type.label, fmt(shown), width, r[0], r[1], r[2], r[3]);
    }

    static String fmt(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        String s = String.format(Locale.ROOT, "%.4f", v);
        while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        return s;
    }
}
