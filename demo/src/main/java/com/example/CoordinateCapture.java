package com.example;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class CoordinateCapture {

    public static void main(String[] args) throws AWTException {
        Robot robot = new Robot();
        
        // Captura a coordenada de onde o mouse está
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        System.out.println("Posição do mouse: " + mouseLocation);
        
        // Para clicar, mova o mouse para uma posição e clique
        robot.mouseMove(300, 500);  // Exemplo de coordenada para o botão de comprar
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }
}
