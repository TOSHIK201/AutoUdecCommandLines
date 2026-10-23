import java.util.ArrayList;
import java.util.List;

public class LogGroup {
    public String name;
    public String fileName;
    public final List<LogItem> items = new ArrayList<>();

    public LogGroup(String name) {
        this.name = name;
    }

    public String autoFileName() {
        if (items.isEmpty()) return "stage4.log";
        LogItem first = items.get(0);
        StringBuilder sb = new StringBuilder();
        if (first.pline != null) sb.append("pline").append(first.pline).append("_");
        sb.append(safe(first.data));
        if (items.size() > 1) sb.append("_multi");
        sb.append(".log");
        return sb.toString();
    }

    public String fileName() {
        return fileName == null || fileName.trim().isEmpty() ? autoFileName() : fileName.trim();
    }

    private String safe(String value) {
        if (value == null) return "data";
        return value.trim().replaceAll("[^A-Za-z0-9_]+", "_");
    }
}
