import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Step3 {
    public static void step3(String path, boolean callNext) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {

            writer.write("restore " + ParameterList.outputPath + "/dat/step_2.dat\n\n");

            double maxHeight = ParameterList.heightList[0];
            double maxLength = ParameterList.modelLength;
            double tol = ParameterList.boundTol;

            String g = (ParameterList.gravity == Math.rint(ParameterList.gravity)) ? String.valueOf((int) ParameterList.gravity) : String.valueOf(ParameterList.gravity);
            writer.write("set gravity 0 " + g + "\n\n");

            //左
            writer.write(String.format("bound xvel 0 range %.2f %.2f 0.00 %.2f\n",
                    -tol, tol, maxHeight));

            //右
            writer.write(String.format("bound xvel 0 range %.2f %.2f 0.00 %.2f\n",
                    maxLength - tol, maxLength + tol, maxHeight));

            //底
            writer.write(String.format("bound yvel 0 range 0.00 %.2f %.2f %.2f\n",
                    maxLength, -tol, tol));
            writer.write("\n");

            writer.write("damp auto\n");
            writer.write("hist solve_ratio type 1\n");
            writer.write("hist unbal\n");
            writer.write("solve\n\n");

            writer.write("save " + ParameterList.outputPath + "/dat/step_3.dat\n\n");

            for (PlotPreset preset : ParameterList.plotPresets) {
                if (preset.selected) {
                    writer.write(Util.generatePlotCommands(preset.title, preset.plotCommand, preset.fileName));
                }
            }

            if (callNext) {
                writer.write("\n");
                writer.write("call " + ParameterList.outputPath + "/code/step_4.txt\n");
            }

        } catch (IOException e) {
            System.out.println("step3():" + e.getMessage());
        }
    }
}