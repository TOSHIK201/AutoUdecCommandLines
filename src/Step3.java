import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Step3 {
    public static void step3(String path, boolean callNext) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {

            writer.write("restore " + ParameterList.outputPath + "/dat/step_2.dat\n\n");

            String g = (ParameterList.gravity == Math.rint(ParameterList.gravity)) ? String.valueOf((int) ParameterList.gravity) : String.valueOf(ParameterList.gravity);
            writer.write("set gravity 0 " + g + "\n\n");

            // 应力边界必须先于速度边界（应力会清除同点速度条件）
            List<BoundaryCondition> boundaries = new ArrayList<>(ParameterList.boundaries);
            boundaries.sort((a, b) -> Boolean.compare(b.type.stress(), a.type.stress()));
            for (BoundaryCondition b : boundaries) writer.write(b.command() + "\n");
            writer.write("\n");

            writer.write("damp auto\n");

            // 勾选顺序决定 hist 编号；先 reset，避免 restore 的存档历史干扰
            List<PlotPreset> selectedPlots = new ArrayList<>();
            for (PlotPreset preset : ParameterList.plotPresets3) {
                if (preset.selected) selectedPlots.add(preset);
            }
            selectedPlots.sort((a, b) -> Integer.compare(
                    a.order > 0 ? a.order : Integer.MAX_VALUE,
                    b.order > 0 ? b.order : Integer.MAX_VALUE));
            List<PlotPreset> histPlots = new ArrayList<>();
            for (PlotPreset preset : selectedPlots) {
                if (preset.histCommand != null) histPlots.add(preset);
            }
            if (!histPlots.isEmpty()) {
                writer.write("reset hist\n");
                for (int i = 0; i < histPlots.size(); i++) {
                    PlotPreset preset = histPlots.get(i);
                    String line = preset.histCommand;
                    if (preset.console) line += " type " + (i + 1);
                    writer.write(line + "\n");
                }
            }
            writer.write("solve\n\n");

            writer.write("save " + ParameterList.outputPath + "/dat/step_3.dat\n\n");

            for (PlotPreset preset : selectedPlots) {
                String plot = preset.histCommand != null
                        ? "plot hist " + (histPlots.indexOf(preset) + 1)
                        : preset.plotCommand;
                String title = preset.title == null || preset.title.trim().isEmpty()
                        ? preset.label : preset.title;
                writer.write(Util.generatePlotCommands(title, plot, preset.fileName));
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
