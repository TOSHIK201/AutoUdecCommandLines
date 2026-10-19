import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Step2 {
    public static void step2(String path, boolean callNext) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {

            writer.write("restore " + ParameterList.outputPath + "/dat/step_1.dat\n");
            writer.write("\n");

            writer.write(Util.generateMeshCommands(ParameterList.heightList, ParameterList.spacing, ParameterList.modelLength));
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

            writer.write("title\n");
            writer.write("模型网格生成与岩体参数的设定\n\n");
            writer.write("plot block mat\n");
            writer.write("save " + ParameterList.outputPath + "/dat/step_2.dat\n");
            writer.write("set output " + ParameterList.outputPath + "/output/step_2.jpg\n");
            writer.write("set plot jpg size " + ParameterList.outputWidth + " " + ParameterList.outputHeight + "\n");
            writer.write("copy");

            if (callNext) {
                writer.write("\n");
                writer.write("call " + ParameterList.outputPath + "/code/step_3.txt\n");
            }

        } catch (IOException e){
            System.out.println("stp2():" + e.getMessage());
        }
    }
}
