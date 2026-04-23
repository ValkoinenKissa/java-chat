package me.abollo.javachat.server;


import me.abollo.javachat.ChatController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * <p>
 * Sin este hilo, la UI se congelaría esperando mensajes.
 * <p>
 * CONCEPTOS CLAVE:
 *   - Lee líneas del servidor en bucle infinito
 *   - Llama a cliente.recibirMensaje() → que usa Platform.runLater() para
 *     actualizar el TextArea desde el hilo correcto
 */

public class HiloEscucha implements Runnable{

    private final Socket socket;
    private final ChatController cliente;

    public HiloEscucha(Socket socket, ChatController cliente) {
        this.socket = socket;
        this.cliente = cliente;
    }

    @Override
    public void run() {

        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String linea;
            //Bucle: readline() bloquea esperando mensajes
            // Devuelve null cuando cierra la conexión

            while ((linea = entrada.readLine()) != null) {

                //filtrado de estados para ejecutar acciones distintas

                if (linea.startsWith("JOINED:")) {
                    cliente.addCurrentConectedUser(linea.substring(7));
                } else if (linea.startsWith("LEFT:")) {
                    cliente.deleteUser(linea.substring(5));
                } else {
                    cliente.reciveMessage(linea.substring(4));
                }

            }
        }catch (IOException e){

            cliente.reciveMessage("--- Desconectado del servidor ---");


        }

    }
}
