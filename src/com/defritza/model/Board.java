package com.defritza.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.defritza.app.GamePlay;
import com.defritza.chess.R;
import com.defritza.chess.R.drawable;
import com.defritza.util.Location;


/**
 * Board object
 * @author Joseph, Chris
 *
 */
public class Board implements Cloneable, Serializable {

	private Piece[][] cells;
	private static final long serialVersionUID = 1;
	private RelativeLayout container;
	private int sDim;
	private OnTouchListener onTouchListener;

	public Board(Piece[][] cells){

		this.cells = cells;

	}

	public Board(Board board){
		this.cells = new Piece[8][8];

		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){

				if(board.cells[i][j] instanceof Pawn){
					this.cells[i][j] = new Pawn(board.cells[i][j]);
				}else if(board.cells[i][j] instanceof Rook){
					this.cells[i][j] = new Rook(board.cells[i][j]);
				}else if(board.cells[i][j] instanceof Knight){
					this.cells[i][j]= new Knight(board.cells[i][j]);
				}else if(board.cells[i][j] instanceof Bishop){
					this.cells[i][j] = new Bishop(board.cells[i][j]);
				}else if(board.cells[i][j] instanceof Queen){
					this.cells[i][j] = new Queen(board.cells[i][j]);
				}else if(board.cells[i][j] instanceof King){
					this.cells[i][j] = new King(board.cells[i][j]);
				}else if(board.cells[i][j] instanceof Enpassant){
					this.cells[i][j] = new Enpassant(board.cells[i][j]);
				}else{
					this.cells[i][j] = null;
				}
			}
		}
	}



	public ArrayList<Piece> getStartSetup(){

		ArrayList<Piece> pieces = new ArrayList<Piece>();

		for(int i = 0; i < 8; i++){

			for(int j = 0; j < 8; j++){

				if(this.cells[i][j] != null){

					pieces.add(this.cells[i][j]);
				}

			}
		}

		return pieces;

	}

	public void printBoard(){

		System.out.println();
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){

				if(this.isEmpty(i, j)){
					if(i % 2 == 0){
						if(j % 2 != 0){

							System.out.print("##");
						}else{

							System.out.print("  ");
						}
					}
					else{
						if(j % 2 == 0){

							System.out.print("##");
						}else{

							System.out.print("  ");
						}
					}

					System.out.print(" ");
				}else{

					System.out.print(this.cells[i][j]);

					System.out.print(" ");
				}

			}
			System.out.println(8 - i);
		}


		for(int i = 0; i < 8; i++){
			System.out.print(" " + Character.toString((char) ('a' + i))+ " ");
		}
		System.out.println("\n");

	}


	public boolean isEmpty(int i, int j){

		if(this.cells[i][j] == null || this.cells[i][j] instanceof Enpassant)
			return true;
		else
			return false;
	}


	public Piece getPiece(Location location){

		return this.cells[location.getI()][location.getJ()];
	}


	public boolean loadPlayer(Player player){

		String owner = player.toString();

		if(owner.equals("Black")){

			this.cells[0][0] = new Rook(owner, new Location(0,0));
			this.cells[0][1] = new Knight(owner, new Location(0,1));
			this.cells[0][2] = new Bishop(owner, new Location(0,2));
			this.cells[0][3] = new Queen(owner, new Location(0,3));
			this.cells[0][4] = new King(owner, new Location(0,4));
			this.cells[0][5] = new Bishop(owner, new Location(0,5));
			this.cells[0][6] = new Knight(owner, new Location(0,6));
			this.cells[0][7] = new Rook(owner, new Location(0,7));

			for(int i = 0; i < 8; i++){

				this.cells[1][i] = new Pawn(owner, new Location(1,i));
			}

			return true;


		}else if(owner.equals("White")){

			for(int i = 0; i < 8; i++){

				this.cells[6][i] = new Pawn(owner, new Location(6,i));
			}

			this.cells[7][0] = new Rook(owner, new Location(7,0));
			this.cells[7][1] = new Knight(owner, new Location(7,1));
			this.cells[7][2] = new Bishop(owner, new Location(7,2));
			this.cells[7][3] = new Queen(owner, new Location(7,3));
			this.cells[7][4] = new King(owner, new Location(7,4));
			this.cells[7][5] = new Bishop(owner, new Location(7,5));
			this.cells[7][6] = new Knight(owner, new Location(7,6));
			this.cells[7][7] = new Rook(owner, new Location(7,7));

			return true;

		}else{

			return false;
		}
	}


	public void updateBoard(Piece piece, Location location){

		if(piece instanceof King){
			RelativeLayout.LayoutParams layoutParams;
			int drawableId = 0;
			String resField;


			if(piece.getOwner().equals("White")){
				resField = "whiter";
			}else{
				resField = "blackr";
			}



			if(Math.abs(piece.getPos().getJ() - location.getJ()) == 2){
				//castling

				//right castle
				if(location.getJ() > piece.getPos().getJ()){

					try {
						Class<drawable> res = R.drawable.class;
						Field field = null;
						field = res.getField(resField);
						drawableId = field.getInt(null);

						this.cells[location.getI()][location.getJ()] = piece; // move king
						Log.d("msg",this.cells[location.getI()][location.getJ()+1].toString());
						this.cells[location.getI()][location.getJ()+1].kill();
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1] = new Rook(piece.getOwner(), new Location(piece.getPos().getI(), piece.getPos().getJ() + 1));



						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap = new PieceView(this.container.getContext(), this.cells[piece.getPos().getI()][piece.getPos().getJ()+1]);
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.setImageResource(drawableId);
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.getLayoutParams().height = sDim / 8;
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.getLayoutParams().width = sDim / 8;
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.requestLayout();

						this.container.addView(this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap);
						layoutParams = (RelativeLayout.LayoutParams) this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.getLayoutParams();
						layoutParams.leftMargin = (sDim / 8) * (piece.getPos().getJ()+1);
						layoutParams.topMargin = (sDim / 8) * (piece.getPos().getI());

						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.setLayoutParams(layoutParams);
						this.cells[piece.getPos().getI()][piece.getPos().getJ()+1].pieceMap.setOnTouchListener(this.onTouchListener);

					}catch(Exception e){
						Log.e("MyTag", "Failure to get drawable id.", e);
					}

					//left side
				}else{


					try {
						Class<drawable> res = R.drawable.class;
						Field field = null;
						field = res.getField(resField);
						drawableId = field.getInt(null);


						this.cells[location.getI()][location.getJ()] = piece; // move king
						this.cells[location.getI()][location.getJ()-2].kill();
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1] = new Rook(piece.getOwner(), new Location(piece.getPos().getI(), piece.getPos().getJ() + 1));


						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap = new PieceView(this.container.getContext(), this.cells[piece.getPos().getI()][piece.getPos().getJ()-1]);
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.setImageResource(drawableId);
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.getLayoutParams().height = sDim / 8;
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.getLayoutParams().width = sDim / 8;
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.requestLayout();

						this.container.addView(this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap);
						layoutParams = (RelativeLayout.LayoutParams) this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.getLayoutParams();
						layoutParams.leftMargin = (sDim / 8) * (piece.getPos().getJ()-1);
						layoutParams.topMargin = (sDim / 8) * (piece.getPos().getI());

						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.setLayoutParams(layoutParams);
						this.cells[piece.getPos().getI()][piece.getPos().getJ()-1].pieceMap.setOnTouchListener(this.onTouchListener);

					}catch(Exception e){
						Log.e("MyTag", "Failure to get drawable id.", e);
					}

				}

				this.cells[piece.getPos().getI()][piece.getPos().getJ()] = null;
				piece.hasMoved();
				piece.updatePos(location);

				return;
			}

		}

		if(piece instanceof Pawn){

			if(Math.abs(piece.getPos().getI() - location.getI()) == 2){

				if(piece.getPos().getI() > location.getI()){

					this.cells[piece.getPos().getI() - 1][piece.getPos().getJ()] = new Enpassant(piece.getOwner(), new Location(piece.getPos().getI() - 1, piece.getPos().getJ()), location);
				}else{

					this.cells[piece.getPos().getI() + 1][piece.getPos().getJ()] = new Enpassant(piece.getOwner(), new Location(piece.getPos().getI() + 1, piece.getPos().getJ()), location);
				}
			}
		}

		this.cells[location.getI()][location.getJ()] = piece;
		this.cells[piece.getPos().getI()][piece.getPos().getJ()] = null;
		piece.updatePos(location);

		if(piece.atStart()){

			piece.hasMoved();

		}

	}


	public void nukeCell(Location location){

		this.cells[location.getI()][location.getJ()] = null;
	}

	public boolean isEnpassant(int i, int j){

		if(this.cells[i][j] instanceof Enpassant){
			return true;
		}else{
			return false;
		}
	}


	public Piece[][] getBoard(){
		return this.cells;
	}

	@SuppressLint("DefaultLocale")
	public void updatePiece(Piece piece, String newPiece){
		RelativeLayout.LayoutParams layoutParams;
		newPiece = newPiece.toLowerCase();

		if(piece.isCopy){
			return;
		}

		char choice = newPiece.charAt(0);

		if(choice == 'k'){
			choice = 'n';
			newPiece = newPiece.substring(1);
		}


		switch(choice){

		case 'q':
			this.cells[piece.getPos().getI()][piece.getPos().getJ()].kill();
			this.cells[piece.getPos().getI()][piece.getPos().getJ()] = new Queen(piece.getOwner(), piece.getPos());
			break;
		case 'b':
			this.cells[piece.getPos().getI()][piece.getPos().getJ()].kill();
			this.cells[piece.getPos().getI()][piece.getPos().getJ()] = new Bishop(piece.getOwner(), piece.getPos());
			break;
		case 'n':
			this.cells[piece.getPos().getI()][piece.getPos().getJ()].kill();
			this.cells[piece.getPos().getI()][piece.getPos().getJ()] = new Knight(piece.getOwner(), piece.getPos());
			break;
		case 'r':
			this.cells[piece.getPos().getI()][piece.getPos().getJ()].kill();
			this.cells[piece.getPos().getI()][piece.getPos().getJ()] = new Rook(piece.getOwner(), piece.getPos());
			break;
		default:

		}

		if(piece.getOwner().toString().charAt(0) == 'W'){

			try {
				Class<drawable> res = R.drawable.class;
				Field field = null;
				field = res.getField("white" + newPiece.substring(0,1));
				int drawableId = field.getInt(null);

				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap = new PieceView(this.container.getContext(), this.cells[piece.getPos().getI()][piece.getPos().getJ()]);
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setImageResource(drawableId);
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.getLayoutParams().height = sDim / 8;
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.getLayoutParams().width = sDim / 8;
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.requestLayout();

				this.container.addView(this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap);
				layoutParams = (RelativeLayout.LayoutParams) this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.getLayoutParams();
				layoutParams.leftMargin = (sDim / 8) * piece.getPos().getJ();
				layoutParams.topMargin = (sDim / 8) * piece.getPos().getI();

				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setLayoutParams(layoutParams);
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setOnTouchListener(this.onTouchListener);
			}
			catch (Exception e) {

				Log.e("MyTag", "Failure to get drawable id.", e);

			}

		}else{

			try {
				Class<drawable> res = R.drawable.class;
				Field field = null;
				field = res.getField("black" + newPiece.substring(0,1));
				int drawableId = field.getInt(null);

				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap = new PieceView(this.container.getContext(), this.cells[piece.getPos().getI()][piece.getPos().getJ()]);
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setImageResource(drawableId);
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.getLayoutParams().height = sDim / 8;
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.getLayoutParams().width = sDim / 8;
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.requestLayout();

				this.container.addView(this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap);
				layoutParams = (RelativeLayout.LayoutParams) this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.getLayoutParams();
				layoutParams.leftMargin = (sDim / 8) * piece.getPos().getJ();
				layoutParams.topMargin = (sDim / 8) * piece.getPos().getI();

				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setLayoutParams(layoutParams);
				this.cells[piece.getPos().getI()][piece.getPos().getJ()].pieceMap.setOnTouchListener(this.onTouchListener);
			}
			catch (Exception e) {

				Log.e("MyTag", "Failure to get drawable id.", e);

			}

		}

	}


	public Board getClone(){
		try {
			return (Board) this.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		Board cloned = new Board(this.cells);
		return cloned;
	}


	public RelativeLayout getContainer() {
		return container;
	}


	public void setContainer(RelativeLayout container) {
		this.container = container;
	}


	public int getDimension() {
		return sDim;
	}


	public void setDimension(int sDim) {
		this.sDim = sDim;
	}


	public OnTouchListener getOnTouchListener() {
		return onTouchListener;
	}


	public void setOnTouchListener(OnTouchListener onTouchListener) {
		this.onTouchListener = onTouchListener;
	}


}
