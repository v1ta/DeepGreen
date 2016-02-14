package com.defritza.model;

import com.defritza.util.Location;


/**
 * Pawn object
 * @author Joseph, Chris
 *
 */
public class Pawn extends Piece{
	
	private static final long serialVersionUID = 1;
	
	public Pawn(String owner, Location currentPos){
		
		super(currentPos);
		if(owner.equals("Black")){
			
			this.asciiModel = "bp";
			this.upgradeLoc = 7;
			this.startRow = 1;
			this.moveset = new int[][]{{1, 0}};
			this.sMoveset = new int[][]{{1, -1} , {1 , 1}};
		}else{
			
			this.asciiModel = "wp";
			this.upgradeLoc = 0;
			this.startRow = 6;
			this.moveset = new int[][]{{-1, 0}};
			this.sMoveset = new int[][]{{-1, -1} , {-1 , 1}};
		}
		
		this.owner = owner;
		this.numMoves = 1;
	}
	
	public Pawn(Piece piece){
		super(piece);
	}
	

}
