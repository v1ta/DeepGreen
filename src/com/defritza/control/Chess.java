package com.defritza.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.util.Log;

import com.defritza.model.Bishop;
import com.defritza.model.Board;
import com.defritza.model.Enpassant;
import com.defritza.model.King;
import com.defritza.model.Knight;
import com.defritza.model.Pawn;
import com.defritza.model.Piece;
import com.defritza.model.Player;
import com.defritza.model.Queen;
import com.defritza.model.Rook;
import com.defritza.util.Location;
/**
 * View
 * @author Joseph, Chris
 *
 */

public class Chess implements AsciiChess, Serializable {

	private int turns = 0;
	private boolean whiteCheck = false;
	private boolean blackCheck = false;
	private boolean canMove = true;
	private boolean drawFlag = false;
	private Player playerWhite;
	private Player playerBlack;
	private static final long serialVersionUID = 1;
	
	public Chess(Player playerWhite, Player playerBlack){
		
		this.playerWhite = playerWhite;
		this.playerBlack = playerBlack;
	}
	
	public Player playerTurn(){
		
		if(turns % 2 == 0){
			return this.playerWhite;
		}
		else{
			return playerBlack;
		}
	}
	
	public void turn(){
		turns++;
		
	}
	
	public void undo(){
		
		turns--;
	}
	public boolean whiteCheck(){

		return whiteCheck;
	}


	public boolean blackCheck(){

		return blackCheck;
	}


	public boolean drawFlag(){

		return this.drawFlag;
	}


	public void toggleDrawFlag(){

		if(this.drawFlag){

			this.drawFlag = false;

		}else{

			this.drawFlag = true;

		}

	}


	public void play(Board game, Player playerWhite, Player playerBlack) {

		while(canMove){
			
			if(turns % 2 == 0){ 
				
				System.out.print("White's move: ");
				
			}else{
				
				System.out.print("Black's move: ");
				
			}

			if(turns % 2 == 0){
				
				if(movePiece(game.getPiece(new Location(-1,-1)), game, new Location(-1,-1), playerWhite)){
					
					turns++;
					
				}else{
					
					System.out.println("Illegal move, try again");
					continue;
					
				}
				
				
			}else{
				
				if(movePiece(game.getPiece(new Location(-1,-1)), game, new Location(-1,-1), playerBlack)){
					
					turns++;
					
				}else{
					
					System.out.println("Illegal move, try again");
					continue;
					
				}
				
			}
			
			calculateMoves(game);
				
				if(blackCheck()){
					if(checkMate(game, playerBlack)){
						
						System.out.println("Checkmate\n\nWhite wins\n");
						break;
						
					}
					
					System.out.println("Check");
					
				}else if(whiteCheck()){
					
					if(checkMate(game, playerWhite)){
						
						System.out.println("Checkmate\n\nBlack wins\n");
						break;
						
					}
					
					System.out.println("Check\n");
					
				}else{
					
					if(turns % 2 == 0){
						
						if(stalemate(game, playerWhite)){
							
							System.out.println("\nStalemate\n\nDraw");
							break;
							
						}
					}else{
						
						if(stalemate(game, playerBlack)){
							
							System.out.println("\nStalemate\n\nDraw");
							break;
							
						}
					}
				}
				
				
			
		
				if(drawFlag()){
				
					toggleDrawFlag();
				
				}

			}
		

		}


	public void calculateMoves(Board board) {

		whiteCheck = false;
		blackCheck = false;
		Piece[][] pieces= board.getBoard();
		int iOff = 0;
		int jOff = 0;

		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){

				if(pieces[i][j] == null){

					continue;
				}

				Piece piece = pieces[i][j];

				if(piece instanceof Enpassant){
					if(piece.getTurns() == 1){

						board.nukeCell(piece.getPos());

					}else{

						piece.incrementTurn();
					}

					continue;

				}

				piece.resetValidMoves();

				int[][] vectors = piece.getMoveSet();

				for(int index = 0; index < vectors.length; index++){//iterate through moveset unit vectors

					int moveDist = piece.getMoves();

					if(piece instanceof Pawn){
						if(piece.atStart()){

							moveDist++;

						}
					}

					iOff = piece.getPos().getI();
					jOff = piece.getPos().getJ();

					for(int offset = 0; offset < moveDist; offset++){ //iterate max length of moves;

						iOff += vectors[index][0];
						jOff += vectors[index][1];

						if((iOff >= 0 && iOff <= 7) && (jOff >= 0 && jOff <= 7)){

							if(board.isEmpty(iOff, jOff)){

								piece.addValidMove(new Location(iOff,jOff));

							}else if(!(piece.getOwner().equals(board.getPiece(new Location(iOff, jOff)).getOwner()))){
								if(piece instanceof Pawn){

									break;

								}

								piece.addValidMove(new Location(iOff,jOff));

								if(board.getPiece(new Location(iOff, jOff)) instanceof King && !(piece instanceof King)){
									if(board.getPiece(new Location(iOff, jOff)).getOwner().equals("White")){

										whiteCheck = true;

									}else{

										blackCheck = true;

									}
								}

								break;

							}else{

								break; //friendly piece, break;

							}
						}
					}
				}


				if(piece instanceof Pawn){

					int [][] sVectors = piece.getSMoveSet();
					int sMoveDist = piece.getMoves();

					for(int index = 0; index < sVectors.length; index++){

						iOff = piece.getPos().getI();
						jOff = piece.getPos().getJ();

						for(int offset = 0; offset < sMoveDist; offset++){

							iOff += sVectors[index][0];
							jOff += sVectors[index][1];

							if((iOff >= 0 && iOff <= 7) && (jOff >= 0 && jOff <= 7)){

								if(!board.isEmpty(iOff, jOff) || board.isEnpassant(iOff, jOff)){
									if(!(piece.getOwner().equals(board.getPiece(new Location(iOff, jOff)).getOwner()))){

										piece.addValidMove(new Location(iOff,jOff));//enemy piece, break (need to add checking mate checking)

										if(board.getPiece(new Location(iOff, jOff)) instanceof King){
											if(board.getPiece(new Location(iOff, jOff)).getOwner().equals("White")){

												whiteCheck = true;

											}else{

												blackCheck = true;

											}
										}
										break;
									}else{

										//friendly piece, break;
										break;
									}
								}
							}
						}
					}
				}

				if(piece.atStart()){
					if(piece instanceof King){

						int [][] sVectors = piece.getSMoveSet();
						int sMoveDist = piece.getMoves() + 1;

						for(int index = 0; index < sVectors.length; index++){

							iOff = piece.getPos().getI();
							jOff = piece.getPos().getJ();

							for(int offset = 0; offset < sMoveDist; offset++){


								iOff += sVectors[index][0];
								jOff += sVectors[index][1];


								if((iOff >= 0 && iOff <= 7) && (jOff >= 0 && jOff <= 6)){

									if(board.isEmpty(iOff, jOff)){

										if(offset == 1){

											//right
											if(!board.isEmpty(iOff, jOff + sVectors[index][1])){


												if((piece.getOwner().equals(board.getPiece(new Location(iOff, jOff + sVectors[index][1])).getOwner())) && (board.getPiece(new Location(iOff, jOff + sVectors[index][1])) instanceof Rook) && (board.getPiece(new Location(iOff, jOff + sVectors[index][1])).atStart())){

													piece.addValidMove(new Location(iOff,jOff));

												}
											}

											//left
											if((iOff >= 0 && iOff <= 7) && ((jOff + 2*sVectors[index][1])	 >= 0 && (jOff + 2*sVectors[index][1])	 <= 7))
												if(!board.isEmpty(iOff, jOff + 2*sVectors[index][1])){


													if((piece.getOwner().equals(board.getPiece(new Location(iOff, jOff + 2*sVectors[index][1])).getOwner())) && (board.getPiece(new Location(iOff, jOff + 2*sVectors[index][1])) instanceof Rook) && (board.getPiece(new Location(iOff, jOff + 2*sVectors[index][1])).atStart()) && (board.isEmpty(iOff, jOff + sVectors[index][1]))) {

														piece.addValidMove(new Location(iOff,jOff));
														break;

													}
												}
										}
									}

								}else{

									break;

								}
							}
						}
					}
				}
			}
		}

		return;
	}


	public boolean movePiece(Piece piece, Board board, Location toMove, Player player) {


		if(piece == null){

			return false;

		}else if(!(piece.getOwner().equals(player.toString()))){

			return false;

		}

		Board tempB = new Board(board);
		Piece temp = null;

		if(piece instanceof Pawn){

			temp = new Pawn(piece);

		}else if(piece instanceof Rook){

			temp = new Rook(piece);

		}else if(piece instanceof Knight){

			temp = new Knight(piece);

		}else if(piece instanceof Bishop){

			temp = new Bishop(piece);

		}else if(piece instanceof Queen){

			temp = new Queen(piece);

		}else{

			temp = new King(piece);

		}

		boolean validMoveFlag = false;
		ArrayList<Location> validMoves = piece.getValidMoves();


		if(validMoves == null){
			return false; //piece has no valid moves

		}

		for(Location validMove : validMoves){
			
			if(toMove.equals(validMove)){

				validMoveFlag = true;
				break;
			}
		}

		if(!validMoveFlag){
			

			return false;
		}
		
		if(tempB != null && temp != null){
			tempB.updateBoard(temp, toMove);

			calculateMoves(tempB);

			if(player.toString().equals("White") && whiteCheck){
				

				return false;

			}else if(blackCheck && player.toString().equals("Black")){
				

				return false;
			}
		}
		
		//check for enemy piece
		if(!board.isEmpty(toMove.getI(), toMove.getJ()) || board.isEnpassant(toMove.getI(), toMove.getJ())){	

			if(board.getPiece(toMove) instanceof Enpassant && piece instanceof Pawn){

				Location temp1 = board.getPiece(toMove).getGhost();
				player.capturePiece(board.getPiece(temp1));
				board.getPiece(temp1).kill();
				board.updateBoard(piece, toMove);
				board.nukeCell(temp1);
				
				return true;

			}else{
				Log.d("IN MOVE PIECE", "PIECE: "+ board.getPiece(toMove) + " LOCATION: " + toMove);
				player.capturePiece(board.getPiece(toMove));
				board.getPiece(toMove).kill();
				board.updateBoard(piece, toMove);

				return true;

			}

		}else{

			board.updateBoard(piece, toMove);

			return true;

		}
	}

	public boolean checkMate(Board board, Player player) {

		String target = "";

		if(player.toString().equals("White")){

			target = "wK";

		}else{

			target = "bK";

		}

		Piece kingInCheck = null;

		//find king in check;
		outerloop:
			for(int i = 0; i < 8; i++){
				for(int j = 0; j < 8; j++){

					kingInCheck = board.getPiece(new Location(i,j));

					if(kingInCheck != null){
						if(kingInCheck.toString().equals(target)){

							break outerloop;

						}
					}
				}
			}

		outerloop2://cycle all enemy pieces 
			for(int i = 0; i < 8; i++){//check if available king moves won't put the king in check
				for(int j = 0; j < 8; j++){

					Piece piece = board.getPiece(new Location(i,j));
					if(piece == null){

						continue;

					}else if(piece.getOwner().equals(kingInCheck.getOwner())){
						//if piece is identical, +1 on loop
						continue;

					}

					ArrayList<Location> kingMoves = kingInCheck.getValidMoves();
					ArrayList<Location> pieceMoves = piece.getValidMoves();

					if(pieceMoves.size() == 0){
						//if no valid moves on enemy piece, +1 on loop
						continue;

					}

					if(kingMoves.size() == 0){

						break outerloop2;

					}

					outerloop3:
						for(int x = 0; x < kingMoves.size(); x++){
							for(int z = 0; z < pieceMoves.size(); z++){

								if((pieceMoves.get(z).getI() == kingMoves.get(x).getI()) && (pieceMoves.get(z).getJ() == kingMoves.get(x).getJ())){

									kingInCheck.delMove(kingMoves.get(x));

									if(x >= kingMoves.size()){

										break outerloop3;

									}
								}
							}
						}
				}
			}

			for(int i = 0; i < 8; i++){//check is friendly pieces can remove check
				for(int j = 0; j < 8; j++){

					Piece piece = board.getPiece(new Location(i,j));

					if(piece == null || piece instanceof Enpassant){

						continue;

					}else if(!(piece.getOwner().equals(kingInCheck.getOwner()))){

						continue;

					}

					ArrayList<Location> moves = piece.getValidMoves();

					if(moves.size() == 0){

						continue;

					}

					for( int e = 0; e < moves.size(); e++){

						Piece temp = null;

						if(piece instanceof Pawn){

							temp = new Pawn(piece);

						}else if(piece instanceof Rook){

							temp = new Rook(piece);

						}else if(piece instanceof Knight){

							temp = new Knight(piece);

						}else if(piece instanceof Bishop){

							temp = new Bishop(piece);

						}else if(piece instanceof Queen){

							temp = new Queen(piece);

						}else{

							temp = new King(piece);

						}

						Board tempBoard = new Board(board);
						tempBoard.updateBoard(temp, moves.get(e));
						calculateMoves(tempBoard);


						if(player.toString().equals("White")){

							if(!whiteCheck){

								return false;

							}

						}else{

							if(!blackCheck){

								return false;
							}
						}
					}

					Piece testKing = new King(kingInCheck);
					ArrayList<Location> kingMoves2 = testKing.getValidMoves();

					for(int r = 0; r < kingMoves2.size(); r++){

						Board tempBoard = new Board(board);
						
						boolean canMove = false;
						try {
							canMove = movePiece(testKing, tempBoard, kingMoves2.get(r), player);
						} catch (NullPointerException e) {
							// Not proud of this.
						}

						if(!canMove){

							kingInCheck.delMove(kingMoves2.get(r));

						}
					}
				}
			}

			if(kingInCheck.getValidMoves().size() == 0){

				return true;

			}else{

				System.out.println("king has moves");
				return false;

			}
	}


	public ArrayList<Location> parseInput(String input) {

		ArrayList<String> userCommands = new ArrayList<String>();
		ArrayList<Location> moves = new ArrayList<Location>();
		StringTokenizer tk = new StringTokenizer(input);

		for(int i = 0; tk.hasMoreTokens(); i++)
		{
			userCommands.add(tk.nextToken());

			if(userCommands.get(i).length() == 2){

				if(!Character.isLetter(userCommands.get(i).charAt(1))){
					userCommands.add(userCommands.get(i).substring(1));
					userCommands.set(i, userCommands.get(i).substring(0, 1));
					i++;

				}
			}
		}

		if(userCommands.size() == 4 || userCommands.size() == 5){

			for(int i = 0; i < 4; i += 2){

				if(!((int)userCommands.get(i).charAt(0) - 97 >= 0 && (int)userCommands.get(i).charAt(0) - 97 < 8)){

					System.out.println("Error: please enter a valid board location, i.e. \"e5\"");
					return null;

				}

				moves.add(new Location(8 - Integer.parseInt(userCommands.get(i+1)), ((int)userCommands.get(i).charAt(0) - 97)));
			}

			if(userCommands.size() == 5){

				if(!userCommands.get(4).equalsIgnoreCase("draw?")){

					System.out.println("Error: The only accepted command following board locations is \"draw?\"");
					return null;

				}else{

					moves.add(new Location(0,0));
				}

			}

			return moves;

		}else if(userCommands.size() == 1){


			if(userCommands.get(0).equals("resign")){

				moves.add(new Location(0, 0));
				return moves;

			}else if(userCommands.get(0).equals("draw")){

				moves.add(new Location(1,1));
				return moves;
			}

		}

		System.out.println("Error: Improper input, please use <piece location> <location to move> [\"draw?\"]");
		return null;

	}

	public boolean stalemate(Board board, Player player){

		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){

				Piece piece = board.getPiece(new Location(i,j));

				if(piece == null){

					continue;

				}else if(!(piece.getOwner().equals(player.toString()))){

					continue;

				}

				ArrayList<Location> moves = piece.getValidMoves();

				for(Location test : moves){

					Piece temp = null;

					if(piece instanceof Pawn){

						temp = new Pawn(piece);

					}else if(piece instanceof Rook){

						temp = new Rook(piece);

					}else if(piece instanceof Knight){

						temp = new Knight(piece);

					}else if(piece instanceof Bishop){

						temp = new Bishop(piece);

					}else if(piece instanceof Queen){

						temp = new Queen(piece);

					}else{

						temp = new King(piece);

					}

					Board tempBoard = new Board(board);		
					tempBoard.updateBoard(temp, test);			
					calculateMoves(tempBoard);

					if(player.toString().equals("White")){

						if(!whiteCheck){
							return false;
						}
					}else{

						if(!blackCheck){
							return false;
						}
					}
				}
			}
		}

		return true;

	}
}