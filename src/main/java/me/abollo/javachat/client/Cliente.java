package me.abollo.javachat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import me.abollo.javachat.server.HiloEscucha;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class Cliente {

    //Componentes de la UI


    @FXML
    public TextField messageTf;

    @FXML
    public TextField userField;

    @FXML
    public TextField ipAdress;

    @FXML
    public TextArea chatTa;

    @FXML
    public ListView<String> userList;

    @FXML
    public Label statusLabel;


    //Red
    private PrintWriter salida;
    private boolean conectado = false;
    private String destinatario;

    //Impedirme hablar conmigo mismo
    private String miNombre;



    // Envía el texto del campo al servidor.
    @FXML
    private void sendButton() {
        String texto = messageTf.getText().trim();
        if (texto.isEmpty() || !conectado) return;
        if (destinatario == null) return;
        //Evitar mandarme mensajes a mi mismo
        if (destinatario.equals(miNombre)) {
            chatTa.appendText("No puedes enviarte mensajes a ti mismo.\n");
            return;
        }
        salida.println("TO:" + destinatario + ":" + texto);
        messageTf.clear();
    }


    public void reciveMessage(String linea){
        Platform.runLater(() -> chatTa.appendText(linea + "\n"));
    }

    @FXML
    public void connectButton() {
        String nombre = userField.getText().trim();
        String ip = ipAdress.getText();

        if (nombre.isEmpty() || ip.isEmpty()) {
            statusLabel.setText("Revisa usuario o IP");
            return;
        }

        try {
            Socket socket = new Socket(ip, 5000);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            // FXML del chat
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/me/abollo/javachat/chat-view.fxml"));
            Parent root = loader.load();

            // Obtener el controller que creo el loader
            Cliente cliente = loader.getController();

            // Inyectar la conexión
            cliente.startConnection(socket, salida, nombre);

            // Abre la ventana
            Stage stage = new Stage();
            stage.setTitle("Chat - " + nombre);
            stage.setScene(new Scene(root));
            stage.show();

            // Cierra la ventana de login
            ((Stage) userField.getScene().getWindow()).close();


        } catch (IOException e) {
            statusLabel.setText("No se pudo conectar: " + e.getMessage());
        }
    }

    public void startConnection(Socket socket, PrintWriter salida, String nombre) {
        this.salida = salida;
        this.conectado = true;
        this.miNombre = nombre;

        // lanzamos el hilo de escucha sobre el controller
        Thread hilo = new Thread(new HiloEscucha(socket, this));
        hilo.setDaemon(true);
        hilo.start();

        // Enviamos el nombre al servidor
        salida.println(nombre);

        //Cuando el usuario hace clic en un nombre del ListView, recogemos la acción para nuevo conectar el chat

        //observable el propio objeto que está siendo observado (la propiedad en sí)
        // oldValue el valor que estaba seleccionado antes del clic.
        //new value el nuevo usuario seleccionado
        userList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        destinatario = newValue;
                        //Limpiamos la antigua sesión de chat
                        chatTa.clear();
                    }
                });
    }

    public void addCurrentConectedUser(String user) {
        Platform.runLater(() -> userList.getItems().add(user));
    }

    public void deleteUser(String user) {
        Platform.runLater(() -> userList.getItems().remove(user));
    }


}
