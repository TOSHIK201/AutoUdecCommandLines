import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Step4 {
    public static void step4(String path) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            writer.write("restore " + ParameterList.outputPath + "/dat/step_3.dat\n\n");

            // 顶板监测层位：9# 煤层顶板与 10# 煤层顶板
            double roof9 = ParameterList.heightList[ParameterList.coal9Layer];
            double roof10 = ParameterList.heightList[ParameterList.coal10Layer];

            // 切眼中心：开挖区整体居中，切眼位于最左侧
            double cutCenterX = (ParameterList.modelLength
                    - (ParameterList.cutWidth + ParameterList.step * ParameterList.stepDist)) / 2.0
                    + ParameterList.cutWidth / 2.0;
            double supportY = roof9 - ParameterList.supportOffset;

            writeMonitorDefinitions(writer, roof9, roof10);

            // 1. 开挖 9# 煤层切眼
            writer.write("delete range group openOffCut_9\n");
            writer.write("solve\n\n");

            // 2. 切眼支护：钢支架横跨切眼，靠近 9# 煤层顶板
            writer.write(String.format("prop mat %d sup_kn %.1e sup_spacing %.1f sup_ycomp %.1e\n",
                    ParameterList.supportMat, ParameterList.supportKn,
                    ParameterList.supportSpacing, ParameterList.supportYcomp));
            writer.write(String.format("support %.2f,%.2f angle 0 material %d width %.2f segment %d type standard\n",
                    cutCenterX, supportY, ParameterList.supportMat,
                    ParameterList.cutWidth, ParameterList.supportSegments));
            writer.write("solve\n\n");

            writer.write("save " + ParameterList.outputPath + "/dat/step_4_1.dat\n\n");
            writer.write(Util.generatePlotCommands("9#煤层切眼支护网格图", "plot support", "step_4_1.jpg"));

            // 3. 分步开挖 9# 煤层
            excavate(writer, 9);
            writer.write("save " + ParameterList.outputPath + "/dat/step_4_2.dat\n\n");
            writer.write(Util.generatePlotCommands("9#煤层开挖后Y方向位移云图", "plot block ydis fill", "step_4_2.jpg"));
            writer.write(Util.generatePlotCommands("9#煤层开挖后Y方向应力云图", "plot block syy fill", "step_4_3.jpg"));
            writeMonitorOutput(writer, roof9, 1);

            // 4. 分步开挖 10# 煤层
            excavate(writer, 10);
            writer.write("save " + ParameterList.outputPath + "/dat/step_4_3.dat\n\n");
            writer.write(Util.generatePlotCommands("10#煤层开挖后Y方向位移云图", "plot block ydis fill", "step_4_4.jpg"));
            writer.write(Util.generatePlotCommands("10#煤层开挖后Y方向应力云图", "plot block syy fill", "step_4_5.jpg"));
            writeMonitorOutput(writer, roof10, 1 + monitorCount());

        } catch (IOException e) {
            System.out.println("step4():" + e.getMessage());
        }
    }

    private static void excavate(BufferedWriter writer, int coalNo) throws IOException {
        for (int i = 1; i <= ParameterList.step; i++) {
            writer.write("delete range group state_" + i + "_" + coalNo + "\n");
            writer.write("solve\n\n");
        }
    }

    private static void writeMonitorDefinitions(BufferedWriter writer, double roof9, double roof10) throws IOException {
        for (double x = 0; x <= ParameterList.modelLength + 1e-9; x += ParameterList.monitorStep) {
            writer.write(String.format("hist ydis %.2f %.2f\n", x, roof9));
        }
        for (double x = 0; x <= ParameterList.modelLength + 1e-9; x += ParameterList.monitorStep) {
            writer.write(String.format("hist ydis %.2f %.2f\n", x, roof10));
        }
        writer.write("\n");
    }

    private static int monitorCount() {
        return (int) Math.floor(ParameterList.modelLength / ParameterList.monitorStep + 1e-9) + 1;
    }

    private static void writeMonitorOutput(BufferedWriter writer, double roof, int firstHistory) throws IOException {
        int n = firstHistory;
        int count = monitorCount();
        for (int i = 0; i < count; i++) {
            double x = i * ParameterList.monitorStep;
            String file = String.format("ydis_%02.0f_%03.0f.txt", roof, x);
            writer.write("hist write " + n + " " + ParameterList.outputPath + "/output/" + file + "\n");
            n++;
        }
        writer.write("\n");
    }
}
