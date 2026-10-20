import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigFrame extends JFrame {
    private final LayerCanvas canvas;
    private final PropertyPanel propertyPanel;
    private final JLabel status;
    private final StagePanel stagePanel;

    public ConfigFrame() {
        setTitle("AutoUDEC - 可视化建模");
        setSize(1280, 860);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        propertyPanel = new PropertyPanel();
        canvas = new LayerCanvas(propertyPanel);
        propertyPanel.setCanvas(canvas);

        stagePanel = new StagePanel(propertyPanel, canvas);
        stagePanel.setPreferredSize(new Dimension(460, 0));
        stagePanel.setMinimumSize(new Dimension(420, 0));
        stagePanel.setAutoRun(ParameterList.autoRunAfterGenerate);
        ToolTipManager.sharedInstance().setEnabled(ParameterList.debugMode);

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, stagePanel);
        split.setDividerLocation(700);
        split.setResizeWeight(0.62);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        status = new JLabel("就绪");
        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        canvas.setStatusLabel(status);
        add(status, BorderLayout.SOUTH);

        propertyPanel.bindData(0);
        SwingUtilities.invokeLater(canvas::fitToScreen);
    }

    private JPanel buildHeader() {
        JPanel north = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton add = new JButton("+ 新建岩层");
        add.setToolTipText("在模型顶部新增一层岩层");
        add.addActionListener(e -> addLayer());
        JButton del = new JButton("- 删除岩层");
        del.setToolTipText("删除当前选中的岩层（最底层不可删）");
        del.setForeground(Color.RED);
        del.addActionListener(e -> deleteLayer());
        JButton fit = new JButton("适应窗口");
        fit.setToolTipText("缩放并居中显示整个模型");
        fit.addActionListener(e -> canvas.fitToScreen());
        top.add(add);
        top.add(del);
        top.add(fit);

        JPanel global = new JPanel(new BorderLayout());
        global.setBorder(BorderFactory.createTitledBorder("全局输出"));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row1.add(new JLabel("输出路径:"));
        JTextField path = new JTextField(ParameterList.outputPath, 30);
        path.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                String text = path.getText().trim();
                if (!text.isEmpty()) ParameterList.outputPath = text;
            }
        });
        row1.add(path);

        row1.add(new JLabel("图片宽:"));
        JTextField width = new JTextField(String.valueOf(ParameterList.outputWidth), 5);
        bindInt(width, v -> ParameterList.outputWidth = v);
        row1.add(width);

        row1.add(new JLabel("图片高:"));
        JTextField height = new JTextField(String.valueOf(ParameterList.outputHeight), 5);
        bindInt(height, v -> ParameterList.outputHeight = v);
        row1.add(height);

        row1.add(new JLabel("圆角数:"));
        JTextField round = new JTextField(String.valueOf(ParameterList.round), 5);
        bindDouble(round, v -> ParameterList.round = v);
        row1.add(round);

        row1.add(new JLabel("嵌入容差:"));
        JTextField ovtol = new JTextField(String.valueOf(ParameterList.ovtol), 5);
        bindDouble(ovtol, v -> ParameterList.ovtol = v);
        row1.add(ovtol);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row2.add(new JLabel("UDEC 程序:"));
        JTextField exe = new JTextField(ParameterList.udecExePath, 28);
        exe.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                String text = exe.getText().trim();
                if (!text.isEmpty()) ParameterList.udecExePath = text;
            }
        });
        row2.add(exe);

        JButton browse = new JButton("浏览...");
        browse.setToolTipText("选择 UDEC 可执行文件");
        browse.addActionListener(e -> browseUdecExe(exe));
        row2.add(browse);

        JCheckBox autoRun = new JCheckBox("生成后自动调用命令", ParameterList.autoRunAfterGenerate);
        autoRun.setToolTipText("生成后从 step_1 开始链式调用 UDEC");
        autoRun.addActionListener(e -> {
            ParameterList.autoRunAfterGenerate = autoRun.isSelected();
            stagePanel.setAutoRun(autoRun.isSelected());
        });
        row2.add(autoRun);

        JCheckBox debug = new JCheckBox("调试模式", ParameterList.debugMode);
        debug.setToolTipText("节理间距放大10倍；悬停按钮显示说明");
        debug.addActionListener(e -> {
            ParameterList.debugMode = debug.isSelected();
            ToolTipManager.sharedInstance().setEnabled(debug.isSelected());
            canvas.repaint();
        });
        row2.add(debug);

        JButton preview = new JButton("预览命令");
        preview.setToolTipText("以 GBK 显示生成的 step_1/2/3 命令");
        preview.addActionListener(e -> new CommandPreviewFrame().setVisible(true));
        row2.add(preview);

        JButton open = new JButton("打开输出文件夹");
        open.setToolTipText("在资源管理器中打开输出路径");
        open.addActionListener(e -> openOutputFolder());
        row2.add(open);

        JButton genAll = new JButton("生成全部（1-3）");
        genAll.setToolTipText("生成阶段1-3；勾选自动调用时启动 UDEC");
        genAll.addActionListener(e -> Main.generateAllFilesAndRun());
        row2.add(genAll);

        global.add(row1, BorderLayout.NORTH);
        global.add(row2, BorderLayout.SOUTH);

        north.add(top, BorderLayout.NORTH);
        north.add(global, BorderLayout.SOUTH);
        return north;
    }

    private void bindInt(JTextField field, java.util.function.IntConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveInt(field, setter); }
        });
        field.addActionListener(e -> saveInt(field, setter));
    }

    private void saveInt(JTextField field, java.util.function.IntConsumer setter) {
        try {
            String text = field.getText().trim();
            if (!text.isEmpty()) setter.accept(Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
        }
    }

    private void bindDouble(JTextField field, java.util.function.DoubleConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveDouble(field, setter); }
        });
        field.addActionListener(e -> saveDouble(field, setter));
    }

    private void saveDouble(JTextField field, java.util.function.DoubleConsumer setter) {
        try {
            String text = field.getText().trim();
            if (!text.isEmpty()) setter.accept(Double.parseDouble(text));
        } catch (NumberFormatException ignored) {
        }
    }

    private void browseUdecExe(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择 UDEC 可执行文件");
        chooser.setFileFilter(new FileNameExtensionFilter("可执行文件 (*.exe)", "exe"));
        String current = field.getText().trim();
        if (!current.isEmpty()) {
            File f = new File(current);
            if (f.getParentFile() != null) chooser.setCurrentDirectory(f.getParentFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            field.setText(path);
            ParameterList.udecExePath = path;
        }
    }

    private void openOutputFolder() {
        try {
            Path path = Paths.get(ParameterList.outputPath);
            Files.createDirectories(path);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
            } else {
                JOptionPane.showMessageDialog(this, "当前系统不支持打开文件夹");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "打开输出文件夹失败：" + e.getMessage());
        }
    }

    private void addLayer() {
        LayerData base = ParameterList.layers.get(0);
        LayerData layer = new LayerData("新岩层", 2.0, base.E, base.poisson, base.density,
                base.friction, base.cohesion, base.tension,
                base.jkn, base.jks, base.jfric, base.jcoh, base.jten,
                base.meshSize, new Color(170, 170, 170));
        ParameterList.layers.add(0, layer);
        for (ExcavationLayer e : ParameterList.excavationLayers) e.layerIndex++;
        ParameterList.syncFromLayers();
        canvas.selectLayer(0);
        status.setText("已在顶部新建岩层");
    }

    private void deleteLayer() {
        int index = canvas.getSelectedIndex();
        int count = ParameterList.layerCount();
        if (count <= 1) {
            JOptionPane.showMessageDialog(this, "至少保留一层岩层");
            return;
        }
        if (index >= count - 1) {
            JOptionPane.showMessageDialog(this, "最底部岩层不能删除");
            return;
        }
        ParameterList.layers.remove(index);
        ParameterList.excavationLayers.removeIf(e -> e.layerIndex == index);
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e.layerIndex > index) e.layerIndex--;
        }
        ParameterList.syncFromLayers();
        int next = Math.min(index, ParameterList.layerCount() - 1);
        canvas.selectLayer(next);
        status.setText("已删除岩层");
    }
}
