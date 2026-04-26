package me.abollo.javachat.client;

import me.abollo.javachat.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HiloCliente implements Runnable {

    private final Socket socket;
    private String nombre;
    private PrintWriter clienteTarget;

    public HiloCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Streams para leer y escribir texto por el socket
        try{
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clienteTarget = new PrintWriter(socket.getOutputStream(), true);

            //Primer mensaje recibido el nombre de usuario:

            nombre = entrada.readLine();
            if (nombre == null || nombre.isEmpty()) nombre = "Anónimo";

            // Registramos el cliente en la lista del servidor:

            Server.enviarListaA(clienteTarget); //se le envia la lista al nuevo cliente
            Server.agregarCliente(nombre, clienteTarget);
            Server.broadcast("JOINED:" + nombre); // a todos: ha llegado alguien

            System.out.println("Cliente registrado: " + nombre + " correctamente");



            //Bucle principal

            // readLine() bloquea esperando un mensaje.
            // Devuelve null cuando el cliente cierra la conexión.

            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.startsWith("TO:")) {
                    String[] partes = linea.split(":", 3);
                    // partes[0] = "TO", partes[1] = destinatario, partes[2] = texto
                    String dest = partes[1];
                    String texto = partes[2];

                    //Enviar al destinatario
                    Server.enviarA(dest, "MSG:" + nombre + ": " + texto);
                    // Envía también al emisor para que vea su propio mensaje
                    Server.enviarA(nombre, "MSG:" + nombre + ": " + texto);
                }
            }

        }catch (Exception e){
            System.out.println("Conexión perdida con: " + nombre);
        } finally {
            Server.borrarCliente(nombre, clienteTarget);
            Server.broadcast("LEFT:" + nombre);
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el servidor");
            }
        }

    }
}
