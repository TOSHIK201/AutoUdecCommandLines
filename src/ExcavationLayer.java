public class ExcavationLayer {
    public int layerIndex = -1;
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
    public double x1, y1, x2, y2;
    public boolean autoGeometry = true;

    public ExcavationLayer(int layerIndex, String name) {
        this.layerIndex = layerIndex;
        this.name = name;
        ParameterList.applyExcavationDefaults(this);
        this.autoGeometry = layerIndex >= 0;
    }

    public ExcavationLayer(String name, double x1, double y1, double x2, double y2) {
        this(-1, name);
        this.autoGeometry = false;
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
    }

    public int stepCount() {
        double width = Math.abs(x2 - x1);
        double usable = Math.max(0, width - cutWidth);
        if (usable <= 1e-9) return 0;
        if (stepDist <= 1e-9) return 1;
        return Math.max(1, (int) Math.ceil(usable / stepDist - 1e-9));
    }
}
