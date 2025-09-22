package ru.vsu.cs.cg.pronin_s_v.task1_primitives.ui;

import javax.swing.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        super("Aquarium");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(900, 740);
        setLocationRelativeTo(null);
        setContentPane(new GamePanel());

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}