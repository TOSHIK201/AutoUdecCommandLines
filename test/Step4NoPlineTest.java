import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Step4NoPlineTest {
    public static void main(String[] args) throws Exception {
        ParameterList.stage4Actions.clear();
        ParameterList.stage4Actions.add(new Stage4Action(Stage4Action.Type.SOLVE));
        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out = dir.resolve("step_4_nopline.txt");
        Step4.step4(out.toString());
        String text = new String(Files.readAllBytes(out), Charset.forName("GBK"));
        if (text.contains("set pline")) throw new AssertionError("未指定测线动作却生成了测线命令");
        System.out.println("STEP4 NO PLINE PASSED");
    }
}
