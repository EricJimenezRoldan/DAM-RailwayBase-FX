public class MsgPostUploadImage {
    String type = "uploadImage";
    String name = "";
    String base64 = "";

    MsgPostUploadImage (String type, String name, String base64) {
        this.type = type;
        this.name = name;
        this.base64 = base64;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
