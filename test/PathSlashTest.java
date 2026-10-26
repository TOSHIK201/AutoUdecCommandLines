import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathSlashTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        ParameterList.outputPath = "F:\\Itasca\\UDECProjects\\102\\";
        check("normalize backslash",
                "F:/Itasca/UDECProjects/102".equals(ParameterList.normalizedOutputPath()));

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out1 = dir.resolve("slash_step_1.dat");
        Path out2 = dir.resolve("slash_step_2.dat");
        Path out3 = dir.resolve("slash_step_3.dat");
        Path out4 = dir.resolve("slash_step_4.dat");
        Step1.step1(out1.toString(), true);
        Step2.step2(out2.toString(), true);
        Step3.step3(out3.toString(), true);
        Step4.step4(out4.toString());

        for (Path path : new Path[]{out1, out2, out3, out4}) {
            String text = new String(Files.readAllBytes(path), Charset.forName("GBK"));
            check("no backslash " + path.getFileName(), !text.contains("\\"));
        }
        String step1 = read(out1);
        check("step1 save path", step1.contains("save F:/Itasca/UDECProjects/102/dat/step_1.sav"));
        check("step1 call path", step1.contains("call F:/Itasca/UDECProjects/102/code/step_2.dat"));

        ParameterList.outputPath = "F://Itasca//UDECProjects//102//";
        check("collapse duplicate",
                "F:/Itasca/UDECProjects/102".equals(ParameterList.normalizedOutputPath()));

        ParameterList.outputPath = "\\\\server\\share\\";
        check("unc path", "//server/share".equals(ParameterList.normalizedOutputPath()));

        System.out.println("PATH SLASH TEST PASSED");
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), Charset.forName("GBK"));
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
