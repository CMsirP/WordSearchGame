import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;
/**
* A word search game engine using depth-first search and a TreeSet data structure.
* Searches through loaded lexicon for all word matches that are valid, then tabulates
* score according to a set of rules. 
* @author Chris Pinto (cmp0106@auburn.edu)
* @version 11/18/2020
*
*/

public class WordHunterX implements WordSearchGame {
   private SortedSet<String> lexicon = new TreeSet<String>();
   private String[][] board;
   private boolean[][] visited;
   private int width;
   private int height;
   private SortedSet<String> validWords = new TreeSet<String>();
   private int minLength;
   private ArrayList<Integer> path = new ArrayList<Integer>();
   private ArrayList<Integer> validPath = new ArrayList<Integer>();
   private final int MAX_NEIGHBORS = 8;
   private String currentWord;
   
   /**
   *Constructor method
   */
   public WordHunterX() {
      this.setBoard(new String[]{"E", "E", "C", "A", "A", "L", "E", "P", "H", 
                                 "N", "B", "O", "Q", "T", "T", "Y"}); //initializes to default board.
   }
   /**
    * Loads the lexicon into a data structure for later use. 
    * 
    * @param fileName A string containing the name of the file to be opened.
    * @throws IllegalArgumentException if fileName is null
    * @throws IllegalArgumentException if fileName cannot be opened.
    */
   public void loadLexicon(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException();
        }
        lexicon = new TreeSet<String>();
        try {
            
            Scanner s = 
                new Scanner(new BufferedReader(new FileReader(new File(fileName))));
            while (s.hasNext()) {
                String str = s.next();
                lexicon.add(str.toUpperCase());
                s.nextLine();
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Error loading word list: " + fileName + ": " + e);
        }
   }
   /**
    * Stores the incoming array of Strings in a data structure that will make
    * it convenient to find words.
    * 
    * @param letterArray This array of length N^2 stores the contents of the
    *     game board in row-major order. Thus, index 0 stores the contents of board
    *     position (0,0) and index length-1 stores the contents of board position
    *     (N-1,N-1). Note that the board must be square and that the strings inside
    *     may be longer than one character.
    * @throws IllegalArgumentException if letterArray is null, or is  not
    *     square.
    */
   public void setBoard(String[] letterArray) {
      if (letterArray == null) {
         throw new IllegalArgumentException("Array is null.");
      }
      double sq = Math.sqrt(letterArray.length); //check to see if letterArray is square.
      if (sq - Math.floor(sq) != 0) {
         throw new IllegalArgumentException("Array is not a square."); 
      }
      int sqInt = (int) Math.floor(sq);
      width = sqInt;
      height = sqInt; //set board width and height to square root of letterArray length. 
      board = new String[width][height];
      visited = new boolean[width][height];
      
      int i = 0;
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
               board[x][y] = letterArray[i].toUpperCase();
               i++;
         }
     }
     markAllUnvisited();
   }
   
   /**
    * Creates a String representation of the board, suitable for printing to
    *   standard out. Note that this method can always be called since
    *   implementing classes should have a default board.
    */
   public String getBoard() {
        StringBuilder sb = new StringBuilder();
        int fieldWidth = 4;
        for (int i = 0; i < fieldWidth * width; i++) {
            sb.append("*");
        }
        sb.append("\n");
        for (String[] row : board) {
            for (String s : row) {
                sb.append(String.format("%" + fieldWidth + "d", s));
            }
            sb.append("\n");
        }
        for (int i = 0; i < fieldWidth * width; i++) {
            sb.append("*");
        }
        sb.append("\n");
        return sb.toString();
    }
   /**
    * Retrieves all valid words on the game board, according to the stated game
    * rules.
    * 
    * @param minimumWordLength The minimum allowed length (i.e., number of
    *     characters) for any word found on the board.
    * @return java.util.SortedSet which contains all the words of minimum length
    *     found on the game board and in the lexicon.
    * @throws IllegalArgumentException if minimumWordLength < 1
    * @throws IllegalStateException if loadLexicon has not been called.
    */
   public SortedSet<String> getAllValidWords(int minimumWordLength) {
      if (lexicon.isEmpty()) {
         throw new IllegalStateException();
      }
      if (minimumWordLength < 1) {
         throw new IllegalArgumentException();
      }
      if (!validWords.isEmpty()) {
         validWords = new TreeSet<String>();
      }
      minLength = minimumWordLength;
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
               wordFinderStart(i, j);  
         }
      }
      return validWords;
   }
  /**
   * Computes the cummulative score for the scorable words in the given set.
   * To be scorable, a word must (1) have at least the minimum number of characters,
   * (2) be in the lexicon, and (3) be on the board. Each scorable word is
   * awarded one point for the minimum number of characters, and one point for 
   * each character beyond the minimum number.
   *
   * @param words The set of words that are to be scored.
   * @param minimumWordLength The minimum number of characters required per word
   * @return the cummulative score of all scorable words in the set
   * @throws IllegalArgumentException if minimumWordLength < 1
   * @throws IllegalStateException if loadLexicon has not been called.
   */  
   public int getScoreForWords(SortedSet<String> words, int minimumWordLength) {
      if (lexicon.isEmpty()) {
         throw new IllegalStateException();
      }
      if (minimumWordLength < 1) {
         throw new IllegalArgumentException();
      }
      int score = 0;
      for (String word : words) {
         if (word.length() == minimumWordLength) {
            score++;
         }
         else if (word.length() > minimumWordLength) {
            score += 1 + (word.length() - minimumWordLength);
         }
      }
      return score;
   }
   /**
    * Determines if the given word is in the lexicon.
    * 
    * @param wordToCheck The word to validate
    * @return true if wordToCheck appears in lexicon, false otherwise.
    * @throws IllegalArgumentException if wordToCheck is null.
    * @throws IllegalStateException if loadLexicon has not been called.
    */
   public boolean isValidWord(String wordToCheck) {
      if (lexicon.isEmpty()) {
         throw new IllegalStateException();
      }
      if (wordToCheck == null) {
         throw new IllegalArgumentException();
      }
      boolean checkValid = lexicon.contains(wordToCheck);
      return checkValid;
   }
   
   /**
    * Determines if there is at least one word in the lexicon with the 
    * given prefix.
    * 
    * @param prefixToCheck The prefix to validate
    * @return true if prefixToCheck appears in lexicon, false otherwise.
    * @throws IllegalArgumentException if prefixToCheck is null.
    * @throws IllegalStateException if loadLexicon has not been called.
    */
   public boolean isValidPrefix(String prefixToCheck) {
      if (lexicon.isEmpty()) {
         throw new IllegalStateException();
      }
      if (prefixToCheck == null) {
         throw new IllegalArgumentException();
      }
      boolean checkValid = lexicon.subSet(prefixToCheck.toUpperCase(), prefixToCheck.toUpperCase() +
                                                      Character.MAX_VALUE).size() > 0;
      return checkValid;
   }
      
   /**
    * Determines if the given word is in on the game board. If so, it returns
    * the path that makes up the word.
    * @param wordToCheck The word to validate
    * @return java.util.List containing java.lang.Integer objects with  the path
    *     that makes up the word on the game board. If word is not on the game
    *     board, return an empty list. Positions on the board are numbered from zero
    *     top to bottom, left to right (i.e., in row-major order). Thus, on an NxN
    *     board, the upper left position is numbered 0 and the lower right position
    *     is numbered N^2 - 1.
    * @throws IllegalArgumentException if wordToCheck is null.
    * @throws IllegalStateException if loadLexicon has not been called.
    */
   public List<Integer> isOnBoard(String wordToCheck) {
      if (lexicon.isEmpty()) {
         throw new IllegalStateException();
      }
      if (wordToCheck == null) {
         throw new IllegalArgumentException();
      }
      path.clear();
      validPath.clear();
      
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            if (wordToCheck.equals(board[i][j])) {
               path.add(i + (j * width));
               return path;
            }
            if (wordToCheck.startsWith(board[i][j])) {
               isOnBoardRecursiveStart(i, j, wordToCheck); 
            }
         }
      }
      return validPath;
   }
   //Private Methods//
   public void wordFinderStart(int x, int y) {
     markAllUnvisited();
     currentWord = "";
     Location start = new Location(x, y);
     if (isValid(start)) {
         wordFinderRecursive(start);
     }
   }
   /**
   *Depth-first recursive search method for getAllValidWords method.
   */
   private void wordFinderRecursive(Location location) {
      String temp = currentWord + board[location.x][location.y];
      
      if (isVisited(location) || !isValidPrefix(temp)) {
            return; 
        }
      visit(location);
      currentWord = currentWord + board[location.x][location.y];
      
      if ((isValidWord(currentWord)) && (currentWord.length() >= minLength)) {
         validWords.add(currentWord); //add word if valid
      }  
      
        for (Location neighbor : location.neighbors()) {
            wordFinderRecursive(neighbor);
            
        }
         unVisit(location);
         currentWord = currentWord.substring(0, currentWord.length() - board[location.x][location.y].length());
      }
      /**
      *Starting point of the isOnBoardRecursive search. 
      */
      public void isOnBoardRecursiveStart(int x, int y, String wordToCheck) {
        markAllUnvisited();
        currentWord = "";
        Location start = new Location(x, y);
        if (isValid(start)) {
            isOnBoardRecursive(start, wordToCheck);
        }
      }
      /**
      *Recursive DFS for isOnBoard method. 
      */
       private void isOnBoardRecursive(Location location, String wordToCheck) {
         String temp = currentWord + board[location.x][location.y];
         boolean confirmed = false;
         
         if (isVisited(location) || !wordToCheck.startsWith(temp)) {
               return; 
           }
         visit(location);
         currentWord = currentWord + board[location.x][location.y];
         path.add(location.x * width + location.y);
         
         if (wordToCheck.equals(currentWord) && (currentWord.length() == path.size())) {
            validPath = new ArrayList<Integer>(path); //add path if valid
            confirmed = true;
         }  
         
           for (Location neighbor : location.neighbors()) {
               if (confirmed) {
                  break;
               }
               isOnBoardRecursive(neighbor, wordToCheck);
               
           }
            unVisit(location);
            currentWord = currentWord.substring(0, currentWord.length() - board[location.x][location.y].length());
            path.remove(path.size() - 1);
         }
      /**
      *Utility method to clear board. 
      */
    private void markAllUnvisited() {
        visited = new boolean[width][height];
        for (boolean[] row : visited) {
            Arrays.fill(row, false);
        }
        path.clear();
    }
    /**
     * Models an (x,y) position in the grid.
     */
    private class Location {
        int x;
        int y;

        /** Constructs a Location with coordinates (x,y). */
        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /** Returns all the neighbors of this Location. */
        public Location[] neighbors() {
            Location[] nbrs = new Location[MAX_NEIGHBORS];
            int count = 0;
            Location p;
            // generate all eight neighbor locations
            // add to return value if valid
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!((i == 0) && (j == 0))) {
                        p = new Location(x + i, y + j);
                        if (isValid(p)) {
                            nbrs[count++] = p;
                        }
                    }
                }
            }
            return Arrays.copyOf(nbrs, count);
        }
    }

    /**
     * Is this location valid in the search area?
     */
    private boolean isValid(Location p) {
        return (p.x >= 0) && (p.x < width) && (p.y >= 0) && (p.y < height);
    }

    /**
     * Has this valid location been visited?
     */
    private boolean isVisited(Location p) {
        return visited[p.x][p.y];
    }

    /**
     * Mark this valid location as having been visited.
     */
    private void visit(Location p) {
        visited[p.x][p.y] = true;
    }
     private void unVisit(Location p) {
     visited[p.x][p.y] = false;
    }
}