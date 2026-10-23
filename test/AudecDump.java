import java.nio.file.Paths;

public class AudecDump {
    public static void main(String[] args) throws Exception {
        ProjectIO.load(Paths.get("C:/Users/13978/Documents/test.audec"));
        System.out.println("modelLength=" + ParameterList.modelLength);
        System.out.println("layers=" + ParameterList.layers.size());
        for (LayerData l : ParameterList.layers) {
            System.out.printf("  %s t=%.4f E=%.3f v=%.3f rho=%.3f fric=%.2f coh=%.3f ten=%.3f mesh=%.3f%n",
                    l.name, l.thickness, l.E, l.poisson, l.density, l.friction,
                    l.cohesion, l.tension, l.meshSize);
            for (JointSet j : l.joints) {
                System.out.printf("    joint a=%.2f dev=%.2f bridge=%.2f trace=%.2f spacing=%.2f base=%s%n",
                        j.angle, j.angleStdDev, j.bridgeLength, j.traceLength, j.spacing, j.baseGroup);
            }
        }
        System.out.println("excavations=" + ParameterList.excavationLayers.size());
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            System.out.printf("  %s layer=%d rect %.2f %.2f %.2f %.2f cut=%.2f step=%d dist=%.2f dir=%s%n",
                    e.name, e.layerIndex, e.x1, e.y1, e.x2, e.y2, e.cutWidth, e.steps, e.stepDist, e.direction);
        }
        System.out.println("boundaries=" + ParameterList.boundaries.size());
        for (BoundaryCondition b : ParameterList.boundaries) {
            System.out.println("  " + b.type + " " + b.label());
        }
        System.out.println("plot4=" + ParameterList.plotPresets4.size());
        for (PlotPreset p : ParameterList.plotPresets4) {
            if (p.selected) System.out.println("  selected " + p.label + " order=" + p.order
                    + " file=" + p.fileName + " cmd=" + p.plotCommand);
        }
        System.out.println("bolts=" + ParameterList.boltGroups.size());
        for (BoltGroup g : ParameterList.boltGroups) {
            System.out.printf("  %s line %.2f %.2f %.2f %.2f count=%d spacing=%.3f len=%.3f nodes=%d " +
                            "cable=%d grout=%d area=%.2e pre=% .1e end=%.3f bolts=%d%n",
                    g.name, g.x1, g.y1, g.x2, g.y2, g.count, g.spacing, g.length, g.nodes,
                    g.cableMat, g.groutMat, g.area, g.pretension, g.endOffset, g.bolts.size());
            for (int i = 0; i < g.bolts.size(); i++) {
                Bolt b = g.bolts.get(i);
                System.out.printf("    #%d start %.3f %.3f angle %.2f len %.3f custom %s%n",
                        i, b.sx, b.sy, b.angleDeg, b.length, b.custom);
            }
        }
        System.out.println("plines=" + ParameterList.plines.size());
        for (Pline p : ParameterList.plines) System.out.println("  " + p.command());
        System.out.println("actions=" + ParameterList.stage4Actions.size());
        for (Stage4Action a : ParameterList.stage4Actions) {
            System.out.println("  " + a.type + " group=" + a.group + " name=" + a.name
                    + " solve=" + a.solve + " cable=" + a.cableMat + " grout=" + a.groutMat
                    + " ymod=" + a.cbYmod + " kbond=" + a.cbKbond);
        }
        System.out.println("logs=" + ParameterList.logGroups.size());
        for (LogGroup g : ParameterList.logGroups) {
            System.out.println("  " + g.name + " file=" + g.fileName);
            for (LogItem i : g.items) System.out.println("    " + i.label());
        }
    }
}
