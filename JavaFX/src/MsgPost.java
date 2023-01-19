public class MsgPost {
    String type = "bounce";
    String text = "";

    MsgPost (String type, String text) {
        this.type = type;
        this.text = text;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }
}
