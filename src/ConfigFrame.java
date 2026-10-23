import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class ConfigFrame extends JFrame {
    private static final Preferences PREFS = Preferences.userNodeForPackage(ConfigFrame.class);
    private LayerCanvas canvas;
    private PropertyPanel propertyPanel;
    private JLabel status;
    private StagePanel stagePanel;
    private JSplitPane split;

    public ConfigFrame() {
        setTitle("AutoUDEC");
        setSize(1280, 860);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int px = PREFS.getInt("x", Integer.MIN_VALUE);
        int py = PREFS.getInt("y", Integer.MIN_VALUE);
        if (px == Integer.MIN_VALUE) {
            setLocationRelativeTo(null);
        } else {
            setBounds(px, py, PREFS.getInt("width", 1280), PREFS.getInt("height", 860));
        }

        propertyPanel = new PropertyPanel();
        canvas = new LayerCanvas(propertyPanel);
        propertyPanel.setCanvas(canvas);

        stagePanel = new StagePanel(propertyPanel, canvas);
        stagePanel.setPreferredSize(new Dimension(460, 0));
        stagePanel.setMinimumSize(new Dimension(420, 0));
        stagePanel.setAutoRun(ParameterList.autoRunAfterGenerate);
        ToolTipManager.sharedInstance().setEnabled(ParameterList.debugMode);

        add(buildHeader(), BorderLayout.NORTH);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, stagePanel);
        split.setDividerLocation(PREFS.getInt("divider", 700));
        split.setResizeWeight(0.62);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        status = new JLabel("就绪");
        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        canvas.setStatusLabel(status);
        add(status, BorderLayout.SOUTH);

        propertyPanel.bindData(0);
        boolean hasView = PREFS.getDouble("viewScale", 0) > 0;
        SwingUtilities.invokeLater(() -> {
            stagePanel.setSelectedTab(PREFS.getInt("tab", 0));
            stagePanel.setSelectedSubTab(PREFS.getInt("subTab", 0));
            if (hasView) {
                canvas.setView(PREFS.getDouble("viewScale", 1.0),
                        PREFS.getDouble("viewOriginX", 60), PREFS.getDouble("viewOriginY", 40));
                canvas.setSnapEnabled(PREFS.getBoolean("snap", true));
                canvas.setGridEnabled(PREFS.getBoolean("grid", false));
            } else {
                canvas.fitToScreen();
            }
            if (PREFS.getBoolean("maximized", false)) setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control Z"), "undoProject");
        getRootPane().getActionMap().put("undoProject", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                undoProject();
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                saveWindowState();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel north = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton load = new JButton("打开工程");
        load.setToolTipText("加载 .audec 工程文件");
        load.addActionListener(e -> loadProject());
        JButton save = new JButton("保存工程");
        save.setToolTipText("保存为 .audec 工程文件");
        save.addActionListener(e -> saveProject());
        JButton undo = new JButton("撤销");
        undo.setToolTipText("撤销上一步编辑（Ctrl+Z）");
        undo.addActionListener(e -> undoProject());
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
        top.add(load);
        top.add(save);
        top.add(undo);

        JPanel global = new JPanel(new BorderLayout());
        global.setBorder(BorderFactory.createTitledBorder("全局输出"));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row1.add(new JLabel("输出路径:"));
        JTextField path = new JTextField(ParameterList.outputPath, 30);
        path.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                String text = path.getText().trim();
                if (!text.isEmpty()) {
                    UndoManager.push();
                    ParameterList.outputPath = text;
                }
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
                if (!text.isEmpty()) {
                    UndoManager.push();
                    ParameterList.udecExePath = text;
                }
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
            UndoManager.push();
            ParameterList.autoRunAfterGenerate = autoRun.isSelected();
            stagePanel.setAutoRun(autoRun.isSelected());
        });
        row2.add(autoRun);

        JCheckBox debug = new JCheckBox("调试模式", ParameterList.debugMode);
        debug.setToolTipText("按倍数放大节理间距；悬停按钮显示说明");
        JTextField debugScale = new JTextField(String.valueOf(ParameterList.debugScale), 5);
        debugScale.setToolTipText("调试模式下节理间距的放大倍数");
        debugScale.setEnabled(ParameterList.debugMode);
        bindDouble(debugScale, v -> {
            UndoManager.push();
            ParameterList.debugScale = Math.max(1.0, v);
            canvas.repaint();
        });
        debug.addActionListener(e -> {
            UndoManager.push();
            ParameterList.debugMode = debug.isSelected();
            debugScale.setEnabled(debug.isSelected());
            ToolTipManager.sharedInstance().setEnabled(debug.isSelected());
            canvas.repaint();
        });
        row2.add(debug);
        row2.add(new JLabel("倍数:"));
        row2.add(debugScale);

        JButton preview = new JButton("预览命令");
        preview.setToolTipText("以 GBK 显示生成的 step_1/2/3 命令");
        preview.addActionListener(e -> new CommandPreviewFrame().setVisible(true));
        row2.add(preview);

        JButton open = new JButton("打开输出文件夹");
        open.setToolTipText("在资源管理器中打开输出路径");
        open.addActionListener(e -> openOutputFolder());
        row2.add(open);

        JButton genAll = new JButton("生成全部（1-4）");
        genAll.setToolTipText("生成阶段1-4；勾选自动调用时从 step_1 链式运行到 step_4");
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
            if (!text.isEmpty()) {
                UndoManager.push();
                setter.accept(Integer.parseInt(text));
            }
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
            if (!text.isEmpty()) {
                UndoManager.push();
                setter.accept(Double.parseDouble(text));
            }
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
            UndoManager.push();
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
        UndoManager.push();
        UndoManager.suspend(() -> {
            LayerData base = ParameterList.layers.get(0);
            LayerData layer = new LayerData("新岩层", 2.0, base.E, base.poisson, base.density,
                    base.friction, base.cohesion, base.tension,
                    base.jkn, base.jks, base.jfric, base.jcoh, base.jten,
                    base.meshSize, new Color(170, 170, 170));
            ParameterList.layers.add(0, layer);
            for (ExcavationLayer e : ParameterList.excavationLayers) {
                if (e.layerIndex >= 0) e.layerIndex++;
            }
            ParameterList.syncFromLayers();
            canvas.selectLayer(0);
            status.setText("已在顶部新建岩层");
        });
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
        UndoManager.push();
        UndoManager.suspend(() -> {
            ParameterList.layers.remove(index);
            ParameterList.excavationLayers.removeIf(e -> e.layerIndex == index);
            for (ExcavationLayer e : ParameterList.excavationLayers) {
                if (e.layerIndex > index) e.layerIndex--;
            }
            ParameterList.syncFromLayers();
            int next = Math.min(index, ParameterList.layerCount() - 1);
            canvas.selectLayer(next);
            status.setText("已删除岩层");
        });
    }

    private void saveProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("保存 AutoUDEC 工程");
        chooser.setFileFilter(new FileNameExtensionFilter("AutoUDEC 工程 (*.audec)", "audec"));
        if (!ParameterList.projectPath.isEmpty()) {
            chooser.setSelectedFile(new File(ParameterList.projectPath));
        }
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".audec")) {
            file = new File(file.getParentFile(), file.getName() + ".audec");
        }
        try {
            ProjectIO.save(file.toPath());
            ParameterList.projectPath = file.getAbsolutePath();
            status.setText("已保存工程：" + file.getName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存工程失败：\n" + e.getMessage());
        }
    }

    private void loadProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("打开 AutoUDEC 工程");
        chooser.setFileFilter(new FileNameExtensionFilter("AutoUDEC 工程 (*.audec)", "audec"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            ProjectIO.load(chooser.getSelectedFile().toPath());
            ParameterList.projectPath = chooser.getSelectedFile().getAbsolutePath();
            UndoManager.clear();
            rebuildUi("已加载工程：" + chooser.getSelectedFile().getName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载工程失败：\n" + e.getMessage());
        }
    }

    private void undoProject() {
        if (!UndoManager.undo()) {
            status.setText("没有可撤销的操作");
            return;
        }
        rebuildUi("已撤销上一步");
    }

    private void rebuildUi(String message) {
        UndoManager.suspend(() -> rebuildUiInternal(message));
    }

    private void rebuildUiInternal(String message) {
        int divider = split == null ? 700 : split.getDividerLocation();
        int tab = stagePanel == null ? 0 : stagePanel.getSelectedTab();
        int subTab = stagePanel == null ? 0 : stagePanel.getSelectedSubTab();
        int layerIndex = canvas == null ? 0 : canvas.getSelectedIndex();
        double scale = canvas == null ? 1.0 : canvas.getScale();
        double originX = canvas == null ? 60 : canvas.getOriginX();
        double originY = canvas == null ? 40 : canvas.getOriginY();
        boolean snap = canvas == null || canvas.isSnapEnabled();
        boolean grid = canvas != null && canvas.isGridEnabled();

        getContentPane().removeAll();
        propertyPanel = new PropertyPanel();
        canvas = new LayerCanvas(propertyPanel);
        propertyPanel.setCanvas(canvas);
        canvas.setView(scale, originX, originY);
        canvas.setSnapEnabled(snap);
        canvas.setGridEnabled(grid);

        stagePanel = new StagePanel(propertyPanel, canvas);
        stagePanel.setPreferredSize(new Dimension(460, 0));
        stagePanel.setMinimumSize(new Dimension(420, 0));
        stagePanel.setAutoRun(ParameterList.autoRunAfterGenerate);
        ToolTipManager.sharedInstance().setEnabled(ParameterList.debugMode);

        add(buildHeader(), BorderLayout.NORTH);
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, stagePanel);
        split.setDividerLocation(divider);
        split.setResizeWeight(0.62);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        status = new JLabel(message);
        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        canvas.setStatusLabel(status);
        add(status, BorderLayout.SOUTH);

        int index = Math.max(0, Math.min(layerIndex, ParameterList.layerCount() - 1));
        propertyPanel.bindData(index);
        canvas.selectLayer(index);
        getContentPane().revalidate();
        getContentPane().repaint();
        SwingUtilities.invokeLater(() -> {
            split.setDividerLocation(divider);
            stagePanel.setSelectedTab(tab);
            stagePanel.setSelectedSubTab(subTab);
            status.setText(message);
        });
    }

    private void saveWindowState() {
        PREFS.putInt("x", getX());
        PREFS.putInt("y", getY());
        PREFS.putInt("width", getWidth());
        PREFS.putInt("height", getHeight());
        PREFS.putBoolean("maximized", (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
        PREFS.putInt("divider", split == null ? 700 : split.getDividerLocation());
        PREFS.putInt("tab", stagePanel == null ? 0 : stagePanel.getSelectedTab());
        PREFS.putInt("subTab", stagePanel == null ? 0 : stagePanel.getSelectedSubTab());
        if (canvas != null) {
            PREFS.putDouble("viewScale", canvas.getScale());
            PREFS.putDouble("viewOriginX", canvas.getOriginX());
            PREFS.putDouble("viewOriginY", canvas.getOriginY());
            PREFS.putBoolean("snap", canvas.isSnapEnabled());
            PREFS.putBoolean("grid", canvas.isGridEnabled());
        }
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
    }
}
