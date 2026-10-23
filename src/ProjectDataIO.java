import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class ProjectDataIO {
    static void write(DataOutputStream out) throws IOException {
        out.writeInt(ParameterList.layers.size());
        for (LayerData l : ParameterList.layers) {
            out.writeUTF(l.name);
            out.writeDouble(l.thickness);
            out.writeDouble(l.E);
            out.writeDouble(l.poisson);
            out.writeDouble(l.density);
            out.writeDouble(l.friction);
            out.writeDouble(l.cohesion);
            out.writeDouble(l.tension);
            out.writeDouble(l.jkn);
            out.writeDouble(l.jks);
            out.writeDouble(l.jfric);
            out.writeDouble(l.jcoh);
            out.writeDouble(l.jten);
            out.writeDouble(l.meshSize);
            out.writeInt(l.color.getRGB());
            out.writeInt(l.savedColor == null ? -1 : l.savedColor.getRGB());
            out.writeInt(l.joints.size());
            for (JointSet j : l.joints) writeJoint(out, j);
        }

        out.writeInt(ParameterList.excavationLayers.size());
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            out.writeInt(e.layerIndex);
            out.writeUTF(e.name);
            out.writeUTF(e.preset);
            out.writeDouble(e.cutWidth);
            out.writeDouble(e.overallHeight);
            out.writeInt(e.steps);
            out.writeDouble(e.stepDist);
            out.writeUTF(e.direction.name());
            out.writeBoolean(e.centered);
            out.writeDouble(e.originX);
            out.writeDouble(e.originY);
            out.writeDouble(e.x1);
            out.writeDouble(e.y1);
            out.writeDouble(e.x2);
            out.writeDouble(e.y2);
            out.writeBoolean(e.autoGeometry);
        }

        writePlots(out, ParameterList.plotPresets3);
        writePlots(out, ParameterList.plotPresets1);
        writePlots(out, ParameterList.plotPresets2);

        out.writeInt(ParameterList.boundaries.size());
        for (BoundaryCondition b : ParameterList.boundaries) {
            out.writeUTF(b.type.name());
            out.writeDouble(b.value);
            out.writeDouble(b.sx);
            out.writeDouble(b.sy);
            out.writeDouble(b.ex);
            out.writeDouble(b.ey);
            out.writeDouble(b.width);
            out.writeDouble(b.sxx);
            out.writeDouble(b.sxy);
            out.writeDouble(b.syy);
            out.writeUTF(b.name == null ? "" : b.name);
            out.writeUTF(b.edge.name());
        }
        ProjectStage4IO.write(out);
    }

    static void read(DataInputStream in, int version) throws IOException {
        ParameterList.layers.clear();
        int layerCount = in.readInt();
        for (int i = 0; i < layerCount; i++) {
            LayerData l = new LayerData(in.readUTF(), in.readDouble(), in.readDouble(),
                    in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(),
                    in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(),
                    in.readDouble(), in.readDouble(), in.readDouble(), new Color(in.readInt()));
            int saved = in.readInt();
            l.savedColor = saved == -1 ? null : new Color(saved);
            l.joints.clear();
            int jointCount = in.readInt();
            for (int k = 0; k < jointCount; k++) l.joints.add(readJoint(in));
            ParameterList.layers.add(l);
        }

        ParameterList.excavationLayers.clear();
        int excavationCount = in.readInt();
        for (int i = 0; i < excavationCount; i++) {
            ExcavationLayer e = new ExcavationLayer(in.readInt(), in.readUTF());
            e.preset = in.readUTF();
            e.cutWidth = in.readDouble();
            e.overallHeight = in.readDouble();
            e.steps = in.readInt();
            e.stepDist = in.readDouble();
            e.direction = Util.Direction.valueOf(in.readUTF());
            e.centered = in.readBoolean();
            e.originX = in.readDouble();
            e.originY = in.readDouble();
            if (version >= 3) {
                e.x1 = in.readDouble();
                e.y1 = in.readDouble();
                e.x2 = in.readDouble();
                e.y2 = in.readDouble();
                e.autoGeometry = in.readBoolean();
            } else {
                applyLegacyGeometry(e);
            }
            ParameterList.excavationLayers.add(e);
        }

        readPlots(in, ParameterList.plotPresets3, version);
        if (version >= 2) {
            readPlots(in, ParameterList.plotPresets1, version);
            readPlots(in, ParameterList.plotPresets2, version);
            ParameterList.boundaries.clear();
            int boundaryCount = in.readInt();
            for (int i = 0; i < boundaryCount; i++) {
                BoundaryCondition b = new BoundaryCondition();
                b.type = BoundaryCondition.Type.valueOf(in.readUTF());
                b.value = in.readDouble();
                b.sx = in.readDouble();
                b.sy = in.readDouble();
                b.ex = in.readDouble();
                b.ey = in.readDouble();
                b.width = in.readDouble();
                if (version >= 3) {
                    b.sxx = in.readDouble();
                    b.sxy = in.readDouble();
                    b.syy = in.readDouble();
                }
                b.name = in.readUTF();
                if (version >= 3) {
                    b.edge = BoundaryCondition.Edge.valueOf(in.readUTF());
                }
                ParameterList.boundaries.add(b);
            }
        }
        normalizePlotOrder(ParameterList.plotPresets3);
        normalizePlotOrder(ParameterList.plotPresets1);
        normalizePlotOrder(ParameterList.plotPresets2);
        if (version >= 6) ProjectStage4IO.read(in, version);
        markExcavationColors();
    }

    static void writePlots(DataOutputStream out, java.util.List<PlotPreset> presets)
            throws IOException {
        out.writeInt(presets.size());
        for (PlotPreset p : presets) {
            out.writeUTF(p.label);
            out.writeUTF(p.plotCommand);
            out.writeBoolean(p.selected);
            out.writeUTF(p.title);
            out.writeUTF(p.fileName);
            out.writeInt(p.order);
            out.writeBoolean(p.console);
        }
    }

    private static void applyLegacyGeometry(ExcavationLayer e) {
        e.autoGeometry = false;
        if (e.layerIndex < 0 || e.layerIndex >= ParameterList.layers.size()) return;
        double bottom = 0;
        for (int k = ParameterList.layers.size() - 1; k > e.layerIndex; k--) {
            bottom += ParameterList.layers.get(k).thickness;
        }
        double height = e.overallHeight > 0
                ? e.overallHeight
                : ParameterList.layers.get(e.layerIndex).thickness;
        double total = e.cutWidth + e.steps * e.stepDist;
        double left;
        if (e.centered) {
            left = (ParameterList.modelLength - total) / 2.0;
        } else if (Double.isNaN(e.originX)) {
            left = e.direction == Util.Direction.LEFT_TO_RIGHT
                    ? 0.0 : ParameterList.modelLength - total;
        } else {
            left = e.originX;
        }
        double y = (e.centered || Double.isNaN(e.originY)) ? bottom : e.originY;
        e.x1 = left;
        e.x2 = left + total;
        e.y1 = y;
        e.y2 = y + height;
    }

    static void readPlots(DataInputStream in, java.util.List<PlotPreset> presets,
                                  int version) throws IOException {
        presets.clear();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            PlotPreset preset = new PlotPreset(in.readUTF(), in.readUTF(),
                    in.readBoolean(), in.readUTF(), in.readUTF());
            if (version >= 4) preset.order = in.readInt();
            if (version >= 5) preset.console = in.readBoolean();
            preset.histCommand = histCommandFor(preset.label);
            if (preset.histCommand != null) preset.plotCommand = "plot hist";
            presets.add(preset);
        }
    }

    private static String histCommandFor(String label) {
        if ("最大不平衡力".equals(label)) return "hist unbal";
        if ("最大速度".equals(label)) return "hist vmax";
        if ("运算速率".equals(label)) return "hist solve_ratio";
        return null;
    }

    static void normalizePlotOrder(java.util.List<PlotPreset> presets) {
        boolean missing = false;
        for (PlotPreset preset : presets) {
            if (preset.selected && preset.order <= 0) {
                missing = true;
                break;
            }
        }
        if (!missing) return;
        int order = 0;
        for (PlotPreset preset : presets) {
            if (preset.selected) preset.order = ++order;
        }
    }

    private static void writeJoint(DataOutputStream out, JointSet j) throws IOException {
        out.writeDouble(j.angle);
        out.writeDouble(j.angleStdDev);
        out.writeDouble(j.bridgeLength);
        out.writeDouble(j.bridgeStdDev);
        out.writeDouble(j.traceLength);
        out.writeDouble(j.traceStdDev);
        out.writeDouble(j.spacing);
        out.writeDouble(j.spacingStdDev);
        out.writeDouble(j.startX);
        out.writeDouble(j.startY);
        out.writeBoolean(j.linkSpacing);
        out.writeBoolean(j.baseGroup);
    }

    private static JointSet readJoint(DataInputStream in) throws IOException {
        JointSet j = new JointSet(in.readDouble());
        j.angleStdDev = in.readDouble();
        j.bridgeLength = in.readDouble();
        j.bridgeStdDev = in.readDouble();
        j.traceLength = in.readDouble();
        j.traceStdDev = in.readDouble();
        j.spacing = in.readDouble();
        j.spacingStdDev = in.readDouble();
        j.startX = in.readDouble();
        j.startY = in.readDouble();
        j.linkSpacing = in.readBoolean();
        j.baseGroup = in.readBoolean();
        return j;
    }

    private static void markExcavationColors() {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e.layerIndex < 0 || e.layerIndex >= ParameterList.layers.size()) continue;
            LayerData l = ParameterList.layers.get(e.layerIndex);
            if (l.savedColor == null) l.savedColor = l.color;
            l.color = Color.BLACK;
        }
    }
}
