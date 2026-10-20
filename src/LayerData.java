import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class LayerData {
    public String name;
    public double thickness;
    public double E;
    public double poisson;
    public double density;
    public double friction;
    public double cohesion;
    public double tension;
    public double jkn;
    public double jks;
    public double jfric;
    public double jcoh;
    public double jten;
    public double meshSize;
    public Color color;
    public Color savedColor;
    public final List<JointSet> joints = new ArrayList<>();

    public LayerData(String name, double thickness, double E, double poisson, double density,
                     double friction, double cohesion, double tension,
                     double jkn, double jks, double jfric, double jcoh, double jten,
                     double meshSize, Color color) {
        this.name = name;
        this.thickness = thickness;
        this.E = E;
        this.poisson = poisson;
        this.density = density;
        this.friction = friction;
        this.cohesion = cohesion;
        this.tension = tension;
        this.jkn = jkn;
        this.jks = jks;
        this.jfric = jfric;
        this.jcoh = jcoh;
        this.jten = jten;
        this.meshSize = meshSize;
        this.color = color;
        JointSet j0 = new JointSet(0.0);
        j0.spacing = meshSize;
        j0.baseGroup = true;
        j0.linkSpacing = true;
        j0.bridgeLength = 0.0;
        j0.bridgeStdDev = 0.0;
        j0.angleStdDev = 0.0;
        j0.traceStdDev = 0.0;

        JointSet j90 = new JointSet(90.0);
        j90.spacing = meshSize;
        j90.baseGroup = true;
        j90.linkSpacing = true;
        j90.bridgeLength = 0.0;
        j90.bridgeStdDev = 0.0;
        j90.angleStdDev = 0.0;
        j90.traceStdDev = 0.0;

        joints.add(j0);
        joints.add(j90);
    }
}
