
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JFrame;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dmitry
 */
public class Main extends JComponent implements MouseListener{
    
    final int SCR_WIDTH = 800, SCR_HEIGHT = 800;
    
    JFrame frame;

    enum CellState {
        EMPTY, PLAYER_X, PLAYER_O
    };
    
    final int SCREEN_SIDE_TILES = 7;
    final int GRID_SIDE_TILES = 4;
    final int TILE_WIDTH = SCR_WIDTH / SCREEN_SIDE_TILES, TILE_HEIGHT = SCR_HEIGHT / SCREEN_SIDE_TILES;
    // grid starts 2 right, 1 down from top left corner
    final Rectangle GRID_RECT = new Rectangle(2, 1, GRID_SIDE_TILES, GRID_SIDE_TILES);
    CellState[][] grid = new CellState[GRID_SIDE_TILES][GRID_SIDE_TILES];
    
    int movesLeft = GRID_SIDE_TILES * GRID_SIDE_TILES;
    
    Point winStartPos = new Point(), winEndPos = new Point(), lastMove = new Point(-1, -1);
    CellState winner = CellState.EMPTY;
    boolean gameOver = false;
    String endGameMessage = "";
    
    CellState currentPlayer = CellState.PLAYER_X;
    
    final Rectangle PLAY_BUTTON = new Rectangle(2, 5, 3, 1);
    
    boolean mouseClicked = false;
    Point mouseClickPoint = new Point();
    
    Font font = new Font("Comic Sans MS", Font.PLAIN, 24);
    
    public Main() {
        frame = new JFrame("Tic Tac Toe");
        this.setPreferredSize(new Dimension(SCR_WIDTH, SCR_HEIGHT));
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        this.addMouseListener(this);
        
        resetGame();
    }
    
    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, SCR_WIDTH, SCR_HEIGHT);
        
        // GRID TILES BACKGROUND
        for (int y = 0; y < GRID_SIDE_TILES; y ++) {
            for (int x = 0; x < GRID_SIDE_TILES; x ++) {
                if (lastMove.x != -1 && lastMove.x == x && lastMove.y == y) {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.fillRect((x + GRID_RECT.x) * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
            }
        }
        
        // GREEN LINE THROUGH WINNING CELLS
        if (gameOver && winner != CellState.EMPTY) {
            g.setColor(Color.GREEN);
            int xDir = (int)(Math.signum(winEndPos.x - winStartPos.x));
            int yDir = (int)(Math.signum(winEndPos.y - winStartPos.y));

            int x = winStartPos.x;            
            int y = winStartPos.y;
            while (x != winEndPos.x + xDir || y != winEndPos.y + yDir) {
                g.fillRect((x + GRID_RECT.x) * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
                x += xDir;
                y += yDir;
            }
        }
        
        // X AND O'S
        for (int y = 0; y < GRID_SIDE_TILES; y ++) {
            for (int x = 0; x < GRID_SIDE_TILES; x ++) {
                if (grid[y][x] == CellState.PLAYER_X) {
                    g.setColor(Color.RED);
                    // individual lines for the X
                    g.drawLine((x + GRID_RECT.x) * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT, ((x + GRID_RECT.x) + 1) * TILE_WIDTH, ((y + GRID_RECT.y) + 1) * TILE_HEIGHT);
                    g.drawLine((x + GRID_RECT.x) * TILE_WIDTH, ((y + GRID_RECT.y) + 1) * TILE_HEIGHT, ((x + GRID_RECT.x) + 1) * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT);
                } else if (grid[y][x] == CellState.PLAYER_O) {
                    g.setColor(Color.BLUE);
                    g.drawOval((x + GRID_RECT.x) * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
                }
            }
        }
        
        // grid lines for the board
        g.setColor(Color.BLACK);
        for (int y = 0; y <= GRID_RECT.width; y ++) {
            g.drawLine(GRID_RECT.x * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT, (GRID_RECT.x + GRID_RECT.width) * TILE_WIDTH, (y + GRID_RECT.y) * TILE_HEIGHT);
        }
        for (int x = 0; x <= GRID_RECT.height; x ++) {
            g.drawLine((x + GRID_RECT.x) * TILE_WIDTH, GRID_RECT.y * TILE_HEIGHT, (x + GRID_RECT.x) * TILE_WIDTH, (GRID_RECT.y + GRID_RECT.height) * TILE_HEIGHT);
        }
        
        if (gameOver) {
            g.setFont(font);
            g.setColor(Color.BLACK);
            g.drawString(endGameMessage, TILE_WIDTH / 2, TILE_HEIGHT / 2);
            
            g.setColor(Color.MAGENTA);
            g.fillRect(PLAY_BUTTON.x * TILE_WIDTH, PLAY_BUTTON.y * TILE_HEIGHT, PLAY_BUTTON.width * TILE_WIDTH, PLAY_BUTTON.height * TILE_HEIGHT);
            
            g.setColor(Color.BLUE);
            g.drawString("PLAY AGAIN", PLAY_BUTTON.x * TILE_WIDTH + TILE_WIDTH / 2, PLAY_BUTTON.y * TILE_HEIGHT + TILE_HEIGHT / 2);
        }
    }
    
    public void run() {
        boolean gameIsRunning = true;
        while (gameIsRunning) {
            
            repaint();
            
            if (mouseClicked) {
                processClick();
            }
        }
    }
    
    public void processClick() {

        if (!gameOver) {
            if (isValidMove()) {
                // offset the click so that it's within valid game grid coordinates
                lastMove.setLocation(mouseClickPoint.x - GRID_RECT.x, mouseClickPoint.y - GRID_RECT.y);
                grid[lastMove.y][lastMove.x] = currentPlayer;
                movesLeft --;
                testForGameOver();
                if (!gameOver) {
                    currentPlayer = (currentPlayer == CellState.PLAYER_X) ? CellState.PLAYER_O : CellState.PLAYER_X;
                }
            }
        } else {
            if (PLAY_BUTTON.contains(mouseClickPoint)) {
                resetGame();
            }
        }
        
        
        mouseClicked = false;
    }
    
    // Check if the mouse clicked within the game grid
    public boolean isValidMove() {
        if (GRID_RECT.contains(mouseClickPoint)) {
            if (grid[mouseClickPoint.y - GRID_RECT.y][mouseClickPoint.x - GRID_RECT.x] == CellState.EMPTY) {
                return true;
            }
        }
        return false;
    }
    
    /*
    Checks for a 3-in-a-row by iterating through the grid, starting on the column/row/diagonal on which the last move was made. 
    Whenever a winner is found:
     - gameOver is set to true
     - winner receives the winning player
     - winStartPos is set to the last move
     - winEndPos is set to the last cell in the 3-in-a-row
     - endGameMessage is updated to reflect which player won
    If no winner is found but the board is detected to be full:
     - gameOver is set to true
     - endGameMessage is updated to say a tie occurred
    
    */
    public void testForGameOver() {
        winner = CellState.EMPTY;
        CellState playerToMatch = grid[lastMove.y][lastMove.x];
        boolean won = false;
        // check horizontally
        winStartPos.setLocation(0, lastMove.y);
        won = true;
        for (int x = winStartPos.x; x < GRID_SIDE_TILES; x ++) {
            if (grid[lastMove.y][x] != playerToMatch) {
                won = false;
                break;
            } else {
                winEndPos.setLocation(x, lastMove.y);
            }
        }
        if (won) {
            winGame(playerToMatch);
            return;
        }
        // check vertically
        winStartPos.setLocation(lastMove.x, 0);
        won = true;
        for (int y = winStartPos.y; y < GRID_SIDE_TILES; y ++) {
            if (grid[y][lastMove.x] != playerToMatch) {
                won = false;
                break;
            } else {
                winEndPos.setLocation(lastMove.x, y);
            }
        }
        if (won) {
            winGame(playerToMatch);
            return;
        }
        // check diagonally down right
        if (lastMove.x == lastMove.y) {
            winStartPos.setLocation(0, 0);
            won = true;
            for (int x = winStartPos.x, y = winStartPos.y; y < GRID_SIDE_TILES; x ++, y ++) {
                if (grid[y][x] != playerToMatch) {
                    won = false;
                    break;
                } else {
                    winEndPos.setLocation(x, y);
                }
            }
            if (won) {
                winGame(playerToMatch);
                return;
            }
        }
        // check diagonally down left
        if ((GRID_SIDE_TILES - 1) - lastMove.x == lastMove.y) {
            winStartPos.setLocation(GRID_SIDE_TILES - 1, 0);
            won = true;
            for (int x = winStartPos.x, y = winStartPos.y; y < GRID_SIDE_TILES; x --, y ++) {
                if (grid[y][x] != playerToMatch) {
                    won = false;
                    break;
                } else {
                    winEndPos.setLocation(x, y);
                }
            }
            if (won) {
                winGame(playerToMatch);
                return;
            }
        }
        
        if (movesLeft == 0) {
            endGameMessage = "IT'S A TIE!";
            gameOver = true;
        }
    }
    
    public void winGame(CellState player) {
        winner = player;
        endGameMessage = "Player (" + (winner == CellState.PLAYER_X ? "X" : "O") + ") WINS!";
        gameOver = true;
    }
    
    public void resetGame() {
        for (int y = 0; y < GRID_SIDE_TILES; y ++) {
            for (int x = 0; x < GRID_SIDE_TILES; x ++) {
                grid[y][x] = CellState.EMPTY;
            }
        }
        
        currentPlayer = CellState.PLAYER_X;
        
        lastMove.setLocation(-1, -1);
        movesLeft = GRID_SIDE_TILES * GRID_SIDE_TILES;
        
        gameOver = false;
        winner = CellState.EMPTY;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseClickPoint.setLocation(e.getX() / TILE_WIDTH, e.getY() / TILE_HEIGHT);
        mouseClicked = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
