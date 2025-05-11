import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Chess {
    private Timer player1Timer;
    private Timer player2Timer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}

class MainMenu extends JFrame {
    private JButton pvpButton, pvcButton, colorButton, pieceButton;
    private String[] boardColors = { "Classic", "Blue", "Green", "Red", "Purple" };
    private String[] pieceStyles = { "Standard", "Modern", "Minimalist" };
    private String selectedColor = "Classic";
    private String selectedStyle = "Standard";

    public MainMenu() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        pvpButton = new JButton("Player vs Player");
        pvcButton = new JButton("Player vs Computer");
        colorButton = new JButton("Change Board Color");
        pieceButton = new JButton("Change Piece Style");

        pvpButton.addActionListener(e -> openPlayerNamesDialog());
        pvcButton.addActionListener(e -> openPlayerVsComputerDialog());
        colorButton.addActionListener(e -> changeBoardColor());
        pieceButton.addActionListener(e -> changePieceStyle());

        panel.add(pvpButton);
        panel.add(pvcButton);
        panel.add(colorButton);
        panel.add(pieceButton);

        add(panel);
        setVisible(true);
    }

    private void openPlayerNamesDialog() {
        JDialog dialog = new JDialog(this, "Enter Player Names", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField player1Field = new JTextField("Player 1");
        JTextField player2Field = new JTextField("Player 2");
        JButton startButton = new JButton("Start Game");

        startButton.addActionListener(e -> {
            String player1Name = player1Field.getText().trim();
            String player2Name = player2Field.getText().trim();
            if (player1Name.isEmpty())
                player1Name = "Player 1";
            if (player2Name.isEmpty())
                player2Name = "Player 2";
            dialog.dispose();
            startGame(player1Name, player2Name, false, "Easy");
        });

        dialog.add(new JLabel("Player 1:"));
        dialog.add(player1Field);
        dialog.add(new JLabel("Player 2:"));
        dialog.add(player2Field);
        dialog.add(new JLabel(""));
        dialog.add(startButton);
        dialog.setVisible(true);
    }

    private void openPlayerVsComputerDialog() {
        JDialog dialog = new JDialog(this, "Player vs Computer Setup", true);
        dialog.setSize(300, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField playerField = new JTextField("Player");
        JComboBox<String> difficultyBox = new JComboBox<>(new String[] { "Easy", "Intermediate", "Hard" });
        JButton startButton = new JButton("Start Game");

        startButton.addActionListener(e -> {
            String playerName = playerField.getText().trim();
            if (playerName.isEmpty())
                playerName = "Player";
            String difficulty = (String) difficultyBox.getSelectedItem();
            dialog.dispose();
            startGame(playerName, "Computer", true, difficulty);
        });

        dialog.add(new JLabel("Player Name:"));
        dialog.add(playerField);
        dialog.add(new JLabel("Difficulty:"));
        dialog.add(difficultyBox);
        dialog.add(new JLabel(""));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel(""));
        dialog.add(startButton);
        dialog.setVisible(true);
    }

    private void changeBoardColor() {
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select Board Color:",
                "Board Color",
                JOptionPane.QUESTION_MESSAGE,
                null,
                boardColors,
                selectedColor);
        if (selected != null) {
            selectedColor = selected;
            JOptionPane.showMessageDialog(this, "Board color changed to " + selected);
        }
    }

    private void changePieceStyle() {
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select Piece Style:",
                "Piece Style",
                JOptionPane.QUESTION_MESSAGE,
                null,
                pieceStyles,
                selectedStyle);
        if (selected != null) {
            selectedStyle = selected;
            JOptionPane.showMessageDialog(this, "Piece style changed to " + selected);
        }
    }

    private void startGame(String player1Name, String player2Name, boolean vsComputer, String difficulty) {
        SwingUtilities.invokeLater(() -> {
            new ChessBoard(player1Name, player2Name, vsComputer, difficulty, selectedColor, selectedStyle);
        });
    }
}

// Chess Board GUI
class ChessBoard extends JFrame {
    private static final int BOARD_SIZE = 8;
    private JPanel boardPanel;
    private JLabel statusLabel, timerLabel1, timerLabel2;
    private JButton[][] squares = new JButton[BOARD_SIZE][BOARD_SIZE];
    private ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    private ChessPiece selectedPiece = null;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean whiteTurn = true; // White starts
    private String player1Name, player2Name;
    private boolean vsComputer;
    private String difficulty;
    private String boardColor;
    private String pieceStyle;
    private javax.swing.Timer player1Timer, player2Timer;
    private boolean gameActive = false;
    private int player1Seconds = 1200; // 20 minutes in seconds
    private int player2Seconds = 1200;

    // Chess piece images
    private Image[][] pieceImages = new Image[2][6]; // [color][piece type]

    public ChessBoard(String player1Name, String player2Name, boolean vsComputer,
            String difficulty, String boardColor, String pieceStyle) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.vsComputer = vsComputer;
        this.difficulty = difficulty;
        this.boardColor = boardColor;
        this.pieceStyle = pieceStyle;

        setTitle("Chess Game: " + player1Name + " vs " + player2Name);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 900);
        setLocationRelativeTo(null);

        loadPieceImages();
        initializeComponents();
        initializeBoard();
        setupTimers();

        setVisible(true);
        gameActive = true;

        // Start player 1's timer (white's turn)
        player1Timer.start();
    }

    private void loadPieceImages() {
        String[] pieceSymbols = { "♟", "♞", "♝", "♜", "♛", "♚" };
        for (int color = 0; color < 2; color++) {
            for (int pieceType = 0; pieceType < 6; pieceType++) {
                BufferedImage img = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, 60, 60);
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setFont(new Font("Serif", Font.BOLD, 48));
                g2d.setColor(color == 0 ? Color.WHITE : Color.BLACK);
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (60 - metrics.stringWidth(pieceSymbols[pieceType])) / 2;
                int y = ((60 - metrics.getHeight()) / 2) + metrics.getAscent();
                g2d.drawString(pieceSymbols[pieceType], x, y);
                g2d.dispose();
                pieceImages[color][pieceType] = img;
            }
        }
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Game started! " + player1Name + "'s turn (White)");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel timerPanel = new JPanel(new GridLayout(1, 2));
        timerLabel1 = new JLabel(player1Name + ": 20:00");
        timerLabel2 = new JLabel(player2Name + ": 20:00");
        timerLabel1.setHorizontalAlignment(JLabel.CENTER);
        timerLabel2.setHorizontalAlignment(JLabel.CENTER);
        timerPanel.add(timerLabel1);
        timerPanel.add(timerLabel2);
        statusPanel.add(timerPanel, BorderLayout.SOUTH);

        add(statusPanel, BorderLayout.NORTH);

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                squares[row][col] = new JButton();
                squares[row][col].setPreferredSize(new Dimension(80, 80));
                updateSquareColor(row, col);
                final int finalRow = row;
                final int finalCol = col;
                squares[row][col].addActionListener(e -> handleSquareClick(finalRow, finalCol));
                boardPanel.add(squares[row][col]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
    }

    private void updateSquareColor(int row, int col) {
        boolean isLightSquare = (row + col) % 2 == 0;
        Color lightColor, darkColor;

        switch (boardColor) {
            case "Blue":
                lightColor = new Color(187, 222, 251);
                darkColor = new Color(33, 150, 243);
                break;
            case "Green":
                lightColor = new Color(200, 230, 201);
                darkColor = new Color(76, 175, 80);
                break;
            case "Red":
                lightColor = new Color(255, 205, 210);
                darkColor = new Color(229, 115, 115);
                break;
            case "Purple":
                lightColor = new Color(225, 190, 231);
                darkColor = new Color(156, 39, 176);
                break;
            default:
                lightColor = new Color(240, 217, 181);
                darkColor = new Color(181, 136, 99);
                break;
        }

        squares[row][col].setBackground(isLightSquare ? lightColor : darkColor);
        squares[row][col].setBorderPainted(false);
        squares[row][col].setFocusPainted(false);
    }

    private void setupTimers() {
        player1Timer = new javax.swing.Timer(1000, e -> {
            player1Seconds--;
            if (player1Seconds <= 0) {
                player1Timer.stop();
                gameOver(false);
            }
            updateTimerLabel(timerLabel1, player1Name, player1Seconds);
        });

        player2Timer = new javax.swing.Timer(1000, e -> {
            player2Seconds--;
            if (player2Seconds <= 0) {
                player2Timer.stop();
                gameOver(true);
            }
            updateTimerLabel(timerLabel2, player2Name, player2Seconds);
        });
    }

    private void updateTimerLabel(JLabel label, String playerName, int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        label.setText(playerName + ": " + String.format("%02d:%02d", minutes, secs));
    }

    private void initializeBoard() {
        for (int col = 0; col < BOARD_SIZE; col++) {
            int pieceType;
            switch (col) {
                case 0:
                case 7:
                    pieceType = ChessPiece.ROOK;
                    break;
                case 1:
                case 6:
                    pieceType = ChessPiece.KNIGHT;
                    break;
                case 2:
                case 5:
                    pieceType = ChessPiece.BISHOP;
                    break;
                case 3:
                    pieceType = ChessPiece.QUEEN;
                    break;
                case 4:
                    pieceType = ChessPiece.KING;
                    break;
                default:
                    pieceType = ChessPiece.PAWN;
                    break;
            }
            board[0][col] = new ChessPiece(ChessPiece.BLACK, pieceType);
            board[7][col] = new ChessPiece(ChessPiece.WHITE, pieceType);
        }

        for (int col = 0; col < BOARD_SIZE; col++) {
            board[1][col] = new ChessPiece(ChessPiece.BLACK, ChessPiece.PAWN);
            board[6][col] = new ChessPiece(ChessPiece.WHITE, ChessPiece.PAWN);
        }

        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                board[row][col] = null;
            }
        }

        updateBoardDisplay();
    }

    private void updateBoardDisplay() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                updateSquareColor(row, col);
                if (board[row][col] != null) {
                    ChessPiece piece = board[row][col];
                    squares[row][col].setIcon(new ImageIcon(pieceImages[piece.getColor()][piece.getType()]));
                } else {
                    squares[row][col].setIcon(null);
                }
            }
        }
    }

    private void handleSquareClick(int row, int col) {
        if (!gameActive)
            return;

        if (selectedPiece == null) {
            if (board[row][col] != null && ((board[row][col].getColor() == ChessPiece.WHITE && whiteTurn) ||
                    (board[row][col].getColor() == ChessPiece.BLACK && !whiteTurn))) {
                selectedPiece = board[row][col];
                selectedRow = row;
                selectedCol = col;
                squares[row][col].setBackground(Color.YELLOW);
                statusLabel.setText("Selected piece at " + getSquareName(row, col));
            }
        } else {
            if (board[row][col] != null && board[row][col].getColor() == selectedPiece.getColor()) {
                updateSquareColor(selectedRow, selectedCol);
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
                if (row != selectedRow || col != selectedCol) {
                    selectedPiece = board[row][col];
                    selectedRow = row;
                    selectedCol = col;
                    squares[row][col].setBackground(Color.YELLOW);
                    statusLabel.setText("Selected piece at " + getSquareName(row, col));
                } else {
                    statusLabel.setText("Piece deselected");
                }
                return;
            }

            if (isValidMove(selectedRow, selectedCol, row, col)) {
                if (whiteTurn) {
                    player1Timer.stop();
                    player2Timer.start();
                } else {
                    player2Timer.stop();
                    player1Timer.start();
                }

                board[row][col] = selectedPiece;
                board[selectedRow][selectedCol] = null;
                updateSquareColor(selectedRow, selectedCol);
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;

                if (board[row][col].getType() == ChessPiece.PAWN &&
                        ((board[row][col].getColor() == ChessPiece.WHITE && row == 0) ||
                                (board[row][col].getColor() == ChessPiece.BLACK && row == 7))) {
                    promotePawn(row, col);
                }

                updateBoardDisplay();
                whiteTurn = !whiteTurn;
                statusLabel.setText((whiteTurn ? player1Name : player2Name) + "'s turn " +
                        (whiteTurn ? "(White)" : "(Black)"));

                if (isCheckmate(whiteTurn ? ChessPiece.WHITE : ChessPiece.BLACK)) {
                    gameOver(!whiteTurn);
                    return;
                }

                if (vsComputer && !whiteTurn) {
                    makeComputerMove();
                }
            } else {
                statusLabel.setText("Invalid move! Try again.");
            }
        }
    }

    private String getSquareName(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = board[fromRow][fromCol];
        if (piece == null)
            return false;
        if (board[toRow][toCol] != null && board[toRow][toCol].getColor() == piece.getColor())
            return false;

        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        int absRow = Math.abs(rowDiff), absCol = Math.abs(colDiff);

        switch (piece.getType()) {
            case ChessPiece.PAWN:
                if ((piece.getColor() == ChessPiece.WHITE && rowDiff >= 0) ||
                        (piece.getColor() == ChessPiece.BLACK && rowDiff <= 0))
                    return false;
                if (absCol == 0 && absRow == 1 && board[toRow][toCol] == null)
                    return true;
                if (absCol == 0 && absRow == 2 &&
                        ((piece.getColor() == ChessPiece.WHITE && fromRow == 6) ||
                                (piece.getColor() == ChessPiece.BLACK && fromRow == 1))) {
                    int midRow = (fromRow + toRow) / 2;
                    return board[midRow][fromCol] == null && board[toRow][toCol] == null;
                }
                return absCol == 1 && absRow == 1 && board[toRow][toCol] != null;
            case ChessPiece.KNIGHT:
                return (absRow == 2 && absCol == 1) || (absRow == 1 && absCol == 2);
            case ChessPiece.BISHOP:
                return absRow == absCol && isDiagonalPathClear(fromRow, fromCol, toRow, toCol);
            case ChessPiece.ROOK:
                return (rowDiff == 0 || colDiff == 0) && isStraightPathClear(fromRow, fromCol, toRow, toCol);
            case ChessPiece.QUEEN:
                if (absRow == absCol)
                    return isDiagonalPathClear(fromRow, fromCol, toRow, toCol);
                return (rowDiff == 0 || colDiff == 0) && isStraightPathClear(fromRow, fromCol, toRow, toCol);
            case ChessPiece.KING:
                return absRow <= 1 && absCol <= 1;
            default:
                return false;
        }
    }

    private boolean isStraightPathClear(int fr, int fc, int tr, int tc) {
        int dr = Integer.compare(tr, fr);
        int dc = Integer.compare(tc, fc);
        int r = fr + dr, c = fc + dc;
        while (r != tr || c != tc) {
            if (board[r][c] != null)
                return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    private boolean isDiagonalPathClear(int fr, int fc, int tr, int tc) {
        int dr = Integer.compare(tr, fr);
        int dc = Integer.compare(tc, fc);
        int r = fr + dr, c = fc + dc;
        while (r != tr) {
            if (board[r][c] != null)
                return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    private void promotePawn(int row, int col) {
        String[] options = { "Queen", "Rook", "Bishop", "Knight" };
        int choice = JOptionPane.showOptionDialog(this,
                "Choose a piece for pawn promotion:", "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        int pieceType;
        switch (choice) {
            case 1:
                pieceType = ChessPiece.ROOK;
                break;
            case 2:
                pieceType = ChessPiece.BISHOP;
                break;
            case 3:
                pieceType = ChessPiece.KNIGHT;
                break;
            default:
                pieceType = ChessPiece.QUEEN;
                break;
        }

        board[row][col] = new ChessPiece(board[row][col].getColor(), pieceType);
    }

    private boolean isCheckmate(int color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].getType() == ChessPiece.KING &&
                        board[r][c].getColor() == color)
                    return false;
            }
        }
        return true;
    }

    private void gameOver(boolean whiteWins) {
        gameActive = false;
        player1Timer.stop();
        player2Timer.stop();
        String winner = whiteWins ? player1Name : player2Name;
        JOptionPane.showMessageDialog(this, "Game Over! " + winner + " wins!", "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("Game Over! " + winner + " wins!");
    }

    private void makeComputerMove() {
        List<Move> validMoves = getAllValidMoves(ChessPiece.BLACK);
        if (!validMoves.isEmpty()) {
            Move selectedMove;
            switch (difficulty) {
                case "Hard":
                    selectedMove = getBestMove(validMoves);
                    break;
                case "Intermediate":
                    selectedMove = getIntermediateMove(validMoves);
                    break;
                default:
                    selectedMove = validMoves.get(new Random().nextInt(validMoves.size()));
                    break;
            }

            board[selectedMove.toRow][selectedMove.toCol] = board[selectedMove.fromRow][selectedMove.fromCol];
            board[selectedMove.fromRow][selectedMove.fromCol] = null;

            if (board[selectedMove.toRow][selectedMove.toCol].getType() == ChessPiece.PAWN && selectedMove.toRow == 7) {
                board[selectedMove.toRow][selectedMove.toCol] = new ChessPiece(ChessPiece.BLACK, ChessPiece.QUEEN);
            }

            updateBoardDisplay();
            whiteTurn = true;
            player2Timer.stop();
            player1Timer.start();
            statusLabel.setText(player1Name + "'s turn (White)");

            if (isCheckmate(ChessPiece.WHITE))
                gameOver(false);
        }

        Timer delay = new Timer(1000, e -> {
        });
        delay.setRepeats(false);
        delay.start();
    }

    private List<Move> getAllValidMoves(int color) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].getColor() == color) {
                    for (int tr = 0; tr < 8; tr++) {
                        for (int tc = 0; tc < 8; tc++) {
                            if (isValidMove(r, c, tr, tc)) {
                                moves.add(new Move(r, c, tr, tc));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private Move getBestMove(List<Move> moves) {
        return moves.isEmpty() ? null : moves.get(0);
    }

    private Move getIntermediateMove(List<Move> moves) {
        return moves.size() > 1 ? moves.get(1) : moves.get(0);
    }

    private static class Move {
        int fromRow, fromCol, toRow, toCol;

        Move(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
        }
    }
}

class ChessPiece {
    public static final int PAWN = 0, KNIGHT = 1, BISHOP = 2, ROOK = 3, QUEEN = 4, KING = 5;
    public static final int WHITE = 0, BLACK = 1;
    private final int color, type;

    public ChessPiece(int color, int type) {
        this.color = color;
        this.type = type;
    }

    public int getColor() {
        return color;
    }

    public int getType() {
        return type;
    }
}