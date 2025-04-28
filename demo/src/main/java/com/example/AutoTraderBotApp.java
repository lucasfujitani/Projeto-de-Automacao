package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AutoTraderBotApp {

    private static double buyPrice;
    private static double sellPrice;
    private static boolean isBotRunning = false;
    private static Point buyButtonLocation = null;
    private static Point sellButtonLocation = null;

    // Labels para exibir as coordenadas
    private static JLabel buyCoordinatesLabel = new JLabel("Coordenada de Compra: Não selecionada");
    private static JLabel sellCoordinatesLabel = new JLabel("Coordenada de Venda: Não selecionada");

    // Instância do MouseAdapter
    private static MouseAdapter mouseListener;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Auto Trader Bot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Labels e TextFields para Preço de Compra e Venda
            JLabel buyPriceLabel = new JLabel("Preço de Compra:");
            JTextField buyPriceField = new JTextField(10);

            JLabel sellPriceLabel = new JLabel("Preço de Venda:");
            JTextField sellPriceField = new JTextField(10);

            // Botão para Iniciar/Parar o Bot
            JButton startButton = new JButton("Iniciar Bot");

            // ActionListener para o botão de iniciar/parar
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isBotRunning) {
                        startButton.setText("Iniciar Bot");
                        stopBot();
                    } else {
                        try {
                            buyPrice = Double.parseDouble(buyPriceField.getText());
                            sellPrice = Double.parseDouble(sellPriceField.getText());

                            startButton.setText("Parar Bot");
                            startBot();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Por favor, insira valores válidos para os preços.");
                        }
                    }
                    isBotRunning = !isBotRunning;
                }
            });

            // Botões para Escolher as Coordenadas (Compra e Venda)
            JButton setBuyButton = new JButton("Escolher coordenada Compra");
            JButton setSellButton = new JButton("Escolher coordenada Venda");

            // Ação para escolher coordenada de Compra
            setBuyButton.addActionListener(e -> chooseCoordinates("comprar"));

            // Ação para escolher coordenada de Venda
            setSellButton.addActionListener(e -> chooseCoordinates("vender"));

            // Layout da Interface
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(buyPriceLabel);
            panel.add(buyPriceField);
            panel.add(sellPriceLabel);
            panel.add(sellPriceField);
            panel.add(startButton);
            panel.add(setBuyButton);
            panel.add(setSellButton);
            panel.add(buyCoordinatesLabel);  // Exibir coordenada de Compra
            panel.add(sellCoordinatesLabel); // Exibir coordenada de Venda

            frame.getContentPane().add(panel);

            frame.pack();
            frame.setVisible(true);
        });
    }

    // Método para Iniciar o Bot
    public static void startBot() {
        System.out.println("Bot iniciado com Preço de Compra: " + buyPrice + " e Preço de Venda: " + sellPrice);
        if (buyButtonLocation != null && sellButtonLocation != null) {
            System.out.println("Bot usando as coordenadas de Compra: " + buyButtonLocation + " e Venda: " + sellButtonLocation);
            try {
                Robot robot = new Robot();

                // Simula o clique no botão de Compra
                robot.mouseMove(buyButtonLocation.x, buyButtonLocation.y);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(500);  // Atraso de meio segundo

                // Simula o clique no botão de Venda
                robot.mouseMove(sellButtonLocation.x, sellButtonLocation.y);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(500);  // Atraso de meio segundo

                System.out.println("Cliques simulados: Compra e Venda");

            } catch (AWTException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para Parar o Bot
    public static void stopBot() {
        System.out.println("Bot parado.");
        // Aqui você pode parar qualquer execução do bot, como captura de tela.
    }

    // Método para escolher as coordenadas de compra e venda ao clicar na tela globalmente
    private static void chooseCoordinates(String type) {
        // Display uma mensagem de instrução
        JOptionPane.showMessageDialog(null, "Clique na posição desejada na tela. O programa irá capturar a coordenada.");

        // Após fechar a mensagem, permitir captura de coordenadas
        SwingUtilities.invokeLater(() -> {
            // Agora aguardamos até que a caixa de diálogo seja fechada
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Aguardar um pequeno tempo para garantir que a caixa foi fechada
                    // Após isso, permitir captura de coordenadas
                    mouseListener = new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Point mouseLocation = e.getPoint();
                            if (type.equals("comprar") && buyButtonLocation == null) {
                                buyButtonLocation = mouseLocation;
                                buyCoordinatesLabel.setText("Coordenada de Compra: " + mouseLocation);
                                JOptionPane.showMessageDialog(null, "Localização de Compra selecionada: " + mouseLocation);
                            } else if (type.equals("vender") && sellButtonLocation == null) {
                                sellButtonLocation = mouseLocation;
                                sellCoordinatesLabel.setText("Coordenada de Venda: " + mouseLocation);
                                JOptionPane.showMessageDialog(null, "Localização de Venda selecionada: " + mouseLocation);
                            }
                        }
                    };

                    // Capturar o clique do mouse, independente de onde for
                    Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
                        if (e instanceof MouseEvent) {
                            mouseListener.mouseClicked((MouseEvent) e);
                        }
                    }, AWTEvent.MOUSE_EVENT_MASK);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }
}
