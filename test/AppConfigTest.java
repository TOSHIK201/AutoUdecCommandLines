import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfigTest {
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get("test_out", "appconfig");
        Files.createDirectories(dir);
        Path config = dir.resolve("defaults.properties");
        Files.deleteIfExists(config);
        System.setProperty("autoudec.config", config.toString());

        Path template = AppConfig.ensureTemplate();
        check("template created", Files.exists(template));
        String text = new String(Files.readAllBytes(template), StandardCharsets.UTF_8);
        check("template outputPath", text.contains("outputPath="));
        check("template boltLength", text.contains("boltLength=2.4"));
        check("template startupProject", text.contains("startupProject="));
        check("template direction", text.contains("direction=LEFT_TO_RIGHT"));

        String overrides = "outputPath=" + dir.resolve("out").toString().replace("\\", "/") + "\n"
                + "modelLength=120\n"
                + "step=2\n"
                + "stepDist=8\n"
                + "cutWidth=6\n"
                + "direction=RIGHT_TO_LEFT\n"
                + "boltCount=7\n"
                + "boltLength=3.5\n"
                + "plinePoints=100\n"
                + "unknownKey=1\n";
        Files.write(config, overrides.getBytes(StandardCharsets.UTF_8));
        AppConfig.apply();

        check("outputPath", ParameterList.outputPath.endsWith("/out"));
        check("modelLength", Math.abs(ParameterList.modelLength - 120) < 1e-9);
        check("step", ParameterList.step == 2);
        check("direction", ParameterList.direction == Util.Direction.RIGHT_TO_LEFT);
        check("bolt count default", new BoltGroup("X").count == 7);
        check("bolt length default", Math.abs(new BoltGroup("X").length - 3.5) < 1e-9);
        check("pline points default", new Pline().points == 100);
        check("heights synced", ParameterList.heightList.length == ParameterList.layerCount() + 1);

        ProjectIO.save(dir.resolve("default.audec"));
        ParameterList.modelLength = 99;
        Files.write(config, "startupProject=default.audec\n".getBytes(StandardCharsets.UTF_8));
        AppConfig.apply();
        check("startup project loaded", Math.abs(ParameterList.modelLength - 120) < 1e-9);
        check("project path", ParameterList.projectPath.endsWith("default.audec"));

        System.out.println("APP CONFIG TEST PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
