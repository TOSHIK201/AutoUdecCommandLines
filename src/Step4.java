import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Step4 {
    public static void step4(String path) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write("restore " + ParameterList.outputPath + "/dat/step_3.dat\n\n");

            List<PlotItem> histPlots = allHistItems();
            writeHist(writer, histPlots);

            for (Stage4Action action : ParameterList.stage4Actions) {
                switch (action.type) {
                    case EXCAVATE:
                        writeExcavate(writer, action);
                        break;
                    case SOLVE:
                        writer.write("solve\n\n");
                        break;
                    case BOLT:
                        writeBolts(writer, action.name);
                        break;
                    case MATERIAL:
                        writeMaterial(writer, action);
                        break;
                    case PLINE:
                        writePlines(writer);
                        break;
                    case SAVE:
                        writer.write("save " + ParameterList.outputPath
                                + "/dat/" + saveName(action.name) + ".dat\n\n");
                        break;
                    case PLOT:
                        writePlots(writer, action, histPlots);
                        break;
                    case LOG:
                        writeLog(writer, action);
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("step4():" + e.getMessage());
        }
    }

    private static List<PlotItem> allHistItems() {
        List<PlotItem> list = new ArrayList<>();
        for (Stage4Action action : ParameterList.stage4Actions) {
            if (action.type != Stage4Action.Type.PLOT) continue;
            for (PlotItem item : action.plotItems) {
                if (item.histCommand != null && !containsLabel(list, item.label)) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    private static boolean containsLabel(List<PlotItem> list, String label) {
        for (PlotItem item : list) {
            if (item.label.equals(label)) return true;
        }
        return false;
    }

    private static void writeHist(BufferedWriter writer, List<PlotItem> hist) throws IOException {
        if (hist.isEmpty()) return;
        writer.write("reset hist\n");
        for (int i = 0; i < hist.size(); i++) {
            String line = hist.get(i).histCommand;
            if (hist.get(i).console) line += " type " + (i + 1);
            writer.write(line + "\n");
        }
        writer.write("\n");
    }

    private static void writeExcavate(BufferedWriter writer, Stage4Action action) throws IOException {
        if (action.group == null || action.group.trim().isEmpty()) return;
        writer.write("delete range group " + action.group.trim() + "\n");
        if (action.solve) writer.write("solve\n");
        writer.write("\n");
    }

    private static void writeMaterial(BufferedWriter writer, Stage4Action action) throws IOException {
        int cable = action.cableMat > 0 ? action.cableMat : ParameterList.cableMaterial();
        int grout = action.groutMat > 0 ? action.groutMat : ParameterList.groutMaterial();
        double ymod = action.cbYmod > 0 ? action.cbYmod : ParameterList.cableYmod;
        double dens = action.cbDensity > 0 ? action.cbDensity : ParameterList.cableDensity;
        double yield = action.cbYield > 0 ? action.cbYield : ParameterList.cableYield;
        double ycomp = action.cbYcomp > 0 ? action.cbYcomp : ParameterList.cableYcomp;
        double kbond = action.cbKbond > 0 ? action.cbKbond : ParameterList.groutKbond;
        double sbond = action.cbSbond > 0 ? action.cbSbond : ParameterList.groutSbond;
        writer.write(String.format(java.util.Locale.ROOT,
                "prop mat %d cb_ymod %.1e cb_dens %.0f cb_yield %.1e cb_ycomp %.1e\n",
                cable, ymod, dens, yield, ycomp));
        writer.write(String.format(java.util.Locale.ROOT,
                "prop mat %d cb_kbond %.1e cb_sbond %.1e\n",
                grout, kbond, sbond));
        writer.write("\n");
    }

    private static void writeBolts(BufferedWriter writer, String name) throws IOException {
        for (BoltGroup group : ParameterList.boltGroups) {
            if (name != null && !name.trim().isEmpty() && !name.equals(group.name)) continue;
            for (Bolt bolt : group.bolts) writer.write(bolt.command() + "\n");
            writer.write("\n");
        }
    }

    private static void writePlines(BufferedWriter writer) throws IOException {
        writer.write("set pline off\n");
        for (Pline pline : ParameterList.plines) writer.write(pline.command() + "\n");
        writer.write("\n");
    }

    private static void writePlots(BufferedWriter writer, Stage4Action action,
                                   List<PlotItem> hist) throws IOException {
        String safeTag = safeTag(action.name);
        for (PlotItem item : action.plotItems) {
            int index = histIndex(hist, item.label);
            String plot = item.histCommand != null ? "plot hist " + index : item.command;
            String baseTitle = item.title == null || item.title.trim().isEmpty()
                    ? item.label : item.title;
            String title = safeTag.isEmpty() ? baseTitle : action.name.trim() + " " + baseTitle;
            String file = safeTag.isEmpty() ? item.fileName : safeTag + "_" + item.fileName;
            writer.write(Util.generatePlotCommands(title, plot, file));
        }
    }

    private static int histIndex(List<PlotItem> hist, String label) {
        for (int i = 0; i < hist.size(); i++) {
            if (hist.get(i).label.equals(label)) return i + 1;
        }
        return 1;
    }

    private static String safeTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) return "";
        return tag.trim().replaceAll("[^A-Za-z0-9_\\-]+", "_");
    }

    private static void writeLog(BufferedWriter writer, Stage4Action action) throws IOException {
        if (action.name == null || action.name.trim().isEmpty()
                || action.data == null || action.data.trim().isEmpty()) return;
        String file = action.name.trim();
        if (!file.toLowerCase().endsWith(".log")) file += ".log";
        writer.write("set log " + ParameterList.outputPath + "/dat/" + file + "\n");
        if (action.pline != null) {
            writer.write("print pline " + action.pline + " " + action.data.trim() + "\n");
        } else {
            writer.write("print " + action.data.trim() + "\n");
        }
        writer.write("set log off\n\n");
    }

    private static String saveName(String name) {
        if (name == null || name.trim().isEmpty()) return "step_4";
        String value = name.trim();
        return value.toLowerCase().endsWith(".dat")
                ? value.substring(0, value.length() - 4) : value;
    }
}
