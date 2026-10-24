import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultStep4Test {
    public static void main(String[] args) throws Exception {
        check("bolt groups", ParameterList.boltGroups.size() == 4);
        BoltGroup b1 = ParameterList.boltGroups.get(0);
        check("corner angles", Math.abs(b1.bolts.get(0).angleDeg - 105) < 1e-9
                && Math.abs(b1.bolts.get(7).angleDeg - 75) < 1e-9);
        BoltGroup b3 = ParameterList.boltGroups.get(2);
        check("wall angles", Math.abs(b3.bolts.get(0).angleDeg + 195) < 1e-9
                && Math.abs(b3.bolts.get(4).angleDeg + 165) < 1e-9);
        check("actions", ParameterList.stage4Actions.size() == 23);

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out = dir.resolve("step_4_default.txt");
        Step4.step4(out.toString());
        String text = new String(Files.readAllBytes(out), Charset.forName("GBK"));
        check("angled cable", text.contains("cable 100.5000 26.4000 99.8788 28.7182"));
        check("initial plot", text.contains("plot block white cable blue"));
        check("cable props", text.contains(
                "prop mat 11 cb_ymod 2.0e+11 cb_dens 7850 cb_yield 5.1e+08 cb_ycomp 5.1e+08"));
        check("grout props", text.contains(
                "prop mat 12 cb_kbond 3.6e+07 cb_sbond 1.6e+07"));
        check("cut9 plot", text.contains("plot syy fill block white cable blue"));
        check("cut9 log", text.contains("after_state_3_9_pline1_ydis.log"));
        check("no unbal", !text.contains("hist unbal"));
        System.out.println("DEFAULT STEP4 PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
