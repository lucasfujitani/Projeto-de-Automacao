package com.example;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

public class AutoTraderBotApp extends JFrame {

    private JTextField buyPriceField, sellPriceField;
    private JTextField buyXField, buyYField, sellXField, sellYField;
    private JTextField priceAreaXField, priceAreaYField, priceAreaWidthField, priceAreaHeightField;
    private JButton startButton, stopButton;

    private volatile boolean running = false;

    public AutoTraderBotApp() {
        setTitle("AutoTrader Bot");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(12, 2, 5, 5));

        // Campos de entrada
        add(new JLabel("Preço máximo de compra:"));
        buyPriceField = new JTextField();
        add(buyPriceField);

        add(new JLabel("Preço mínimo de venda:"));
        sellPriceField = new JTextField();
        add(sellPriceField);

        add(new JLabel("X do botão Comprar:"));
        buyXField = new JTextField();
        add(buyXField);

        add(new JLabel("Y do botão Comprar:"));
        buyYField = new JTextField();
        add(buyYField);

        add(new JLabel("X do botão Vender:"));
        sellXField = new JTextField();
        add(sellXField);

        add(new JLabel("Y do botão Vender:"));
        sellYField = new JTextField();
        add(sellYField);

        add(new JLabel("Área Preço X:"));
        priceAreaXField = new JTextField();
        add(priceAreaXField);

        add(new JLabel("Área Preço Y:"));
        priceAreaYField = new JTextField();
        add(priceAreaYField);

        add(new JLabel("Área Preço Largura:"));
        priceAreaWidthField = new JTextField();
        add(priceAreaWidthField);

        add(new JLabel("Área Preço Altura:"));
        priceAreaHeightField = new JTextField();
        add(priceAreaHeightField);

        startButton = new JButton("Iniciar Bot");
        stopButton = new JButton("Parar Bot");

        add(startButton);
        add(stopButton);

        startButton.addActionListener(this::startBot);
        stopButton.addActionListener(this::stopBot);

        setVisible(true);
    }

    private void startBot(ActionEvent e) {
        running = true;
        new Thread(this::botLoop).start();
    }

    private void stopBot(ActionEvent e) {
        running = false;
    }

    private void botLoop() {
        try {
            double buyPrice = Double.parseDouble(buyPriceField.getText());
            double sellPrice = Double.parseDouble(sellPriceField.getText());
            int buyX = Integer.parseInt(buyXField.getText());
            int buyY = Integer.parseInt(buyYField.getText());
            int sellX = Integer.parseInt(sellXField.getText());
            int sellY = Integer.parseInt(sellYField.getText());
            int areaX = Integer.parseInt(priceAreaXField.getText());
            int areaY = Integer.parseInt(priceAreaYField.getText());
            int areaWidth = Integer.parseInt(priceAreaWidthField.getText());
            int areaHeight = Integer.parseInt(priceAreaHeightField.getText());

            Robot robot = new Robot();
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("src/main/resources/tessdata");
            tesseract.setLanguage("eng");

            Rectangle captureArea = new Rectangle(areaX, areaY, areaWidth, areaHeight);
            Point buyButton = new Point(buyX, buyY);
            Point sellButton = new Point(sellX, sellY);

            while (running) {
                BufferedImage image = robot.createScreenCapture(captureArea);
                String text = tesseract.doOCR(image);
                text = text.replaceAll("[^0-9.,]", "").replace(",", ".").trim();

                if (!text.isEmpty()) {
                    double price = Double.parseDouble(text);
                    System.out.println("Preço atual: " + price);

                    if (price <= buyPrice) {
                        System.out.println("Comprando...");
                        click(robot, buyButton);
                    } else if (price >= sellPrice) {
                        System.out.println("Vendendo...");
                        click(robot, sellButton);
                    }
                } else {
                    System.out.println("Não foi possível ler o preço.");
                }

                Thread.sleep(3000);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void click(Robot robot, Point point) throws InterruptedException {
        robot.mouseMove(point.x, point.y);
        Thread.sleep(100);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AutoTraderBotApp::new);
    }
}
