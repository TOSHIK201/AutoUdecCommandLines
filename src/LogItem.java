public class LogItem {
    public Integer pline;
    public String data;

    public LogItem(Integer pline, String data) {
        this.pline = pline;
        this.data = data;
    }

    public String command() {
        if (pline == null) return "print " + data;
        return "print pline " + pline + " " + data;
    }

    public String label() {
        return pline == null ? data : "测线 " + pline + " · " + data;
    }
}
