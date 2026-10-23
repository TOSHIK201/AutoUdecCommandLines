import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// 生成带中文分组的默认参数模板
class ConfigTemplate {
    private static final Set<String> SKIP = new HashSet<>(Arrays.asList(
            "projectPath", "restoring", "coal9Layer", "coal10Layer"));
    private static final String[] STARTUP = {"outputPath", "udecExePath", "autoRunAfterGenerate",
            "s1Calls2", "s2Calls3", "s3Calls4", "outputWidth", "outputHeight"};
    private static final String[] MODEL = {"modelLength", "step", "stepDist", "cutWidth",
            "direction", "excavationHeight", "excavationCentered", "excavationOriginX",
            "excavationOriginY", "excavationPreset"};
    private static final String[] BOUNDARY = {"gravity", "stressEnabled", "boundaryWidth",
            "round", "ovtol", "boundTol", "stressTop", "stressGradient", "stressX", "stressXY"};
    private static final String[] JOINT = {"jointAngle", "jointAngleStdDev", "jointBridgeLength",
            "jointBridgeStdDev", "jointTraceLength", "jointTraceStdDev", "jointSpacing",
            "jointSpacingStdDev", "jointStartX", "jointStartY"};
    private static final String[] SUPPORT = {"boltCount", "boltSpacing", "boltLength", "boltNodes",
            "boltArea", "boltPretension", "plinePoints", "cableYmod", "cableDensity",
            "cableYield", "cableYcomp", "groutKbond", "groutSbond"};
    private static final String[] RUN = {"udecMemoryHigh", "udecMemoryLow", "udecWindowPollCount",
            "udecWindowPollMs", "udecActivateWaitSec", "debugMode", "debugScale"};
    private static final String[] TITLE = {"step1Title", "step2Title"};

    static String text() {
        StringBuilder sb = new StringBuilder();
        sb.append("# AutoUDEC 默认参数\n");
        sb.append("# 修改后重启程序生效；删除本文件则恢复程序内置默认值。\n");
        sb.append("# startupProject：可选，填 .audec 工程路径（相对路径按本文件目录解析），\n");
        sb.append("#   用于整体替换岩层、锚杆、测线与阶段4动作序列。\n\n");
        sb.append("startupProject=\n\n");
        writeGroup(sb, "路径与输出", STARTUP);
        writeGroup(sb, "模型与开挖（direction: LEFT_TO_RIGHT / RIGHT_TO_LEFT）", MODEL);
        writeGroup(sb, "边界与数值", BOUNDARY);
        writeGroup(sb, "节理默认值", JOINT);
        writeGroup(sb, "锚杆、测线与材料（长度 m；模量/强度 Pa；密度 kg/m3）", SUPPORT);
        writeGroup(sb, "UDEC 调用与调试", RUN);
        writeGroup(sb, "图标题", TITLE);

        Set<String> written = new LinkedHashSet<>(Arrays.asList(STARTUP));
        written.addAll(Arrays.asList(MODEL));
        written.addAll(Arrays.asList(BOUNDARY));
        written.addAll(Arrays.asList(JOINT));
        written.addAll(Arrays.asList(SUPPORT));
        written.addAll(Arrays.asList(RUN));
        written.addAll(Arrays.asList(TITLE));
        List<Field> rest = new ArrayList<>();
        for (Field field : fields()) {
            if (!written.contains(field.getName())) rest.add(field);
        }
        if (!rest.isEmpty()) {
            sb.append("# ---- 其他 ----\n");
            for (Field field : rest) {
                sb.append(field.getName()).append('=').append(value(field)).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static void writeGroup(StringBuilder sb, String title, String[] keys) {
        sb.append("# ---- ").append(title).append(" ----\n");
        for (String key : keys) {
            Field field = field(key);
            if (field != null) sb.append(key).append('=').append(value(field)).append('\n');
        }
        sb.append('\n');
    }

    private static List<Field> fields() {
        List<Field> list = new ArrayList<>();
        for (Field field : ParameterList.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (SKIP.contains(field.getName())) continue;
            Class<?> type = field.getType();
            if (type == String.class || type == boolean.class || type == int.class
                    || type == double.class || type == Util.Direction.class) {
                list.add(field);
            }
        }
        list.sort((a, b) -> a.getName().compareTo(b.getName()));
        return list;
    }

    private static Field field(String name) {
        try {
            Field field = ParameterList.class.getField(name);
            return Modifier.isStatic(field.getModifiers()) ? field : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String value(Field field) {
        try {
            Object value = field.get(null);
            return value == null ? "" : String.valueOf(value);
        } catch (Exception e) {
            return "";
        }
    }
}
