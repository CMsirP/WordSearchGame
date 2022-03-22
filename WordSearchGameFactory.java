
/**
 * Provides a factory method for creating word search games. 
 *
 * @author Chris Pinto (cmp0106@auburn.edu)
 * @author Dean Hendrix (dh@auburn.edu)
 * @version 11/18/2020
 */
public class WordSearchGameFactory {

   /**
    * Returns an instance of a class that implements the WordSearchGame
    * interface.
    */
   public static WordSearchGame createGame() {
      // You must return an instance of your solution class here instead of null.
      return new WordHunterX();
   }

}
