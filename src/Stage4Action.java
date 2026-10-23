import java.util.ArrayList;
import java.util.List;

public class Stage4Action {
    public enum Type {
        EXCAVATE("开挖"),
        SOLVE("解算"),
        BOLT("锚杆"),
        PLINE("测线"),
        MATERIAL("材质"),
        SAVE("保存"),
        PLOT("绘图"),
        LOG("日志");

        public final String label;

        Type(String label) {
            this.label = label;
        }
    }

    public Type type;
    public String group;
    public boolean solve = true;
    public String name;
    public int cableMat;
    public int groutMat;
    public double cbYmod = 200e9;
    public double cbDensity = 7850;
    public double cbYield = 510e6;
    public double cbYcomp = 510e6;
    public double cbKbond = 36e6;
    public double cbSbond = 16e6;
    public Integer pline;
    public String data;
    public final List<String> plotLabels = new ArrayList<>();
    public final List<PlotItem> plotItems = new ArrayList<>();

    public Stage4Action(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        switch (type) {
            case EXCAVATE:
                return "开挖 " + (group == null ? "" : group) + (solve ? " + solve" : "");
            case SAVE:
                return "保存 " + (name == null ? "" : name);
            case BOLT:
                return "锚杆 " + (name == null ? "" : name);
            case MATERIAL:
                return "材质 锚杆" + cableMat + " / 锚固剂" + groutMat;
            case PLOT:
                return "绘图 " + (name == null ? "" : name)
                        + (plotItems.isEmpty() ? "" : " [" + itemLabels() + "]");
            case LOG:
                return "日志 " + (name == null ? "" : name);
            default:
                return type.label;
        }
    }

    private String itemLabels() {
        StringBuilder sb = new StringBuilder();
        for (PlotItem item : plotItems) {
            if (sb.length() > 0) sb.append(',');
            sb.append(item.label);
        }
        return sb.toString();
    }
}
