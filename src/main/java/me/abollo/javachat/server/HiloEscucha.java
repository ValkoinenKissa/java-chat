package me.abollo.javachat.server;


import me.abollo.javachat.client.Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class HiloEscucha implements Runnable{

    private final Socket socket;
    private final Cliente cliente;

    public HiloEscucha(Socket socket, Cliente cliente) {
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
