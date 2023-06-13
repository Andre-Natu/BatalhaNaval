package com.example.batalhanaval;

import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ClientNetwork implements Runnable {
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
    private boolean running = true;
    private boolean accepted = false;
    private boolean incapazDeComunicarComOponente = false;

    // Referências para outros objetos
    private ControllerBatalhaNaval controllerBatalhaNaval;
    private BatalhaNavalCliente batalhaNavalCliente;

    public boolean iniciarNetwork(String ip, int porta) {
        this.ip = ip;
        this.port = porta;

        // Conectar ao servidor
        if (!conectar()) {
            return false;
        }

        // Iniciar a thread para a comunicação de rede
        thread = new Thread(this, "ClientNetwork");
        thread.start();
        return true;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            // Atualizar o estado da conexão
            tick();
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
                    }
                    if (mensagem.startsWith("iniciarTabuleiro")) {
                        // Iniciar o tabuleiro no controlador
                        controllerBatalhaNaval.iniciarTabuleiro();
                    }
                }

                if (entradaObjeto != null && entradaObjeto.available() > 0) {
                    System.out.println("Objeto recebido com sucesso.");

                    if (entradaObjeto.readObject() instanceof Dados) {
                        // Receber dados de navio
                        Dados dados = receberNavio();
                        batalhaNavalCliente.receberNavioOponente(dados.naviosParaColocarOponente,
                                dados.isVertical, dados.x, dados.y);
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
                errors++;
            }
        }
    }

    private boolean conectar() {
        try {
            // Conectar ao servidor
            socket = new Socket(ip, port);
            enviarObjeto = new ObjectOutputStream(socket.getOutputStream());
            entradaObjeto = new ObjectInputStream(socket.getInputStream());
            enviar = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            accepted = true;
        } catch (IOException e) {
            System.out.println("Não foi possível conectar com o servidor: " + ip + ":" + port);
            return false;
        }
        System.out.println("Você foi conectado ao servidor com sucesso.");
        return true;
    }

    public void close() {
        running = false; // Sinaliza para encerrar a execução da thread
        accepted = false;

        try {
            // Fechar os recursos de comunicação
            if (socket != null) {
                socket.close(); // Fecha o cliente Socket
            }

            if (enviarObjeto != null) {
                enviarObjeto.close(); // Fecha o ObjectOutputStream
            }

            if (entradaObjeto != null) {
                entradaObjeto.close(); // Fecha o ObjectInputStream
            }

            if (enviar != null) {
                enviar.close(); // Fecha o ObjectOutputStream
            }

            if (entrada != null) {
                entrada.close(); // Fecha o ObjectInputStream
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

    // Método para definir a instância do ControllerBatalhaNaval
    public void setControllerBatalhaNaval(ControllerBatalhaNaval controller) {
        this.controllerBatalhaNaval = controller;
    }

    public void setBatalhaNavalCliente(BatalhaNavalCliente batalhaNaval) {
        this.batalhaNavalCliente = batalhaNaval;
        System.out.println("O cliente recebeu a BatalhaNaval com sucesso");
    }
}
