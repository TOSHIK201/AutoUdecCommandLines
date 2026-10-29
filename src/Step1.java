import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Step1 {
    public static void step1(String path, boolean callNext) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
                )
        ) {
            String base = ParameterList.normalizedOutputPath();
            writer.write("new\n");
            writer.write("\n");
            writer.write("round " + ParameterList.round + "\n");
            writer.write("set edge " + Util.numText(ParameterList.effectiveEdge()) + "\n");
            writer.write("set ovtol " + ParameterList.ovtol + "\n");
            writer.write("title\n");
            writer.write(ParameterList.step1Title + "\n");
            writer.write("\n");
            writer.write("block 0 0 0 " + ParameterList.heightList[0] + " " + ParameterList.modelLength + " ");
            writer.write(ParameterList.heightList[0] + " " + ParameterList.modelLength + " " + 0 + "\n");
            writer.write("\n");

            writer.write(Util.generateHorizontalCracks(ParameterList.heightList, ParameterList.modelLength));
            writer.write("\n");

            List<ExcavationLayer> excavation = new ArrayList<>(ParameterList.excavationLayers);
            for (ExcavationLayer e : excavation) {
                List<Double> X = Util.calculateExcavationXList(e);
                double bottom = Util.excavationBottom(e, 0);
                double top = bottom + Util.excavationHeight(e, 0);
                writeBoundaryCracks(writer, X, bottom, top);
                writer.write(Util.generateVerticalCracks(X, bottom, top));
                writer.write("\n");
            }
            for (ExcavationLayer e : excavation) {
                List<Double> X = Util.calculateExcavationXList(e);
                double bottom = Util.excavationBottom(e, 0);
                double top = bottom + Util.excavationHeight(e, 0);
                if (X.size() >= 2) {
                    writer.write(Util.generateGroupCommands(X, bottom, top, e.name));
                }
            }
            writer.write("\n");

            writer.write(Util.generateJregions(ParameterList.heightList, ParameterList.modelLength));
            writer.write("\n");
            writer.write(Util.generateJsetsFromLayers(ParameterList.heightList, ParameterList.layerJoints, ParameterList.modelLength));

            writer.write("save " + base + "/dat/step_1.sav\n");
            for (PlotPreset preset : ParameterList.plotPresets1) {
                if (preset.selected && preset.histCommand == null) {
                    writer.write(Util.generatePlotCommands(preset.title, preset.plotCommand, preset.fileName));
                }
            }

            if (callNext) {
                writer.write("\n");
                writer.write("call " + base + "/code/step_2.dat\n");
            }

        }
    }

    private static void writeBoundaryCracks(BufferedWriter writer, List<Double> X,
                                            double bottom, double top) throws IOException {
        // 边界落在层内时补水平裂隙，保证 group range 四边闭合
        if (X.isEmpty()) return;
        double xMin = X.get(0);
        double xMax = X.get(0);
        for (double x : X) {
            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
        }
        if (!isExistingHorizontalCrack(bottom)) {
            writer.write(Util.generateHorizontalCrack(xMin, xMax, bottom));
        }
        if (!isExistingHorizontalCrack(top)) {
            writer.write(Util.generateHorizontalCrack(xMin, xMax, top));
        }
    }

    private static boolean isExistingHorizontalCrack(double y) {
        for (double h : ParameterList.heightList) {
            if (Math.abs(y - h) < 1e-6) return true;
        }
        return false;
    }
}
