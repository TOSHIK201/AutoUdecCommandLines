import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Step1 {
    public static void step1(String path, boolean callNext) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
                )
        ) {
            writer.write("new\n");
            writer.write("\n");
            writer.write("set ovtol " + ParameterList.ovtol + "\n");
            writer.write("round " + ParameterList.round + "\n");
            writer.write("title\n");
            writer.write(ParameterList.step1Title + "\n");
            writer.write("\n");
            writer.write("block 0 0 0 " + ParameterList.heightList[0] + " " + ParameterList.modelLength + " ");
            writer.write(ParameterList.heightList[0] + " " + ParameterList.modelLength + " " + 0 + "\n");
            writer.write("\n");

            writer.write(Util.generateHorizontalCracks(ParameterList.heightList, ParameterList.modelLength));
            writer.write("\n");

            List<ExcavationLayer> excavation = new ArrayList<>(ParameterList.excavationLayers);
            excavation.sort((a, b) -> Integer.compare(b.layerIndex, a.layerIndex));
            for (ExcavationLayer e : excavation) {
                int idx = Math.max(0, Math.min(e.layerIndex, ParameterList.heightList.length - 2));
                List<Double> X = Util.calculateExcavationXList(e);
                double bottom = excavationBottom(e, idx);
                double height = e.overallHeight > 0 ? e.overallHeight : ParameterList.layers.get(idx).thickness;
                writer.write(Util.generateVerticalCracks(X, bottom, bottom + height));
                writer.write("\n");
            }
            for (ExcavationLayer e : excavation) {
                int idx = Math.max(0, Math.min(e.layerIndex, ParameterList.heightList.length - 2));
                List<Double> X = Util.calculateExcavationXList(e);
                double bottom = excavationBottom(e, idx);
                double height = e.overallHeight > 0 ? e.overallHeight : ParameterList.layers.get(idx).thickness;
                writer.write(Util.generateGroupCommands(X, bottom, bottom + height, e.name));
            }
            writer.write("\n");

            writer.write(Util.generateJregions(ParameterList.heightList, ParameterList.modelLength));
            writer.write("\n");
            writer.write(Util.generateJsetsFromLayers(ParameterList.heightList, ParameterList.layerJoints, ParameterList.modelLength));

            writer.write("save " + ParameterList.outputPath + "/dat/step_1.dat\n");
            writer.write("set output " + ParameterList.outputPath + "/output/step_1.jpg\n");
            writer.write("set plot jpg size " + ParameterList.outputWidth + " " + ParameterList.outputHeight + "\n");
            writer.write("plot block\n");
            writer.write("copy\n");

            if (callNext) {
                writer.write("\n");
                writer.write("call " + ParameterList.outputPath + "/code/step_2.txt\n");
            }

        } catch (IOException e){
            System.out.println("step1():" + e.getMessage());
        }
    }

    private static double excavationBottom(ExcavationLayer e, int idx) {
        if (e.centered || Double.isNaN(e.originY)) {
            return ParameterList.heightList[idx + 1];
        }
        return e.originY;
    }
}
