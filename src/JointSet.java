public class JointSet {
    public double angle;
    public double angleStdDev;
    public double bridgeLength;
    public double bridgeStdDev;
    public double traceLength;
    public double traceStdDev;
    public double spacing;
    public double spacingStdDev;
    public double startX;
    public double startY;
    public boolean linkSpacing = false;
    public boolean baseGroup = false;

    public JointSet() {
        this(0.0);
    }

    public JointSet(double angle) {
        this(angle, 0.0, 0.0);
    }

    public JointSet(double angle, double startX, double startY) {
        this.angle = angle;
        this.startX = startX;
        this.startY = startY;
        this.angleStdDev = 0.0;
        this.bridgeLength = 5.0;
        this.bridgeStdDev = 0.0;
        this.traceLength = 10.0;
        this.traceStdDev = 0.0;
        this.spacing = 2.0;
        this.spacingStdDev = 0.0;
    }
}
