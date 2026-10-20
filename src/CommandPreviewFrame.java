import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CommandPreviewFrame extends JFrame {
    private final JTextArea area1 = area();
    private final JTextArea area2 = area();
    private final JTextArea area3 = area();

    public CommandPreviewFrame() {
        setTitle("生成命令预览");
        setSize(980, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("step_1.txt", new JScrollPane(area1));
        tabs.addTab("step_2.txt", new JScrollPane(area2));
        tabs.addTab("step_3.txt", new JScrollPane(area3));
        add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bottom.add(new JLabel("编码：GBK  |  "));
        JButton refresh = new JButton("重新生成并刷新");
        refresh.addActionListener(e -> load());
        JButton close = new JButton("关闭");
        close.addActionListener(e -> dispose());
        bottom.add(refresh);
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);

        load();
    }

    private void load() {
        try {
            Main.generateAllFiles();
            area1.setText(read("step_1.txt"));
            area2.setText(read("step_2.txt"));
            area3.setText(read("step_3.txt"));
        } catch (Exception e) {
            area1.setText("生成或读取失败：\n" + e);
        }
    }

    private String read(String name) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(ParameterList.outputPath + "/code/" + name));
            return new String(bytes, Charset.forName("GBK"));
        } catch (Exception e) {
            return "无法读取 " + name + "\n" + e;
        }
    }

    private static JTextArea area() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        a.setLineWrap(false);
        a.setTabSize(4);
        return a;
    }
}
