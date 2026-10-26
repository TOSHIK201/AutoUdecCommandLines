public class Bolt {
    public double sx, sy;
    public double length;
    public double angleDeg;
    public int nodes;
    public int cableMat;
    public int groutMat;
    public double area;
    public double pretension;
    public boolean custom;

    public double ex() {
        return sx + length * Math.cos(Math.toRadians(angleDeg));
    }

    public double ey() {
        return sy + length * Math.sin(Math.toRadians(angleDeg));
    }

    public String command() {
        return String.format(java.util.Locale.ROOT,
                "cable %.4f %.4f %.4f %.4f %d %d %.2e %d %.1e",
                sx, sy, ex(), ey(), nodes, cableMat, area, groutMat, pretension);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT, "(%.2f, %.2f)", sx, sy);
    }
}
