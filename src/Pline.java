public class Pline {
    public double x1, y1, x2, y2;
    public int points = ParameterList.plinePoints;

    public String command() {
        return String.format(java.util.Locale.ROOT, "set pline %.2f %.2f %.2f %.2f %d",
                x1, y1, x2, y2, points);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT, "(%.2f,%.2f)-(%.2f,%.2f) %d点",
                x1, y1, x2, y2, points);
    }
}
