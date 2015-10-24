package com.defritza.control;

import java.util.ArrayList;

import com.defritza.model.Board;
import com.defritza.model.Piece;
import com.defritza.model.Player;
import com.defritza.util.Location;

/**
 * control for chess
 * @author Joseph, Chris
 *
 */

public interface AsciiChess {
	
	/**
	 * A board object is put in session. Method is called after the board object has been properly initialized and looped until a player wins, or a draw requested. 
	 * @param baord
	 * @return
	 */
	void play(Board baord, Player playerWhite, Player playerBlack);
	
	/**
	 * All current pieces on the board will have their allowable moves stored vectors within each piece object. 
	 * @param board
	 */
	void calculateMoves(Board board);
	
	/**
	 * Moves the piece passed by the arguments to the location provided argument. 
	 * @param piece
	 * @param board
	 * @param toMove
	 * @param player
	 * @return
	 */
	boolean movePiece(Piece piece, Board board, Location toMove, Player player);
	
	/**
	 * Returns true if the player argument is in check-mate, false otherwise.
	 * @param board
	 * @param player
	 * @return
	 */
	boolean checkMate(Board board, Player player);
	
	/**
	 * Returns true is the player argument is in stale-mate, false otherwise. 
	 * @param board
	 * @param player
	 * @return
	 */
	public boolean stalemate(Board board, Player player);
	
	/**
	 * Returns an ArrayList of tokenized input.
	 * @param input
	 * @return
	 */
	ArrayList<Location> parseInput(String input);

}