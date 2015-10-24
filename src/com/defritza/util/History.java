package com.defritza.util;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Stack;

import com.defritza.control.Chess;
import com.defritza.control.Playable;
import com.defritza.model.Board;
import com.defritza.model.Piece;
import com.defritza.model.Player;
/**
 * 
 * @author Joseph, Chris
 *
 */
public class History implements Serializable, Playable {

	private static final long serialVersionUID = 1;
	private LinkedList<Move> future;
	private Stack<Move> past;
	private String name;
	private boolean frozen;

	public History(String name) {
		past = new Stack<Move>();
		future = new LinkedList<Move>();
		this.name = name;
		frozen = false;
	}

	public void addMove(Player owner, Location to, Location from, Piece moved) {
		addMove(new Move(owner, to, from, moved));
	}

	public void addMove(Player owner, Location to, Location from, Piece moved, Piece killed) {
		addMove(new Move(owner, to, from, moved, killed));
	}

	public void addMove(Move move) {
		if (frozen) throw new IllegalStateException("Moves cannot be added to a historical game after it has been frozen.");

		while (!past.empty()) past.pop();
		future.offer(move);
	}

	@Override
	public void start() {
		// Nothing to do...
	}
	
	public String getName(){
		return this.name;
	}
	
	@Override
	public Move getMove(Chess game, Board board) {
		return advance();
	}

	@Override
	public boolean updatePlayer(Move move) {
		return true;
	}

	@Override
	public void finish() {
		// Nothing to do...
	}

	public void setName(String name){

		this.name = name;
	}

	public Move advance() {
		if (frozen) {
			Move current = future.poll();
			if (current != null) past.push(current);
			return current;
		} else {
			Move current = past.pop();
			if (current != null) future.offer(current);
			return current;
		}
	}

	public Move rollback() {
		if (frozen) {
			if (!past.empty()) {
				Move current = past.pop();
				if (current != null) future.addFirst(current);
				return current;
			}
		} else {
			if (!future.isEmpty()) {
				Move current = future.removeLast();
				if (current != null) past.push(current);
				return current;
			}
		}
		return null;
	}

	public void freeze() {
		frozen = true;
	}

	@Override
	public String toString() {
		return name;
	}

}
