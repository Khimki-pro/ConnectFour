import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

enum Piece {
    Red,
    Green,
    None
}

// Создаем свой класс, расширяющий JButton и приватные поля, которые заиспользуем дальше.
class Slot extends JButton {
    private int i;
    private int j;
    private Piece piece = Piece.None;

    public Slot(int i, int j) {
        this.i = i;
        this.j = j;
        setOpaque(true); // Полная отрисовка элемента.
        setBorder(new LineBorder(Color.BLACK)); // Задаем границу элемента черного цвета.
        setBorderPainted(true); // Отображаем границу элемента.
        setColor();  // Задаем цвет элемента.
    }
    // Геттеры, сеттеры класса.
    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        setColor();
    }
    // В зависимости от выбранного цвета в енуме piece устанавливаем цвет заливки элемента.
    public void setColor() {
        switch(piece) {
            case Red:
                setBackground(Color.red);
                break;
            case Green:
                setBackground(Color.green);
                break;
            case None:
                setBackground(Color.white);
                break;
        }
    }
}

class Tree {
    private int value;
    private Slot[][] slots;
    private ArrayList<Integer> bestMoves;
    private Slot prev = null;
    private int depth;
    public static int MAX_DEPTH = 6;

    public Tree(Slot[][] slots, int depth) {
        this.slots = slots;
        this.bestMoves = new ArrayList<Integer>();
        this.depth = depth;
        this.value = getValue();

        if (depth < MAX_DEPTH && this.value < 100 && this.value > -100 ) {
            ArrayList<Integer> possibilities = new ArrayList<Integer>();

            for (int i = 0; i < 7; i++) {
                if (slots[i][0].getPiece() == Piece.None) {
                    possibilities.add(i);
                }
            }

            for (int i = 0; i < possibilities.size(); i++) {
                insertTo(slots[possibilities.get(i)][0]);
                Tree child = new Tree(slots, depth+1);
                prev.setPiece(Piece.None);

                if (i == 0) {
                    bestMoves.add(possibilities.get(i));
                    value = child.value;
                } else if (depth % 2 == 0) {
                    if (value < child.value) {
                        bestMoves.clear();
                        bestMoves.add(possibilities.get(i));
                        this.value = child.value;
                    } else if (value == child.value) {
                        bestMoves.add(possibilities.get(i));
                    }
                } else if (depth % 2 == 1) {
                    if (value > child.value) {
                        bestMoves.clear();
                        bestMoves.add(possibilities.get(i));
                        this.value = child.value;
                    } else if (value == child.value) {
                        bestMoves.add(possibilities.get(i));
                    }
                }
            }
        }
        else {
            this.value = getValue();
        }
    }

    private void printSlots() {
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                switch(slots[i][j].getPiece()) {
                    case Green: System.out.print("B"); break;
                    case Red: System.out.print("R"); break;
                    default: System.out.print("-"); break;
                }
            }
            System.out.println();
        }
    }

    private void insertTo(Slot slot) {
        if (slot.getPiece() != Piece.None)
            return;

        int i = slot.getI();
        int j = slot.getJ();

        while(j < slots[0].length-1 && slots[i][j+1].getPiece() == Piece.None)
            j++;

        if (depth % 2 == 0)
            slots[i][j].setPiece(Piece.Red);
        else
            slots[i][j].setPiece(Piece.Green);
        prev = slots[i][j];
    }

    public int getX() {
        int random = (int)(Math.random() * 100) % bestMoves.size();
        return bestMoves.get(random);
    }

    public int getValue() {
        int value = 0;
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                if (slots[i][j].getPiece() != Piece.None) {
                    if (slots[i][j].getPiece() == Piece.Red) {
                        value += possibleConnections(i, j) * (MAX_DEPTH - this.depth);
                    } else {
                        value -= possibleConnections(i, j) * (MAX_DEPTH - this.depth);
                    }
                }
            }
        }
        return value;
    }

    private int possibleConnections(int i, int j) {
        int value = 0;
        value += lineOfFour(i, j, -1, -1);
        value += lineOfFour(i, j, -1, 0);
        value += lineOfFour(i, j, -1, 1);
        value += lineOfFour(i, j, 0, -1);
        value += lineOfFour(i, j, 0, 1);
        value += lineOfFour(i, j, 1, -1);
        value += lineOfFour(i, j, 1, 0);
        value += lineOfFour(i, j, 1, 1);

        return value;
    }

    private int lineOfFour(int x, int y, int i, int j) {
        int value = 1;
        Piece color = slots[x][y].getPiece();

        for (int k = 1; k < 4; k++) {
            if (x+i*k < 0 || y+j*k < 0 || x+i*k >= slots.length || y+j*k >= slots[0].length) {
                return 0;
            }
            if (slots[x+i*k][y+j*k].getPiece() == color) {
                value++;
            } else if (slots[x+i*k][y+j*k].getPiece() != Piece.None) {
                return 0;
            } else {
                for (int l = y+j*k; l >= 0; l--) {
                    if (slots[x+i*k][l].getPiece() == Piece.None) {
                        value--;
                    }
                }
            }
        }

        if (value == 4) return 100;
        if (value < 0) return 0;
        return value;
    }
}

public class ConnectFour extends JFrame implements ActionListener {
    private JLabel lblPlayer = new JLabel("Player: ");
    private JLabel lblCurrentPlayer = new JLabel("Green");
    private JPanel pnlMenu = new JPanel();
    private JPanel pnlSlots = new JPanel();
    private JButton btnNewGame = new JButton("New Game (2 players)");
    private JButton btnNewGame2 = new JButton("New Game (Enemy: AI)");

    private Slot[][] slots = new Slot[7][6];

    private boolean winnerExists = false;
    private int currentPlayer = 1;
    private boolean AI;

    public ConnectFour(boolean AI) {
        super("Four In A Line");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        currentPlayer = (int) (Math.random()*2) + 1;

        this.AI = AI;

        btnNewGame.addActionListener(this);
        btnNewGame2.addActionListener(this);
        switch(currentPlayer) {
            case 1:
                lblCurrentPlayer.setForeground(Color.green);
                lblCurrentPlayer.setText("Green");
                break;
            case 2:
                lblCurrentPlayer.setForeground(Color.red);
                lblCurrentPlayer.setText("Red");
                break;
        }
        pnlMenu.add(btnNewGame);
        pnlMenu.add(btnNewGame2);
        pnlMenu.add(lblPlayer);
        pnlMenu.add(lblCurrentPlayer);

        pnlSlots.setLayout(new GridLayout(6, 7));

        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                slots[i][j] = new Slot(i, j);
                slots[i][j].addActionListener(this);
                pnlSlots.add(slots[i][j]);
            }
        }

        add(pnlMenu, BorderLayout.NORTH);
        add(pnlSlots, BorderLayout.CENTER);
        setSize(500, 500);
        setVisible(true);

        if (currentPlayer == 2 && AI) insertTo(minimax());
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == btnNewGame) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Confirmation", JOptionPane.YES_NO_OPTION) == 0) {
                dispose();
                new ConnectFour(false);
                return;
            }
        }
        if (ae.getSource() == btnNewGame2) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Confirmation", JOptionPane.YES_NO_OPTION) == 0) {
                dispose();
                new ConnectFour(true);
                return;
            }
        } else if (!winnerExists) {
            Slot slot = (Slot)ae.getSource();
            insertTo(slot);
        }
    }

    void insertTo(Slot slot) {
        if (slot.getPiece() != Piece.None) {
            return;
        }

        int i = slot.getI();
        int j = slot.getJ();

        while(j < slots[0].length-1 && slots[i][j+1].getPiece() == Piece.None) {
            j++;
        }

        switch(currentPlayer) {
            case 1:
                slots[i][j].setPiece(Piece.Green);
                break;
            case 2:
                slots[i][j].setPiece(Piece.Red);
                break;
        }

        currentPlayer = (currentPlayer % 2) + 1;

        if (thereIsAWinner()) {
            lblPlayer.setText("Winner: ");
            winnerExists = true;
        } else {
            switch(currentPlayer) {
                case 1:
                    lblCurrentPlayer.setForeground(Color.green);
                    lblCurrentPlayer.setText("Green");
                    break;
                case 2:
                    lblCurrentPlayer.setForeground(Color.red);
                    lblCurrentPlayer.setText("Red");
                    break;
            }

            if (currentPlayer == 2 && AI) {
                insertTo(minimax());
            }
        }
    }

    public boolean thereIsAWinner() {
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                if (slots[i][j].getPiece() != Piece.None && connectsToFour(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean connectsToFour(int i, int j) {
        if (lineOfFour(i, j, -1, -1)) return true;
        if (lineOfFour(i, j, -1, 0)) return true;
        if (lineOfFour(i, j, -1, 1)) return true;
        if (lineOfFour(i, j, 0, -1)) return true;
        if (lineOfFour(i, j, 0, 1)) return true;
        if (lineOfFour(i, j, 1, -1)) return true;
        if (lineOfFour(i, j, 1, 0)) return true;
        if (lineOfFour(i, j, 1, 1)) return true;
        return false;
    }

    public boolean lineOfFour(int x, int y, int i, int j) {
        Piece color = slots[x][y].getPiece();

        for (int k = 1; k < 4; k++) {
            if (x+i*k < 0 || y+j*k < 0 || x+i*k >= slots.length || y+j*k >= slots[0].length) {
                return false;
            }
            if (slots[x+i*k][y+j*k].getPiece() != color) {
                return false;
            }
        }
        return true;
    }

    public Slot minimax() {
        Tree tree = new Tree(slots, 0);
        return slots[tree.getX()][0];
    }

    public static void main(String[] args) {
        new ConnectFour(false);
    }
}
