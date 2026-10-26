import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveSuffixTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        ParameterList.outputPath = "F:/Itasca/UDECProjects/102";
        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);

        String step1 = generate(dir.resolve("save_step_1.dat"),
                path -> Step1.step1(path, false));
        String step2 = generate(dir.resolve("save_step_2.dat"),
                path -> Step2.step2(path, false));
        String step3 = generate(dir.resolve("save_step_3.dat"),
                path -> Step3.step3(path, false));

        ParameterList.stage4Actions.clear();
        addSave("a.sav");
        addSave("b.save");
        addSave("c.dat");
        String step4 = generate(dir.resolve("save_step_4.dat"), Step4::step4);

        for (String text : new String[]{step1, step2, step3, step4}) {
            check("no old save suffix", !text.contains(".save"));
        }
        check("step1", step1.contains("/dat/step_1.sav"));
        check("step2 restore", step2.contains("/dat/step_1.sav"));
        check("step2 save", step2.contains("/dat/step_2.sav"));
        check("step3 restore", step3.contains("/dat/step_2.sav"));
        check("step3 save", step3.contains("/dat/step_3.sav"));
        check("step4 restore", step4.contains("/dat/step_3.sav"));
        check("sav input", step4.contains("/dat/a.sav"));
        check("save input", step4.contains("/dat/b.sav"));
        check("dat input", step4.contains("/dat/c.sav"));
        System.out.println("SAVE SUFFIX TEST PASSED");
    }

    private static void addSave(String name) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.SAVE);
        a.name = name;
        ParameterList.stage4Actions.add(a);
    }

    private static String generate(Path path, Writer writer) throws Exception {
        writer.write(path.toString());
        return new String(Files.readAllBytes(path), Charset.forName("GBK"));
    }

    private interface Writer {
        void write(String path) throws Exception;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
