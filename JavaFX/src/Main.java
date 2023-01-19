import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class Main extends Application {

    // Per comunicar-se amb el servidor a través de WebSockets
    public static UtilsWS socketClient;
    // Exemple de configuració per Railwa
    
    // public static int port = 443;
    // public static String protocol = "https";
    // public static String host = "ams-railway-production.up.railway.app";
    // public static String protocolWS = "wss";

    public static int port = 3000;
    public static String protocol = "http";
    public static String host = "localhost";
    public static String protocolWS = "ws";

    // Camps JavaFX a modificar
    public static TextField inputMissatge = new TextField();
    public static Label resultBox = new Label();
    public static Label resultBoxWS = new Label();
    public static Label labelBounce = new Label();
    public static Label labelBroadcast = new Label();
    public static Label labelWS = new Label();

    // Missatges a enviar
    public static MsgPostHerois msgPostHerois = new MsgPostHerois();
    public static MsgPost msgPostBounce = new MsgPost("bounce", "");
    public static MsgPost msgPostBroadcast = new MsgPost("broadcast", "");
    public static MsgPostUploadImage msgPostUploadImage = new MsgPostUploadImage("imageUpload", "", "");
    public static MsgWebSocket msgWebSocket = new MsgWebSocket("bounce", "");

    public static void main(String[] args) {

        // Iniciar el client de WebSockets
        socketClient = UtilsWS.getSharedInstance(protocolWS + "://" + host + ":" + port);
        socketClient.onMessage((response) -> {
            // JavaFX necessita que els canvis es facin des de el thread principal
            Platform.runLater(()->{ 
                // Fer aquí els canvis a la interficie
                MsgWebSocket msgObj = (MsgWebSocket) UtilsJSON.parse(response, MsgWebSocket.class);
                resultBoxWS.setText(msgObj.text); 
            });
        });

        // Iniciar app JavaFX   
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {

        // Definir mida finestra
        int windowHeight = 500;
        int windowWidth = 750;

        // Construir interficie
        VBox root = buildInterface(primaryStage);

        // Definir escena
        Scene  scene = new Scene(root);

        // Iniciar finestra d'app
        primaryStage.setTitle("NodeJS i JavaFX");
        primaryStage.onCloseRequestProperty();
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(true);
        primaryStage.setHeight(windowHeight);
        primaryStage.setWidth(windowWidth);
        primaryStage.setMinHeight(windowHeight);
        primaryStage.setMinWidth(windowWidth);
        primaryStage.heightProperty().addListener((observable, oldValue, newvalue) -> {
            double titleHeight = primaryStage.getHeight() - scene.getHeight();
            double rootHeight = primaryStage.getHeight() - titleHeight;
            root.setPrefHeight(rootHeight);
        });

        // Definir icona d'app
        Image icon = new Image("file:./assets/icon.png");
        primaryStage.getIcons().add(icon);
    }

    @Override
    public void stop() { 
        socketClient.close();
    }

    public VBox buildInterface(Stage primaryStage) {

        // Definir la divisió vertical
        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);

        // Add button with label that calls "/direccioURL"
        Button buttonGET = new Button("GET a /direccioURL");
        buttonGET.setOnAction(e -> {
            UtilsHTTP.sendGET(protocol + "://" + host + ":" + port + "/direccioURL", (response) -> {
                resultBox.setText(response);
            });
        });

        // Add input text 'missatge'
        HBox hboxM = new HBox(0);
        Label labelM = new Label("Missatge:");
        inputMissatge.setOnKeyReleased(e -> {
            setMissatge();
        });
        hboxM.getChildren().add(labelM);
        hboxM.getChildren().add(inputMissatge);

        // Add button that calls "h/dades" with POST of type "herois"
        HBox hboxHerois = new HBox(0);
        
        String paramsPostHerois = "{ 'type' : 'herois' }".replaceAll("'", "\"");
        Button buttonPostHerois = new Button("Enviar Herois");
        buttonPostHerois.setOnAction(e -> {
            setMissatge();
            UtilsHTTP.sendPOST(protocol + "://" + host + ":" + port + "/dades", paramsPostHerois, (response) -> {
                resultBox.setText(response);
            });
        });
        Label labelH = new Label("POST amb paràmetre:" + paramsPostHerois);
        hboxHerois.getChildren().add(buttonPostHerois);
        hboxHerois.getChildren().add(labelH);

        // Add button that calls "/dades" with POST of type "bounce"
        HBox hboxBounce = new HBox(0);
        Button buttonPOSTBounce = new Button("Enviar bounce");
        buttonPOSTBounce.setOnAction(e -> {
            setMissatge();
            UtilsHTTP.sendPOST(protocol + "://" + host + ":" + port + "/dades", UtilsJSON.stringify(msgPostBounce), (response) -> {
                resultBox.setText(response);
            });
        });
        hboxBounce.getChildren().add(buttonPOSTBounce);
        hboxBounce.getChildren().add(labelBounce);

        // Add button that calls "/dades" with POST of type "broadcast"
        HBox hboxBroadcast = new HBox(0);
        Button buttonPOSTBroadcast = new Button("Enviar broadcast");
        buttonPOSTBroadcast.setOnAction(e -> {
            setMissatge();
            UtilsHTTP.sendPOST(protocol + "://" + host + ":" + port + "/dades", UtilsJSON.stringify(msgPostBroadcast), (response) -> {
                resultBox.setText(response);
            });
        });
        hboxBroadcast.getChildren().add(buttonPOSTBroadcast);
        hboxBroadcast.getChildren().add(labelBroadcast);

        // Add button that calls WebSockets
        HBox hboxWS = new HBox(0);
        Button buttonWS = new Button("Enviar amb WebSockets");
        buttonWS.setOnAction(e -> {
            setMissatge();
            socketClient.safeSend(UtilsJSON.stringify(msgWebSocket));
        });
        labelWS = new Label("Enviar el valor del camp 'missatge' per WebSockets");
        hboxWS.getChildren().add(buttonWS);
        hboxWS.getChildren().add(labelWS);

        Text titol0 = new Text("Missatges rebuts amb GET/POST");
        Text titol1 = new Text("Missatges rebuts amb sockets");

        // Add an input image box
        HBox hboxFile = new HBox(0);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Button buttonFile = new Button("Select an Image");
        buttonFile.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            // Read image from File and convert to Base64
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                msgPostUploadImage.name = selectedFile.getName();
                msgPostUploadImage.base64 = Base64.getEncoder().encodeToString(fileContent);
                UtilsHTTP.sendPOST(protocol + "://" + host + ":" + port + "/dades", UtilsJSON.stringify(msgPostUploadImage), (response) -> {
                    resultBox.setText(response);
                }); 
            } catch (IOException e1) { e1.printStackTrace(); }
        });
        Label labelFile = new Label("Enviar una imatge per POST a la carpeta 'private'");
        hboxFile.getChildren().add(buttonFile);
        hboxFile.getChildren().add(labelFile);
        
        // Add vertical separators
        Separator vS0 = new Separator();
        vS0.setPrefHeight(25);
        Separator vS1 = new Separator();
        vS1.setPrefHeight(25);
        Separator vS2 = new Separator();
        vS2.setPrefHeight(25);

        // Draw a border arround resultBox
        resultBox.setStyle("-fx-border-color: gray;-fx-border-width: 1px;-fx-border-style: solid;-fx-border-radius: 5px;-fx-padding: 5px;");
        resultBoxWS.setStyle("-fx-border-color: gray;-fx-border-width: 1px;-fx-border-style: solid;-fx-border-radius: 5px;-fx-padding: 5px;");

        // Add elements to the vertical box (main layout)
        vbox.getChildren().add(buttonGET);
        vbox.getChildren().add(vS0);
        vbox.getChildren().add(hboxM);
        vbox.getChildren().add(hboxHerois);
        vbox.getChildren().add(hboxBounce);
        vbox.getChildren().add(hboxBroadcast);
        vbox.getChildren().add(hboxWS);
        vbox.getChildren().add(hboxFile);
        vbox.getChildren().add(vS1);
        vbox.getChildren().add(titol0);
        vbox.getChildren().add(resultBox);
        vbox.getChildren().add(vS2);
        vbox.getChildren().add(titol1);
        vbox.getChildren().add(resultBoxWS);

        setMissatge();

        return vbox;
    }

    void setMissatge () {
        String missatge = inputMissatge.getText();
        if (missatge.compareTo("")==0) {
            missatge = "Hola";
            inputMissatge.setText(missatge);
        }

        msgPostBounce.setText(missatge);
        msgPostBroadcast.setText(missatge);
        msgWebSocket.setText(missatge);

        labelBounce.setText("POST amb paràmetres:" + UtilsJSON.stringify(msgPostBounce));
        labelBroadcast.setText("POST amb paràmetres:" + UtilsJSON.stringify(msgPostBroadcast));
        labelWS.setText("Enviar '" + UtilsJSON.stringify(msgWebSocket) + "' per WebSockets");
    }
}