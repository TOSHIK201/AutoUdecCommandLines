public class PlotItem {
    public String label;
    public String command;
    public String title;
    public String fileName;
    public String histCommand;
    public boolean console;

    public PlotItem(String label, String command, String title, String fileName, String histCommand) {
        this.label = label;
        this.command = command;
        this.title = title;
        this.fileName = fileName;
        this.histCommand = histCommand;
    }

    public static PlotItem from(PlotPreset preset) {
        return new PlotItem(preset.label, preset.plotCommand, preset.title,
                preset.fileName, preset.histCommand);
    }
}
