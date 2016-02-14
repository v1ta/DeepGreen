package com.defritza.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.annotation.SuppressLint;

/**
 * Player object
 * @author Joseph, Chris
 *
 */
public class Player implements Serializable {
	
	
	protected String player;
	protected ArrayList<Piece> captured;
	private static final long serialVersionUID = 1;
	
	public Player() {
		this("White");
	}
	
	@SuppressLint("DefaultLocale")
	public Player(String team){
		player = team.equalsIgnoreCase("white") ? "White" : "Black";
		captured = new ArrayList<Piece>();
	}
	
	public String toString(){
		return player;
	}
	
	public Piece getCappedPiece(){
		return captured.get(captured.size() - 1);
	}
	
	public int numCapeed(){
		return captured.size();
	}
	
	public void capturePiece(Piece piece){
		if (!piece.getOwner().equals(player)) {
			captured.add(piece);
		} else {
			throw new IllegalArgumentException("Player cannot capture own piece");
		}
	}

}
