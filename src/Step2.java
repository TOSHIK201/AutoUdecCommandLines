import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Step2 {
    public static void step2(String path, boolean callNext) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {
            String base = ParameterList.normalizedOutputPath();

            writer.write("restore " + base + "/dat/step_1.sav\n");
            writer.write("\n");

            writer.write(Util.generateMeshCommands(ParameterList.heightList, ParameterList.layerJoints,
                    ParameterList.spacing, ParameterList.modelLength));
            writer.write("\n");

            writer.write(Util.generateMatCommands(
                    ParameterList.heightList, ParameterList.modelLength,
                    ParameterList.raw_E, ParameterList.raw_poisson, ParameterList.raw_density,
                    ParameterList.raw_friction, ParameterList.raw_cohesion, ParameterList.raw_tension
            ));
            writer.write("\n");

            writer.write(Util.generatePropJmatCommands(
                    ParameterList.heightList, ParameterList.jmat_kn, ParameterList.jmat_ks,
                    ParameterList.jmat_fric, ParameterList.jmat_coh, ParameterList.jmat_ten, ParameterList.modelLength
            ));
            writer.write("\n");

            writer.write("save " + base + "/dat/step_2.sav\n");
            for (PlotPreset preset : ParameterList.plotPresets2) {
                if (preset.selected && preset.histCommand == null) {
                    writer.write(Util.generatePlotCommands(preset.title, preset.plotCommand, preset.fileName));
                }
            }

            if (callNext) {
                writer.write("\n");
                writer.write("call " + base + "/code/step_3.dat\n");
            }

        }
    }
}
