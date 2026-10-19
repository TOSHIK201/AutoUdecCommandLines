import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Step4 {
    public static void step4(String path) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                Charset.forName("GBK"),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )
        ) {

            writer.write("step2");

        } catch (IOException e){
            System.out.println("stp4():" + e.getMessage());
        }
    }
}