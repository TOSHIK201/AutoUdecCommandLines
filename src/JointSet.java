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
        this(0.0, ParameterList.jointStartX, ParameterList.jointStartY);
    }

    public JointSet(double angle) {
        this(angle, ParameterList.jointStartX, ParameterList.jointStartY);
    }

    public JointSet(double angle, double startX, double startY) {
        this.angle = angle;
        this.startX = startX;
        this.startY = startY;
        ParameterList.applyJointDefaults(this);
    }
}
