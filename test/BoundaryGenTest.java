import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BoundaryGenTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);

        BoundaryCondition v = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_X, 20, 10, 20, 30, 0.5);
        v.value = -0.02;
        ParameterList.boundaries.add(v);
        BoundaryCondition d = new BoundaryCondition(
                BoundaryCondition.Type.DISPLACE_Y, 10, 40, 50, 40, 0.3);
        ParameterList.boundaries.add(d);

        double[] vr = v.range();
        check("v range", vr[0] == 19.75 && vr[1] == 20.25 && vr[2] == 10 && vr[3] == 30);
        double[] dr = d.range();
        check("d range", dr[0] == 10 && dr[1] == 50 && Math.abs(dr[2] - 39.85) < 1e-9);
        check("v command", v.command().equals("bound xvel -0.02 range 19.75 20.25 10.00 30.00"));
        check("d command", d.command().equals("bound yvel 0 range 10.00 50.00 39.85 40.15"));

        ParameterList.modelLength = 170;
        ParameterList.syncFromLayers();
        check("right edge follows model", ParameterList.boundaries.get(1).ex == 170);
        ParameterList.modelLength = 150;
        ParameterList.syncFromLayers();

        Path step3 = dir.resolve("step_3.txt");
        Step3.step3(step3.toString(), false);
        List<String> lines = Files.readAllLines(step3, Charset.forName("GBK"));
        String text = String.join("\n", lines);
        System.out.println(text);
        check("top stress has range", text.contains("bound stress 0.0000e+00 0.0000e+00 0.0000e+00 range 0.00 150.00 62.10 62.30"));
        check("velocity boundary", text.contains("bound xvel -0.02 range 19.75 20.25 10.00 30.00"));
        check("displacement boundary", text.contains("bound yvel 0 range 10.00 50.00 39.85 40.15"));
        check("stress before velocity", text.indexOf("bound stress") < text.indexOf("bound xvel 0"));

        Path step1 = dir.resolve("step_1.txt");
        Path step2 = dir.resolve("step_2.txt");
        Step1.step1(step1.toString(), false);
        Step2.step2(step2.toString(), false);
        String s1 = new String(Files.readAllBytes(step1), Charset.forName("GBK"));
        String s2 = new String(Files.readAllBytes(step2), Charset.forName("GBK"));
        check("step1 plot", s1.contains("output/step_1_5.jpg"));
        check("step2 mesh plot", s2.contains("output/step_2_6.jpg"));
        check("step2 material plot", s2.contains("output/step_2_7.jpg"));

        byte[] saved = ProjectIO.toBytes();
        ParameterList.boundaries.clear();
        ParameterList.plotPresets1.clear();
        ProjectIO.loadBytes(saved);
        check("boundary reload", ParameterList.boundaries.size() == 6);
        check("stage1 preset reload", ParameterList.plotPresets1.size() == 14);

        System.out.println("ALL TESTS PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
