public class ExcavationLayer {
    public int layerIndex;
    public String name;
    public String preset;
    public double cutWidth;
    public double overallHeight;
    public int steps;
    public double stepDist;
    public Util.Direction direction;
    public boolean centered;
    public double originX = Double.NaN;
    public double originY = Double.NaN;

    public ExcavationLayer(int layerIndex, String name) {
        this.layerIndex = layerIndex;
        this.name = name;
        ParameterList.applyExcavationDefaults(this);
    }
}
