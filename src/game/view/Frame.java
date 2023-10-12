package game.view;

import game.Client;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

public class Frame extends JFrame {
    public Frame(Panel panel, Client client) {
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE -> System.exit(0);
                    case KeyEvent.VK_A -> client.setMoveLeft(true);
                    case KeyEvent.VK_D -> client.setMoveRight(true);
                    case KeyEvent.VK_S -> client.setMoveBackward(true);
                    case KeyEvent.VK_W -> client.setMoveForward(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> client.setMoveLeft(false);
                    case KeyEvent.VK_D -> client.setMoveRight(false);
                    case KeyEvent.VK_S -> client.setMoveBackward(false);
                    case KeyEvent.VK_W -> client.setMoveForward(false);
                }
            }
        });
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        add(panel);
        pack();
        setVisible(true);
    }
}
