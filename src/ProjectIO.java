import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectIO {
    private static final String MAGIC = "AUDEC12";
    private static final String MAGIC_V11 = "AUDEC11";
    private static final String MAGIC_V10 = "AUDEC10";
    private static final String MAGIC_V9 = "AUDEC9";
    private static final String MAGIC_V8 = "AUDEC8";
    private static final String MAGIC_V7 = "AUDEC7";
    private static final String MAGIC_V6 = "AUDEC6";
    private static final String MAGIC_V5 = "AUDEC5";
    private static final String MAGIC_V4 = "AUDEC4";
    private static final String MAGIC_V3 = "AUDEC3";
    private static final String MAGIC_V2 = "AUDEC2";
    private static final String MAGIC_V1 = "AUDEC1";

    public static void save(Path path) throws IOException {
        Files.write(path, toBytes());
    }

    public static byte[] toBytes() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF(MAGIC);
            writeGlobals(out);
            ProjectDataIO.write(out);
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(Path path) throws IOException {
        loadBytes(Files.readAllBytes(path));
    }

    public static void loadBytes(byte[] data) throws IOException {
        ParameterList.restoring = true;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            String magic = in.readUTF();
            int version;
            if (MAGIC.equals(magic)) version = 12;
            else if (MAGIC_V11.equals(magic)) version = 11;
            else if (MAGIC_V10.equals(magic)) version = 10;
            else if (MAGIC_V9.equals(magic)) version = 9;
            else if (MAGIC_V8.equals(magic)) version = 8;
            else if (MAGIC_V7.equals(magic)) version = 7;
            else if (MAGIC_V6.equals(magic)) version = 6;
            else if (MAGIC_V5.equals(magic)) version = 5;
            else if (MAGIC_V4.equals(magic)) version = 4;
            else if (MAGIC_V3.equals(magic)) version = 3;
            else if (MAGIC_V2.equals(magic)) version = 2;
            else if (MAGIC_V1.equals(magic)) version = 1;
            else throw new IOException("不是 .audec 工程文件");
            readGlobals(in, version);
            ProjectDataIO.read(in, version);
            if (version < 6) {
                ParameterList.ensurePlotPresets();
            }
            if (version == 1) {
                ParameterList.initDefaultBoundaries();
            }
            if (version < 6) {
                Stage4Defaults.ensure();
            }
            ParameterList.syncFromLayers();
        } finally {
            ParameterList.restoring = false;
        }
    }

    private static void writeGlobals(DataOutputStream out) throws IOException {
        out.writeUTF(ParameterList.outputPath);
        out.writeBoolean(ParameterList.s1Calls2);
        out.writeBoolean(ParameterList.s2Calls3);
        out.writeBoolean(ParameterList.s3Calls4);
        out.writeInt(ParameterList.outputWidth);
        out.writeInt(ParameterList.outputHeight);
        out.writeDouble(ParameterList.modelLength);
        out.writeInt(ParameterList.step);
        out.writeDouble(ParameterList.stepDist);
        out.writeDouble(ParameterList.cutWidth);
        out.writeUTF(ParameterList.direction.name());
        out.writeDouble(ParameterList.excavationHeight);
        out.writeBoolean(ParameterList.excavationCentered);
        out.writeDouble(ParameterList.excavationOriginX);
        out.writeDouble(ParameterList.excavationOriginY);
        out.writeUTF(ParameterList.excavationPreset);
        out.writeUTF(ParameterList.udecExePath);
        out.writeBoolean(ParameterList.autoRunAfterGenerate);
        out.writeBoolean(ParameterList.debugMode);
        out.writeDouble(ParameterList.debugScale);
        out.writeInt(ParameterList.udecMemoryHigh);
        out.writeInt(ParameterList.udecMemoryLow);
        out.writeInt(ParameterList.udecWindowPollCount);
        out.writeInt(ParameterList.udecWindowPollMs);
        out.writeInt(ParameterList.udecActivateWaitSec);
        out.writeUTF(ParameterList.step1Title);
        out.writeUTF(ParameterList.step2Title);
        out.writeInt(ParameterList.coal9Layer);
        out.writeInt(ParameterList.coal10Layer);
        out.writeDouble(ParameterList.round);
        out.writeDouble(ParameterList.ovtol);
        out.writeDouble(ParameterList.edge);
        out.writeDouble(ParameterList.boundTol);
        out.writeDouble(ParameterList.gravity);
        out.writeDouble(ParameterList.stressTop);
        out.writeDouble(ParameterList.stressGradient);
        out.writeDouble(ParameterList.stressX);
        out.writeDouble(ParameterList.stressXY);
        out.writeDouble(ParameterList.jointAngle);
        out.writeDouble(ParameterList.jointAngleStdDev);
        out.writeDouble(ParameterList.jointBridgeLength);
        out.writeDouble(ParameterList.jointBridgeStdDev);
        out.writeDouble(ParameterList.jointTraceLength);
        out.writeDouble(ParameterList.jointTraceStdDev);
        out.writeDouble(ParameterList.jointSpacing);
        out.writeDouble(ParameterList.jointSpacingStdDev);
        out.writeDouble(ParameterList.jointStartX);
        out.writeDouble(ParameterList.jointStartY);
        out.writeInt(ParameterList.supportMat);
        out.writeDouble(ParameterList.supportKn);
        out.writeDouble(ParameterList.supportSpacing);
        out.writeDouble(ParameterList.supportYcomp);
        out.writeInt(ParameterList.supportSegments);
        out.writeDouble(ParameterList.supportOffset);
        out.writeDouble(ParameterList.monitorStep);
        out.writeBoolean(ParameterList.stressEnabled);
        out.writeDouble(ParameterList.boundaryWidth);
    }

    private static void readGlobals(DataInputStream in, int version) throws IOException {
        boolean v2 = version >= 2;
        ParameterList.outputPath = in.readUTF();
        ParameterList.s1Calls2 = in.readBoolean();
        ParameterList.s2Calls3 = in.readBoolean();
        ParameterList.s3Calls4 = in.readBoolean();
        ParameterList.outputWidth = in.readInt();
        ParameterList.outputHeight = in.readInt();
        ParameterList.modelLength = in.readDouble();
        ParameterList.step = in.readInt();
        ParameterList.stepDist = in.readDouble();
        ParameterList.cutWidth = in.readDouble();
        ParameterList.direction = Util.Direction.valueOf(in.readUTF());
        ParameterList.excavationHeight = in.readDouble();
        ParameterList.excavationCentered = in.readBoolean();
        ParameterList.excavationOriginX = in.readDouble();
        ParameterList.excavationOriginY = in.readDouble();
        ParameterList.excavationPreset = in.readUTF();
        ParameterList.udecExePath = in.readUTF();
        ParameterList.autoRunAfterGenerate = in.readBoolean();
        ParameterList.debugMode = in.readBoolean();
        ParameterList.debugScale = in.readDouble();
        ParameterList.udecMemoryHigh = in.readInt();
        ParameterList.udecMemoryLow = in.readInt();
        ParameterList.udecWindowPollCount = in.readInt();
        ParameterList.udecWindowPollMs = in.readInt();
        ParameterList.udecActivateWaitSec = in.readInt();
        ParameterList.step1Title = in.readUTF();
        ParameterList.step2Title = in.readUTF();
        ParameterList.coal9Layer = in.readInt();
        ParameterList.coal10Layer = in.readInt();
        ParameterList.round = in.readDouble();
        ParameterList.ovtol = in.readDouble();
        // AUDEC12 起保存最小块体边；旧工程按 0（自动取 2×圆角）处理
        ParameterList.edge = version >= 12 ? in.readDouble() : 0;
        ParameterList.boundTol = in.readDouble();
        ParameterList.gravity = in.readDouble();
        ParameterList.stressTop = in.readDouble();
        ParameterList.stressGradient = in.readDouble();
        ParameterList.stressX = in.readDouble();
        ParameterList.stressXY = in.readDouble();
        ParameterList.jointAngle = in.readDouble();
        ParameterList.jointAngleStdDev = in.readDouble();
        ParameterList.jointBridgeLength = in.readDouble();
        ParameterList.jointBridgeStdDev = in.readDouble();
        ParameterList.jointTraceLength = in.readDouble();
        ParameterList.jointTraceStdDev = in.readDouble();
        ParameterList.jointSpacing = in.readDouble();
        ParameterList.jointSpacingStdDev = in.readDouble();
        ParameterList.jointStartX = in.readDouble();
        ParameterList.jointStartY = in.readDouble();
        ParameterList.supportMat = in.readInt();
        ParameterList.supportKn = in.readDouble();
        ParameterList.supportSpacing = in.readDouble();
        ParameterList.supportYcomp = in.readDouble();
        ParameterList.supportSegments = in.readInt();
        ParameterList.supportOffset = in.readDouble();
        ParameterList.monitorStep = in.readDouble();
        if (v2) {
            ParameterList.stressEnabled = in.readBoolean();
            ParameterList.boundaryWidth = in.readDouble();
        }
    }
}
