package com.defritza.model;

import com.defritza.util.Location;


/**
 * Knight object
 * @author Joseph, Chris
 *
 */
public class Knight extends Piece{
	
	private static final long serialVersionUID = 1;

	public Knight(String owner, Location currentPos){
		
		super(currentPos);
		if(owner.equals("Black")){
			
			this.asciiModel = "bN";
		}else{
			
			this.asciiModel = "wN";
		}
		
		this.owner = owner;
		this.moveset = new int[][]{{2,1}, {-2,1}, {-2,-1}, {2,-1}, {1,2}, {-1,2}, {-1,-2}, {1,-2}};
		this.numMoves = 1;
	}
	
	public Knight(Piece piece){
		super(piece);
	}
}
