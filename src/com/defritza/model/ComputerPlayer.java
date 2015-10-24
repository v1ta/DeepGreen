package com.defritza.model;

import java.util.ArrayList;
import java.util.HashSet;

import com.defritza.control.Chess;
import com.defritza.control.Playable;
import com.defritza.util.Location;
import com.defritza.util.Move;

/**
 * Class represents a computer player, and can decide its own moves.
 * @author Chris Fretz, Joseph
 *
 */
public class ComputerPlayer extends Player implements Playable {

	private static class ScoredMove extends Move {

		public double score;
		private static final long serialVersionUID = 1;

		ScoredMove(Player owner, Location to, Location from, Piece moved, double score) {
			super(owner, to, from, moved);
			this.score = score;
		}

	}
	
	private static final long serialVersionUID = 1;

	public ComputerPlayer() {
		super("White");
	}
	
	public ComputerPlayer(String team) {
		super(team);
	}
	
	public static Move calculateMove(Player player, Chess game, Board board) {
		HashSet<Piece> myPieces = new HashSet<Piece>(), theirPieces = new HashSet<Piece>();
		String name = player.toString();
		Board copy = new Board(board);
		ArrayList<ScoredMove> considering = new ArrayList<ScoredMove>();
		for (Piece[] row : copy.getBoard()) {
			for (Piece piece : row) {
				if (piece == null) continue;
				(piece.getOwner().equals(name) ? myPieces : theirPieces).add(piece);
			}
		}

		// Loop calculates an average score for each possible move.
		for (Piece myPiece : myPieces) {
			// Get our starting position.
			Location myStart = myPiece.getPos();
			
			@SuppressWarnings("unchecked")
			// Clone is necessary to avoid ConcurrentModificationException related to recalculating moves.
			ArrayList<Location> myMoves = (ArrayList<Location>) myPiece.getValidMoves().clone();

			for (Location myMove : myMoves) {
				// Set the score for the current move to zero.
				double score = 0;
				int incrCount = 0;

				Piece myPrize = null;
				if (myPiece instanceof King && Math.abs(myMove.getJ() - myPiece.getPos().getJ()) == 2) {
					continue;
				} else {
					// Move the piece to the currently considered location and, if a piece is captured, grab a reference to it.
					myPrize = copy.getPiece(myMove);
					forceMove(myPiece, myMove, copy);

					// If we captured a piece, make sure we remove it from consideration.
					theirPieces.remove(myPrize);
				}
				myPiece.hasMoved();


				// Recalculate moves since we moved a piece.
				game.calculateMoves(copy);
				
				// Check if we can put the opponent in check or win the game on our turn and apply the necessary weights.
				if (name.equalsIgnoreCase("white") && game.blackCheck()) {
					if (game.checkMate(copy, new Player("Black"))) score = Double.MAX_VALUE;
					else score = Double.MAX_VALUE / 2;
				} else if (name.equalsIgnoreCase("black") && game.whiteCheck()) {
					if (game.checkMate(copy, new Player("White"))) score = Double.MAX_VALUE;
					else score = Double.MAX_VALUE / 2;
				}

				// Iterate across enemy moves.
				for (Piece theirPiece : theirPieces) {
					// Save the starting location of the opponent's piece.
					Location theirStart = theirPiece.getPos();
					
					@SuppressWarnings("unchecked")
					// Clone is necessary to avoid ConcurrentModificationException related to recalculating moves.
					ArrayList<Location> theirMoves = (ArrayList<Location>) theirPiece.getValidMoves().clone();

					for (Location theirMove : theirMoves) {
						Piece theirPrize = null;
						if (theirPiece instanceof King && Math.abs(theirMove.getJ() - theirPiece.getPos().getJ()) == 2) {
							continue;
						} else {
							// Move the piece to the currently considered location and, if a piece is captured, grab a reference to it.
							theirPrize = copy.getPiece(theirMove);
							forceMove(theirPiece, theirMove, copy);
						}
						theirPiece.hasMoved();

						// Iterate across every piece on the board to calculate the score for this permutation.
						for (Piece[] row : copy.getBoard()) {
							for (Piece tmp : row) {
								if (tmp == null) continue;
								boolean sameTeam = tmp.getOwner().equals(name);
								if (tmp instanceof Pawn || tmp instanceof Enpassant) {
									score += sameTeam ? 1 : -1;
								} else if (tmp instanceof Knight || tmp instanceof Bishop) {
									score += sameTeam ? 3 : -3;
								} else if (tmp instanceof Rook) {
									score += sameTeam ? 5 : -5;
								} else if (tmp instanceof Queen) {
									score += sameTeam ? 9 : -9;
								}
							}
						}
						
						// Check if the opponent can put us in check or win the game on their turn and apply necessary weights.
						game.calculateMoves(copy);
						if (name.equals("white") && game.whiteCheck()) {
							if (game.checkMate(copy, player)) score = Double.MIN_VALUE;
							else score -= Double.MAX_VALUE / 2;
						} else if (name.equals("black") && game.blackCheck()) {
							if (game.checkMate(copy, player)) score = Double.MIN_VALUE;
							else score -= Double.MAX_VALUE / 2;
						}
						
						// Revert board back to clean condition.
						forceMove(theirPiece, theirStart, copy);
						if (theirPrize != null) forceMove(theirPrize, theirPrize.getPos(), copy);

						// We're finished calculating the score for this enemy move, reset the board back to the state it was in after we made our first move.
						theirPiece.reset();
						incrCount++;
						game.calculateMoves(copy);
					}
					
				}

				// We've finished permuting for this move, reset the board back to its initial state and save the move's averaged score.
				considering.add(new ScoredMove(player, myMove, myStart, myPiece, score / incrCount));
				forceMove(myPiece, myStart, copy);
				if (myPrize != null) {
					forceMove(myPrize, myPrize.getPos(), copy);
					theirPieces.add(myPrize);
				}
				myPiece.reset();
				
				// Reset possible moves.
				game.calculateMoves(copy);
			}
		}
		
		// Loop chooses the move with the highest averaged score.
		double largest = Double.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < considering.size(); i++) {
			if (considering.get(i).score > largest) {
				largest = considering.get(i).score;
				index = i;
			}
		}

		// Execute the move.
		if (considering.size() > 0) return considering.get(index);
		else return null;
	}

	@Override
	public void start() {
		// Nothing to do...
	}
	
	@Override
	public Move getMove(Chess game, Board board) {
		return calculateMove(this, game, board);
	}
	
	@Override
	public boolean updatePlayer(Move move) {
		return true;
	}
	
	@Override
	public void finish() {
		// Nothing to do...
	}
	
	private static void forceMove(Piece piece, Location to, Board board) {
		board.getBoard()[to.getI()][to.getJ()] = piece;
		if (!to.equals(piece.getPos())) board.nukeCell(piece.getPos());
		piece.updatePos(to);
	}
	
}
