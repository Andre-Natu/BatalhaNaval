package com.example.batalhanaval;

import javafx.stage.Stage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNetwork implements Runnable {
    // Configurações de rede
    private String ip = "localhost";
    private int errors;
    private int port = 22222;

    // Variáveis de conexão
    private Thread thread;
    private Socket socket;
    private DataOutputStream enviar;
    private DataInputStream entrada;
    private ObjectOutputStream enviarObjeto;
    private ObjectInputStream entradaObjeto;
    private ServerSocket serverSocket;
    private boolean running = true;
    private boolean accepted = false;
    private boolean incapazDeComunicarComOponente = false;

    // Referências para outros objetos
    private ControllerBatalhaNaval controllerBatalhaNaval;
    private BatalhaNavalServidor batalhaNavalServidor;
    private String nomeUsuario;

    public boolean iniciarNetwork(String ip, int porta) {
        this.ip = ip;
        this.port = porta;

        // Inicializar o servidor
        if (!iniciarServidor()) {
            return false;
        }

        // Iniciar a thread para a comunicação de rede
        thread = new Thread(this, "ServerNetwork");
        thread.start();
        return true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            // Atualizar o estado da conexão
            tick();

            if (!accepted) {
                // Esperar por pedidos de conexão
                escutarPorPedidosDeServidor();
            }
        }
    }

    private void tick() {
        if (errors >= 10) {
            incapazDeComunicarComOponente = true;
        }

        if (!incapazDeComunicarComOponente) {
            try {
                if (entrada != null) {
                    // Receber mensagens de texto
                    String mensagem = receberMensagem();
                    System.out.println("Mensagem recebida: " + mensagem);
                    if (mensagem.startsWith("oponente é: ")) {
                        // Atualizar o controlador com a mensagem recebida
                        controllerBatalhaNaval.atualizar(mensagem);
                        enviarMensagem(nomeUsuario);
                        controllerBatalhaNaval.mostrarBotao();
                    }
                }

                if (entradaObjeto != null) {
                    System.out.println("Objeto recebido com sucesso.");

                    if (entradaObjeto.readObject() instanceof Dados) {
                        // Receber dados de navio
                        Dados dados = receberNavio();
                        batalhaNavalServidor.receberNavioOponente(dados.naviosParaColocarOponente,
                                dados.isVertical, dados.x, dados.y);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errors++;
            }
        }
    }

    private void escutarPorPedidosDeServidor() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                // Aceitar a conexão do cliente
                socket = serverSocket.accept();
                enviar = new DataOutputStream(socket.getOutputStream());
                entrada = new DataInputStream(socket.getInputStream());
                enviarObjeto = new ObjectOutputStream(socket.getOutputStream());
                entradaObjeto = new ObjectInputStream(socket.getInputStream());
                accepted = true;
                System.out.println("O cliente solicitou entrada e foi aceito.");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    protected boolean iniciarServidor() {
        try {
            // Iniciar o socket do servidor
            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        System.out.println("Você criou uma sala com sucesso.");
        return true;
    }

    public void close() {
        running = false; // Sinaliza para encerrar a execução da thread
        accepted = false;

        try {
            // Fechar os recursos de comunicação
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (socket != null) {
                socket.close();
            }

            if (enviar != null) {
                enviar.close();
            }

            if (entrada != null) {
                entrada.close();
            }

            if (enviarObjeto != null) {
                enviarObjeto.close();
            }

            if (entradaObjeto != null) {
                entradaObjeto.close();
            }
            System.out.println("A thread foi encerrada com sucesso");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void enviarMensagem(String mensagem) {
        try {
            // Enviar mensagem de texto
            enviar.writeUTF(mensagem);
            enviar.flush();
        } catch (IOException e) {
            System.out.println(e);
            errors++;
        }
    }

    public String receberMensagem() {
        try {
            if (entrada != null) {
                // Receber mensagem de texto
                return entrada.readUTF();
            } else {
                System.out.println("O objeto DataInputStream é nulo.");
                return null;
            }
        } catch (IOException e) {
            System.out.println(e);
            errors++;
            return null;
        }
    }

    public void enviarNavio(Dados dados) {
        try {
            // Enviar objeto de dados
            enviarObjeto.reset();
            enviarObjeto.writeObject(dados);
            enviarObjeto.flush();
            System.out.println("Tamanho navio:" + dados.naviosParaColocarOponente + "Posição x:"
                    + dados.x + "Posição y:" + dados.y + "É vertical:" + dados.isVertical);
        } catch (IOException e) {
            System.out.println(e);
            errors++;
        }
    }

    public Dados receberNavio() {
        try {
            System.out.println("dado recebido pela network com sucesso!");
            if (entradaObjeto != null) {
                // Receber objeto de dados
                return (Dados) entradaObjeto.readObject();
            } else {
                System.out.println("O Dado é nulo.");
                return null;
            }
        } catch (IOException e) {
            System.out.println(e);
            errors++;
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // recebe o ControllerBatalhaNaval
    public void setControllerBatalhaNaval(ControllerBatalhaNaval controller, String nomeUsuario) {
        this.controllerBatalhaNaval = controller;
        this.nomeUsuario = nomeUsuario;
    }

    public void setBatalhaNavalServidor(BatalhaNavalServidor batalhaNaval) {
        this.batalhaNavalServidor = batalhaNaval;
        System.out.println("O servidor recebeu a BatalhaNaval com sucesso");
    }
}