# Tuq-TicTacToe

 - Made using Java JFrames/JComponents
 - the UI is custom made, and relies on a tile-based layout, so visually it's not great, but the logic behind it makes it 
 possible to play on any screen dimensions, and any square-sized board (3x3, 4x4, ...), with minimal tweaking of code
  - of course, 4x4 and up requires some change in game-rules, which were not implemented, so while the game would function perfectly, it might not actually be fun in those sizes
  - the only visual glitch that could (should?) happen is with very small game-board sizes, since the text-size does not get adjusted
 - To check for a win, only iterate through rows/columns/diagonals which contain the player, eliminating needless checks
  - Another potential approach (haven't fully thought it out) would be to store the remaining winning moves for each player (instead of re-iterating through the board), but that can get messy very quickly as they would have to get re-calculated often. So the above approach is probably better
