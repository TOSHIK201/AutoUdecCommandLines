import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CommandLines {
    public static void commandLines(String path) {
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        )){
            for (int i = 1; i <= 4; i++) {
                writer.write("call " + ParameterList.outputPath + "/code/step_" + i + ".txt\n");
            }

        } catch (IOException e){
            System.out.println("commandLines():" + e.getMessage());
        }
    }
}