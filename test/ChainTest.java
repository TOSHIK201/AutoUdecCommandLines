import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChainTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path step3 = dir.resolve("step_3_chain.txt");
        Step3.step3(step3.toString(), true);
        String text = new String(Files.readAllBytes(step3), Charset.forName("GBK"));
        String call = "call " + ParameterList.outputPath + "/code/step_4.dat";
        if (!text.contains(call)) throw new AssertionError("step_3 未调用 step_4");
        System.out.println("CHAIN TEST PASSED");
    }
}
