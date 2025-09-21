package ru.vsu.cs.cg.pronin_s_v.task1_primitives;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        super("Aquarium");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(900, 540);
        setLocationRelativeTo(null);

        JPanel placeholder = new JPanel();
        placeholder.setBackground(new Color(236, 230, 220));
        setContentPane(placeholder);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}