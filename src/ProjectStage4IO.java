import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class ProjectStage4IO {
    static void write(DataOutputStream out) throws IOException {
        ProjectDataIO.writePlots(out, ParameterList.plotPresets4);

        out.writeInt(ParameterList.boltGroups.size());
        for (BoltGroup g : ParameterList.boltGroups) {
            out.writeUTF(g.name);
            out.writeDouble(g.x1); out.writeDouble(g.y1);
            out.writeDouble(g.x2); out.writeDouble(g.y2);
            out.writeInt(g.count);
            out.writeDouble(g.spacing);
            out.writeDouble(g.endOffset);
            out.writeDouble(g.length);
            out.writeInt(g.nodes);
            out.writeInt(g.cableMat);
            out.writeInt(g.groutMat);
            out.writeDouble(g.area);
            out.writeDouble(g.pretension);
            out.writeDouble(g.baseAngle);
            out.writeInt(g.bolts.size());
            for (Bolt b : g.bolts) {
                out.writeDouble(b.sx); out.writeDouble(b.sy);
                out.writeDouble(b.length);
                out.writeDouble(b.angleDeg);
                out.writeInt(b.nodes);
                out.writeInt(b.cableMat);
                out.writeInt(b.groutMat);
                out.writeDouble(b.area);
                out.writeDouble(b.pretension);
                out.writeBoolean(b.custom);
            }
        }

        out.writeInt(ParameterList.plines.size());
        for (Pline p : ParameterList.plines) {
            out.writeDouble(p.x1); out.writeDouble(p.y1);
            out.writeDouble(p.x2); out.writeDouble(p.y2);
            out.writeInt(p.points);
        }

        out.writeInt(ParameterList.stage4Actions.size());
        for (Stage4Action a : ParameterList.stage4Actions) {
            out.writeUTF(a.type.name());
            out.writeUTF(a.group == null ? "" : a.group);
            out.writeBoolean(a.solve);
            out.writeUTF(a.name == null ? "" : a.name);
            out.writeInt(a.cableMat);
            out.writeInt(a.groutMat);
            out.writeDouble(a.cbYmod);
            out.writeDouble(a.cbDensity);
            out.writeDouble(a.cbYield);
            out.writeDouble(a.cbYcomp);
            out.writeDouble(a.cbKbond);
            out.writeDouble(a.cbSbond);
            out.writeBoolean(a.pline != null);
            if (a.pline != null) out.writeInt(a.pline);
            out.writeUTF(a.data == null ? "" : a.data);
            out.writeInt(a.plotItems.size());
            for (PlotItem item : a.plotItems) {
                out.writeUTF(item.label);
                out.writeUTF(item.command);
                out.writeUTF(item.title);
                out.writeUTF(item.fileName);
                out.writeBoolean(item.histCommand != null);
                if (item.histCommand != null) out.writeUTF(item.histCommand);
                out.writeBoolean(item.console);
            }
        }

        out.writeInt(ParameterList.logGroups.size());
        for (LogGroup g : ParameterList.logGroups) {
            out.writeUTF(g.name);
            out.writeUTF(g.fileName == null ? "" : g.fileName);
            out.writeInt(g.items.size());
            for (LogItem item : g.items) {
                out.writeBoolean(item.pline != null);
                if (item.pline != null) out.writeInt(item.pline);
                out.writeUTF(item.data);
            }
        }
    }

    static void read(DataInputStream in, int version) throws IOException {
        ProjectDataIO.readPlots(in, ParameterList.plotPresets4, version);
        ProjectDataIO.normalizePlotOrder(ParameterList.plotPresets4);

        ParameterList.boltGroups.clear();
        int groupCount = in.readInt();
        for (int i = 0; i < groupCount; i++) {
            BoltGroup g = new BoltGroup(in.readUTF());
            g.x1 = in.readDouble(); g.y1 = in.readDouble();
            g.x2 = in.readDouble(); g.y2 = in.readDouble();
            g.count = in.readInt();
            g.spacing = in.readDouble();
            g.endOffset = in.readDouble();
            g.length = in.readDouble();
            g.nodes = in.readInt();
            g.cableMat = in.readInt();
            g.groutMat = in.readInt();
            g.area = in.readDouble();
            g.pretension = in.readDouble();
            g.baseAngle = in.readDouble();
            int boltCount = in.readInt();
            g.bolts.clear();
            for (int k = 0; k < boltCount; k++) {
                Bolt b = new Bolt();
                b.sx = in.readDouble(); b.sy = in.readDouble();
                b.length = in.readDouble();
                b.angleDeg = in.readDouble();
                b.nodes = in.readInt();
                b.cableMat = in.readInt();
                b.groutMat = in.readInt();
                b.area = in.readDouble();
                b.pretension = in.readDouble();
                b.custom = in.readBoolean();
                g.bolts.add(b);
            }
            ParameterList.boltGroups.add(g);
        }

        ParameterList.plines.clear();
        int plineCount = in.readInt();
        for (int i = 0; i < plineCount; i++) {
            Pline p = new Pline();
            p.x1 = in.readDouble(); p.y1 = in.readDouble();
            p.x2 = in.readDouble(); p.y2 = in.readDouble();
            p.points = in.readInt();
            ParameterList.plines.add(p);
        }

        ParameterList.stage4Actions.clear();
        int actionCount = in.readInt();
        for (int i = 0; i < actionCount; i++) {
            Stage4Action a = new Stage4Action(Stage4Action.Type.valueOf(in.readUTF()));
            a.group = in.readUTF();
            a.solve = in.readBoolean();
            a.name = in.readUTF();
            a.cableMat = in.readInt();
            a.groutMat = in.readInt();
            a.cbYmod = in.readDouble();
            a.cbDensity = in.readDouble();
            a.cbYield = in.readDouble();
            a.cbYcomp = in.readDouble();
            a.cbKbond = in.readDouble();
            a.cbSbond = in.readDouble();
            if (version >= 7) {
                if (in.readBoolean()) a.pline = in.readInt();
                a.data = in.readUTF();
            }
            if (version >= 9) {
                int plotCount = in.readInt();
                for (int k = 0; k < plotCount; k++) {
                    String label = in.readUTF();
                    String command = in.readUTF();
                    String title = in.readUTF();
                    String file = in.readUTF();
                    String hist = in.readBoolean() ? in.readUTF() : null;
                    PlotItem item = new PlotItem(label, command, title, file, hist);
                    item.console = in.readBoolean();
                    a.plotItems.add(item);
                }
            } else if (version >= 8) {
                int plotCount = in.readInt();
                for (int k = 0; k < plotCount; k++) {
                    PlotPreset preset = findPreset(in.readUTF());
                    if (preset != null) a.plotItems.add(PlotItem.from(preset));
                }
            }
            ParameterList.stage4Actions.add(a);
        }

        ParameterList.logGroups.clear();
        int logCount = in.readInt();
        for (int i = 0; i < logCount; i++) {
            LogGroup g = new LogGroup(in.readUTF());
            g.fileName = in.readUTF();
            int itemCount = in.readInt();
            for (int k = 0; k < itemCount; k++) {
                Integer pline = in.readBoolean() ? in.readInt() : null;
                g.items.add(new LogItem(pline, in.readUTF()));
            }
            ParameterList.logGroups.add(g);
        }
        if (version < 7) migrateLogActions();
        if (version < 9) migratePlotItems();
    }

    private static void migratePlotItems() {
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a.type != Stage4Action.Type.PLOT || !a.plotItems.isEmpty()) continue;
            for (PlotPreset p : ParameterList.plotPresets4) {
                if (p.selected) a.plotItems.add(PlotItem.from(p));
            }
        }
    }

    private static PlotPreset findPreset(String label) {
        for (PlotPreset p : ParameterList.plotPresets4) {
            if (p.label.equals(label)) return p;
        }
        return null;
    }

    private static void migrateLogActions() {
        int index = 0;
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a.type != Stage4Action.Type.LOG) continue;
            index++;
            LogGroup group = null;
            for (LogGroup g : ParameterList.logGroups) {
                if (g.name.equals(a.name)) {
                    group = g;
                    break;
                }
            }
            if (group != null && !group.items.isEmpty()) {
                LogItem item = group.items.get(0);
                a.pline = item.pline;
                a.data = item.data;
            }
            String suffix = (a.pline == null ? "" : "pline" + a.pline + "_")
                    + (a.data == null ? "data" : a.data);
            a.name = "log_" + index + "_" + suffix + ".log";
        }
    }
}
