package com.example;

import net.sourceforge.tess4j.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class BotCompraVendaGUI {
    
    private static final int PRECO_X1 = 500, PRECO_Y1 = 300, PRECO_X2 = 600, PRECO_Y2 = 330;
    private static final int BOTAO_COMPRAR_X = 700, BOTAO_COMPRAR_Y = 400;
    private static final int BOTAO_VENDER_X = 750, BOTAO_VENDER_Y = 450;
    
    private static int VALOR_MAXIMO_COMPRA = 100;
    private static int VALOR_MINIMO_VENDA = 200;

    private static JTextField precoField;
    private static JTextField valorCompraField;
    private static JTextField valorVendaField;
    private static JButton startButton;
    private static JButton stopButton;
    private static boolean isRunning = false;

    public static void main(String[] args) throws AWTException, IOException, TesseractException {
        // Criar a janela GUI
        JFrame frame = new JFrame("Bot de Compra e Venda");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());
        
        // Preço detectado
        precoField = new JTextField("Preço: ");
        precoField.setEditable(false);
        precoField.setPreferredSize(new Dimension(300, 30));
        frame.add(precoField);
        
        // Valor máximo de compra
        valorCompraField = new JTextField("Valor Máximo de Compra");
        valorCompraField.setPreferredSize(new Dimension(300, 30));
        frame.add(valorCompraField);
        
        // Valor mínimo de venda
        valorVendaField = new JTextField("Valor Mínimo de Venda");
        valorVendaField.setPreferredSize(new Dimension(300, 30));
        frame.add(valorVendaField);
        
        // Botões de Start e Stop
        startButton = new JButton("Iniciar");
        stopButton = new JButton("Parar");
        stopButton.setEnabled(false);  // Inicialmente, o botão de parar é desabilitado
        
        frame.add(startButton);
        frame.add(stopButton);
        
        frame.setVisible(true);
        
        // Configuração do Tesseract
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("D:\\ts\\tessdata"); // Ajuste o caminho conforme necessário

        // Ação do botão de Iniciar
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRunning = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                
                // Lógica do Bot em um thread separado para não bloquear a interface
                new Thread(() -> {
                    try {
                        Robot robot = new Robot();
                        while (isRunning) {
                            // Captura a tela onde o preço aparece
                            BufferedImage screenShot = robot.createScreenCapture(new Rectangle(PRECO_X1, PRECO_Y1, PRECO_X2 - PRECO_X1, PRECO_Y2 - PRECO_Y1));
                            File tempFile = new File("preco.png");
                            ImageIO.write(screenShot, "png", tempFile);
                            
                            // Usa o OCR para ler o número do preço
                            String precoTexto = tesseract.doOCR(tempFile);
                            precoTexto = precoTexto.replaceAll("[^0-9]", "");  // Filtra para pegar só os números
                            
                            // Se não encontrar número, ignora e tenta de novo
                            if (precoTexto.isEmpty()) {
                                continue;
                            }

                            int preco = Integer.parseInt(precoTexto);
                            precoField.setText("Preço: " + preco);
                            
                            // Obter os valores de compra e venda
                            try {
                                VALOR_MAXIMO_COMPRA = Integer.parseInt(valorCompraField.getText());
                                VALOR_MINIMO_VENDA = Integer.parseInt(valorVendaField.getText());
                            } catch (NumberFormatException ex) {
                                // Se o valor não for válido, manter os valores padrões
                            }

                            // Decide se compra ou vende
                            if (preco <= VALOR_MAXIMO_COMPRA) {
                                robot.mouseMove(BOTAO_COMPRAR_X, BOTAO_COMPRAR_Y);
                                robot.mousePress(InputEvent.BUTTON1_MASK);
                                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                            } else if (preco >= VALOR_MINIMO_VENDA) {
                                robot.mouseMove(BOTAO_VENDER_X, BOTAO_VENDER_Y);
                                robot.mousePress(InputEvent.BUTTON1_MASK);
                                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                            }

                            // Pausa para o próximo loop
                            Thread.sleep(1000);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
        
        // Ação do botão de Parar
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRunning = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }
}
