import javax.swing.JOptionPane;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// 外部默认参数：与 exe 同级的 defaults.properties，首次运行自动生成
class AppConfig {
    static final String FILE_NAME = "defaults.properties";

    static Path locate() {
        String override = System.getProperty("autoudec.config");
        if (override != null && !override.trim().isEmpty()) return Paths.get(override.trim());
        Path dir = appDir();
        if (dir != null) return dir.resolve(FILE_NAME);
        return Paths.get(System.getProperty("user.dir"), FILE_NAME);
    }

    static Path ensureTemplate() {
        Path path = locate();
        if (Files.exists(path)) return path;
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.write(path, ConfigTemplate.text().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("[CONFIG] 无法生成参数文件: " + e.getMessage());
        }
        return path;
    }

    static void apply() {
        Path path = locate();
        if (!Files.exists(path)) return;
        String startup = "";
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String text = line.trim();
                if (text.isEmpty() || text.startsWith("#") || text.startsWith("!")) continue;
                int split = text.indexOf('=');
                if (split <= 0) continue;
                String key = text.substring(0, split).trim();
                String value = text.substring(split + 1).trim();
                if ("startupProject".equals(key)) {
                    startup = value;
                } else {
                    setField(key, value);
                }
            }
            ParameterList.syncFromLayers();
            loadStartupProject(path, startup);
            System.out.println("[CONFIG] 已读取 " + path);
        } catch (Exception e) {
            System.out.println("[CONFIG] 读取失败: " + e.getMessage());
        }
    }

    static void openConfig(Component parent) {
        try {
            Path path = ensureTemplate();
            new ProcessBuilder("notepad.exe", path.toString()).start();
            JOptionPane.showMessageDialog(parent,
                    "参数文件：\n" + path + "\n\n修改并保存后，重启程序生效。");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "打开参数文件失败：\n" + e.getMessage());
        }
    }

    private static void loadStartupProject(Path config, String value) {
        if (value == null || value.trim().isEmpty()) return;
        Path project = Paths.get(value.trim());
        if (!project.isAbsolute() && config.getParent() != null) {
            project = config.getParent().resolve(project).normalize();
        }
        if (!Files.exists(project)) {
            System.out.println("[CONFIG] startupProject 不存在: " + project);
            return;
        }
        try {
            ProjectIO.load(project);
            ParameterList.projectPath = project.toString();
            System.out.println("[CONFIG] 已加载默认工程 " + project);
        } catch (Exception e) {
            System.out.println("[CONFIG] 默认工程加载失败: " + e.getMessage());
        }
    }

    private static void setField(String key, String value) {
        try {
            Field field = ParameterList.class.getField(key);
            if (!Modifier.isStatic(field.getModifiers())) return;
            Class<?> type = field.getType();
            if (type == String.class) {
                field.set(null, value);
            } else if (type == boolean.class) {
                field.setBoolean(null, Boolean.parseBoolean(value));
            } else if (type == int.class) {
                field.setInt(null, Integer.parseInt(value));
            } else if (type == double.class) {
                field.setDouble(null, Double.parseDouble(value));
            } else if (type == Util.Direction.class) {
                field.set(null, Util.Direction.valueOf(value.toUpperCase()));
            }
        } catch (NoSuchFieldException e) {
            System.out.println("[CONFIG] 未知参数: " + key);
        } catch (Exception e) {
            System.out.println("[CONFIG] 参数无效: " + key + "=" + value);
        }
    }

    private static Path appDir() {
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath != null && !appPath.trim().isEmpty()) {
            Path parent = Paths.get(appPath).getParent();
            if (parent != null) return parent;
        }
        try {
            Path source = Paths.get(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path parent = source.getParent();
            if (parent != null && "app".equalsIgnoreCase(String.valueOf(parent.getFileName()))) {
                return parent.getParent();
            }
            return parent;
        } catch (Exception e) {
            return null;
        }
    }
}
