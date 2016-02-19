package com.defritza.control;

import com.defritza.model.Board;
import com.defritza.util.Move;

/**
 * @author Joseph, Chris
 */
public interface Playable {

  /**
   * Method executes any necessary setup for Playable.
   */
  public void start();

  /**
   * Method returns the next move to be played for this playable object.
   *
   * @param game  The game object this Playable is playing.
   * @param board The board for the game this Playable is playing.
   * @return The next move that should be executed on behalf of this Playable.
   */
  public Move getMove(Chess game, Board board);

  /**
   * Method updates a Playable of the most recent move executed on behalf of its opponent.
   *
   * @param move The move that was executed.
   * @return Whether or not the update operation succeeded.
   */
  public boolean updatePlayer(Move move);

  /**
   * Method alerts a Playable that the game has ended so that any necessary cleanup can be performed.
   */
  public void finish();

}
