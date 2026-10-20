public class ExcavationLayer {
    public int layerIndex;
    public String name;
    public String preset = "开切眼 + 工作面推进";
    public double cutWidth = 8.0;
    public double overallHeight = 0.0;
    public int steps = 3;
    public double stepDist = 10.0;
    public Util.Direction direction = Util.Direction.LEFT_TO_RIGHT;
    public boolean centered = true;
    public double originX = Double.NaN;
    public double originY = Double.NaN;

    public ExcavationLayer(int layerIndex, String name) {
        this.layerIndex = layerIndex;
        this.name = name;
    }
}
