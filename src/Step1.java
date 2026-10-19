import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Step1 {
    public static void step1(String path) {
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
            writer.write("step1\n");
            writer.write("\n");
            writer.write("block 0 0 0 " + ParameterList.heightList[0] + " " + ParameterList.modelLength + " ");
            writer.write(ParameterList.heightList[0] + " " + ParameterList.modelLength + " " + 0 + "\n");
            writer.write("\n");

            writer.write(Util.generateHorizontalCracks(ParameterList.heightList, ParameterList.modelLength));
            writer.write("\n");

            List<Double> X = Util.calculateExcavationXList(ParameterList.step, ParameterList.stepDist, ParameterList.cutWidth,
                    ParameterList.direction);

            writer.write(Util.generateVerticalCracks(X, ParameterList.heightList[9], ParameterList.heightList[8]));
            writer.write("\n");
            writer.write(Util.generateVerticalCracks(X, ParameterList.heightList[7], ParameterList.heightList[6]));
            writer.write("\n");

            writer.write(Util.generateGroupCommands(X, ParameterList.heightList[9], ParameterList.heightList[8], 10));
            writer.write(Util.generateGroupCommands(X, ParameterList.heightList[7], ParameterList.heightList[6], 9));
            writer.write("\n");

            writer.write(Util.generateJregions(ParameterList.heightList, ParameterList.modelLength));
            writer.write("\n");
            writer.write(Util.generateJsets(ParameterList.modelLength, ParameterList.spacing));

            writer.write("save " + ParameterList.outputPath + "/dat/step_1.dat\n");
            writer.write("set output " + ParameterList.outputPath + "/output/step_1.jpg\n");
            writer.write("set plot jpg size " + ParameterList.outputWidth + " " + ParameterList.outputHeight + "\n");
            writer.write("copy\n");

        } catch (IOException e){
            System.out.println("step1():" + e.getMessage());
        }
    }
}
