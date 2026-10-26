import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class CodeStagingTest {
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get("test_out/code_staging");
        delete(dir);
        Files.createDirectories(dir);
        ParameterList.outputPath = dir.toString();
        Main.errorReporter = message -> System.out.println("[EXPECTED] " + message);

        check("generate all", Main.generateAllFiles());
        for (int i = 1; i <= 4; i++) {
            check("step " + i, Files.isRegularFile(
                    dir.resolve("code/step_" + i + ".dat")));
        }
        check("no temp after success", tempCount(dir) == 0);

        delete(dir.resolve("code"));
        Files.write(dir.resolve("code"), new byte[]{1});
        check("failure reported", !Main.generateAllFiles());
        check("code placeholder untouched", Files.isRegularFile(dir.resolve("code")));
        check("no temp after failure", tempCount(dir) == 0);

        delete(dir);
        System.out.println("CODE STAGING TEST PASSED");
    }

    private static long tempCount(Path dir) throws Exception {
        try (java.util.stream.Stream<Path> paths = Files.list(dir)) {
            return paths.filter(p -> p.getFileName().toString().startsWith(".audec_tmp_"))
                    .count();
        }
    }

    private static void delete(Path dir) throws Exception {
        if (!Files.exists(dir)) return;
        try (java.util.stream.Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
