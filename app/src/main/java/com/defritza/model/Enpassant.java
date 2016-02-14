package com.defritza.model;


import com.defritza.util.Location;

/**
 * Enpassant object
 * @author Joseph, Chris
 *
 */
public class Enpassant extends Piece{

	private static final long serialVersionUID = 1;
	
	public Enpassant(String owner, Location location, Location ghost){
		
		super(location);
		this.owner = owner;
		this.ghost = ghost;
		this.turnsAlive = 0;
		this.asciiModel = "";
	}
	
	public Enpassant(Piece piece){
		super(piece);
	}
	
}
