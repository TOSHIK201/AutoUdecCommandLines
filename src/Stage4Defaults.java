class Stage4Defaults {
    static void ensure() {
        if (ParameterList.boltGroups.isEmpty()) addBolts();
        if (ParameterList.plines.isEmpty()) {
            Pline p = new Pline();
            p.x1 = 0; p.y1 = 38.75; p.x2 = 238; p.y2 = 38.75;
            p.points = ParameterList.plinePoints;
            ParameterList.plines.add(p);
        }
        selectPlots();
        if (ParameterList.stage4Actions.isEmpty()) addActions();
    }

    private static void addBolts() {
        BoltGroup b1 = add("BOLT1", 100, 26.4, 108, 26.4, 8, 1.0, 2.4);
        add("BOLT2", 100, 26.4, 108, 26.4, 2, 4.0, 8.3);
        BoltGroup b3 = add("BOLT3", 100, 26.4, 100, 21.6, 5, 1.0, 2.4);
        BoltGroup b4 = add("BOLT4", 108, 26.4, 108, 21.6, 5, 1.0, 2.4);
        angle(b1, 0, 105);
        angle(b1, 7, 75);
        angle(b3, 0, -195);
        angle(b3, 4, -165);
        angle(b4, 0, 15);
        angle(b4, 4, -15);
    }

    private static BoltGroup add(String name, double x1, double y1, double x2, double y2,
                                 int count, double spacing, double length) {
        BoltGroup g = new BoltGroup(name);
        g.x1 = x1; g.y1 = y1; g.x2 = x2; g.y2 = y2;
        g.count = count;
        g.spacing = spacing;
        g.length = length;
        g.rebuild();
        ParameterList.boltGroups.add(g);
        return g;
    }

    private static void angle(BoltGroup g, int index, double angle) {
        if (g != null && index >= 0 && index < g.bolts.size()) {
            g.bolts.get(index).angleDeg = angle;
            g.bolts.get(index).custom = true;
        }
    }

    private static void selectPlots() {
        boolean any = false;
        for (PlotPreset p : ParameterList.plotPresets4) {
            if (p.selected) {
                any = true;
                break;
            }
        }
        if (any) return;
        int order = 0;
        for (PlotPreset p : ParameterList.plotPresets4) {
            if ("Y位移+块体+锚杆".equals(p.label) || "Y应力+块体+锚杆".equals(p.label)) {
                p.selected = true;
                p.order = ++order;
            }
        }
    }

    private static void addActions() {
        exc("openOffCut_9", false);
        material();
        bolt("BOLT1");
        bolt("BOLT2");
        bolt("BOLT3");
        bolt("BOLT4");
        ParameterList.stage4Actions.add(new Stage4Action(Stage4Action.Type.PLINE));
        ParameterList.stage4Actions.add(new Stage4Action(Stage4Action.Type.SOLVE));
        plot("after_openOffCut_9", "块体+锚杆");
        save("step_4");
        for (int i = 1; i <= 3; i++) exc("state_" + i + "_9", true);
        save("step_4_cut9");
        plot("after_state_3_9", "Y应力+块体+锚杆", "Y位移+块体+锚杆");
        log("after_state_3_9_pline1_ydis.log", 1, "ydis");
        exc("openOffCut_10", true);
        for (int i = 1; i <= 3; i++) exc("state_" + i + "_10", true);
        save("step_4_cut10");
        plot("after_state_3_10", "Y应力+块体+锚杆", "Y位移+块体+锚杆");
        log("after_state_3_10_pline1_ydis.log", 1, "ydis");
    }

    private static void exc(String group, boolean solve) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.EXCAVATE);
        a.group = group;
        a.solve = solve;
        ParameterList.stage4Actions.add(a);
    }

    private static void bolt(String name) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.BOLT);
        a.name = name;
        ParameterList.stage4Actions.add(a);
    }

    private static void material() {
        Stage4Action a = new Stage4Action(Stage4Action.Type.MATERIAL);
        a.cableMat = ParameterList.cableMaterial();
        a.groutMat = ParameterList.groutMaterial();
        ParameterList.stage4Actions.add(a);
    }

    private static void plot(String tag, String... labels) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.PLOT);
        a.name = tag;
        for (String label : labels) {
            PlotPreset preset = findPreset(label);
            if (preset != null) a.plotItems.add(PlotItem.from(preset));
        }
        ParameterList.stage4Actions.add(a);
    }

    private static PlotPreset findPreset(String label) {
        for (PlotPreset p : ParameterList.plotPresets4) {
            if (p.label.equals(label)) return p;
        }
        return null;
    }

    private static void save(String name) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.SAVE);
        a.name = name;
        ParameterList.stage4Actions.add(a);
    }

    private static void log(String name, int pline, String data) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.LOG);
        a.name = name;
        a.pline = pline;
        a.data = data;
        ParameterList.stage4Actions.add(a);
    }
}
