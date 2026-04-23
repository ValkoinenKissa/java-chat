package me.abollo.javachat.client;

import me.abollo.javachat.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * Cada vez que un cliente conecta, el Servidor crea un HiloCliente.
 * Este hilo se encarga de:
 *   1. Pedir el nombre al cliente.
 *   2. Escuchar sus mensajes en bucle.
 *   3. Hacer broadcast de cada mensaje a todos.
 *   4. Limpiar cuando el cliente se va.
 * <p>
 *   - Implements Runnable → lo que ejecuta el Thread
 *   - BufferedReader / PrintWriter → leer y escribir texto por el socket
 *   - El bucle while lee líneas hasta que el cliente cierra la conexión (null)
 */
public class HiloCliente implements Runnable {

    private final Socket socket;
    private String nombre;
    private PrintWriter salida;

    public HiloCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Streams para leer y escribir texto por el socket
        try{
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            //Primer mensaje recibido el nombre de usuario:

            nombre = entrada.readLine();
            if (nombre == null || nombre.isEmpty()) nombre = "Anónimo";

            // Registramos el cliente en la lista del servidor:

            Server.enviarListaA(salida); //se le envia la lista al nuevo cliente
            Server.agregarCliente(nombre, salida);
            Server.broadcast("JOINED:" + nombre); // a todos: ha llegado alguien

            System.out.println("Cliente registrado: " + nombre + " correctamente");



            //Bucle principal

            // readLine() bloquea esperando un mensaje.
            // Devuelve null cuando el cliente cierra la conexión.


            String linea;
            while ((linea = entrada.readLine()) != null) {
                Server.broadcast("MSG:" + nombre + ": " + linea);
            }

        }catch (Exception e){
            System.out.println("Conexión perdida con: " + nombre);
        } finally {
            //Limpieza, siempre se ejecuta

            Server.borrarCliente(nombre, salida);
            Server.broadcast("LEFT:" + nombre);
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el servidor");
            }
        }

    }
}
