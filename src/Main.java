import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main{
    public static String path_commandLine = ParameterList.outputPath + "/code/commandLine.txt";
    public static String path_Step1 = ParameterList.outputPath + "/code/step_1.txt";
    public static String path_Step2 = ParameterList.outputPath + "/code/step_2.txt";
    public static String path_Step3 = ParameterList.outputPath + "/code/step_3.txt";
    public static String path_Step4 = ParameterList.outputPath + "/code/step_4.txt";

    public static void main(String[] args) {

        try {
            Files.createDirectories(Paths.get(ParameterList.outputPath + "/code"));
            Files.createDirectories(Paths.get(ParameterList.outputPath + "/dat"));
            Files.createDirectories(Paths.get(ParameterList.outputPath + "/output"));
        } catch (IOException e){
            System.out.println("main():" + e.getMessage());;
        }

        CommandLines.commandLines(path_commandLine);
        Step1.step1(path_Step1);
        Step2.step2(path_Step2);
        Step3.step3(path_Step3);
        Step4.step4(path_Step4);
    }
}