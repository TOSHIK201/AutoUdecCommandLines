import java.util.ArrayList;
import java.util.List;

public class BoltGroup {
    public String name;
    public double x1, y1, x2, y2;
    public int count = ParameterList.boltCount;
    public double spacing = ParameterList.boltSpacing;
    public double endOffset = 0.0;
    public double length = ParameterList.boltLength;
    public int nodes = ParameterList.boltNodes;
    public int cableMat;
    public int groutMat;
    public double area = ParameterList.boltArea;
    public double pretension = ParameterList.boltPretension;
    public double baseAngle;
    public final List<Bolt> bolts = new ArrayList<>();

    public BoltGroup(String name) {
        this.name = name;
        this.cableMat = ParameterList.cableMaterial();
        this.groutMat = ParameterList.groutMaterial();
    }

    public void rebuild() {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 1e-9 || count < 1) {
            bolts.clear();
            return;
        }
        double nx = -dy / len;
        double ny = dx / len;
        double mx = (x1 + x2) / 2.0;
        double my = (y1 + y2) / 2.0;
        boolean plusInside = insideExcavation(mx + nx * 0.5, my + ny * 0.5);
        boolean minusInside = insideExcavation(mx - nx * 0.5, my - ny * 0.5);
        if (plusInside && !minusInside) {
            nx = -nx;
            ny = -ny;
        }
        baseAngle = Math.toDegrees(Math.atan2(ny, nx));

        if (count < 1) count = 1;
        double offset = count > 1 && spacing > 1e-9
                ? Math.max(0, (len - (count - 1) * spacing) / 2.0) : 0;
        endOffset = offset;
        if (count < 1) count = 1;

        boolean keep = bolts.size() == count;
        List<Bolt> next = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double t;
            if (count == 1) {
                t = 0.5;
            } else if (spacing > 1e-9) {
                t = (offset + i * spacing) / len;
            } else {
                t = (double) i / (count - 1);
            }
            Bolt b = keep ? bolts.get(i) : new Bolt();
            b.sx = x1 + dx * t;
            b.sy = y1 + dy * t;
            if (!b.custom) {
                b.length = length;
                b.angleDeg = baseAngle;
                b.nodes = nodes;
                b.cableMat = cableMat;
                b.groutMat = groutMat;
                b.area = area;
                b.pretension = pretension;
            }
            next.add(b);
        }
        bolts.clear();
        bolts.addAll(next);
    }

    public void applyDefaultsTo(Bolt b) {
        b.length = length;
        b.angleDeg = baseAngle;
        b.nodes = nodes;
        b.cableMat = cableMat;
        b.groutMat = groutMat;
        b.area = area;
        b.pretension = pretension;
        b.custom = false;
    }

    public List<String> commands() {
        List<String> list = new ArrayList<>();
        for (Bolt b : bolts) list.add(b.command());
        return list;
    }

    private boolean insideExcavation(double x, double y) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            double xa = Math.min(e.x1, e.x2);
            double xb = Math.max(e.x1, e.x2);
            double ya = Math.min(e.y1, e.y2);
            double yb = Math.max(e.y1, e.y2);
            if (x >= xa && x <= xb && y >= ya && y <= yb) return true;
        }
        return false;
    }
}
