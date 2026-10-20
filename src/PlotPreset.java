public class PlotPreset {
    public String label;
    public String plotCommand;
    public String title;
    public String fileName;
    public boolean selected;

    public PlotPreset(String label, String plotCommand, boolean selected, String title, String fileName) {
        this.label = label;
        this.plotCommand = plotCommand;
        this.selected = selected;
        this.title = title;
        this.fileName = fileName;
    }
}
