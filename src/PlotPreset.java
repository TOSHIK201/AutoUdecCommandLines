public class PlotPreset {
    public String label;
    public String plotCommand;
    public String title;
    public String fileName;
    public boolean selected;
    public String histCommand;
    public int order;
    public boolean console;

    public PlotPreset(String label, String plotCommand, boolean selected, String title, String fileName) {
        this(label, plotCommand, null, selected, title, fileName);
    }

    public PlotPreset(String label, String plotCommand, String histCommand,
                      boolean selected, String title, String fileName) {
        this.label = label;
        this.plotCommand = plotCommand;
        this.histCommand = histCommand;
        this.selected = selected;
        this.title = title;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return label;
    }
}
