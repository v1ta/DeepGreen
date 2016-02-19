package com.defritza.util;

import com.defritza.model.Piece;
import com.defritza.model.Player;

import java.io.Serializable;

/**
 * @author Joseph, Chris
 */
public class Move implements Serializable {
  private static final long serialVersionUID = 1;
  public Player owner;
  public Location to, from, from_clone;
  public Piece moved;
  public Piece killed;
  public Piece clone;

  public Move(Player owner, Location to, Location from, Piece moved) {
    this.owner = owner;
    this.to = to;
    this.from = from;
    this.moved = moved;
    this.killed = null;
  }

  public Move(Player owner, Location to, Location from, Piece moved, Piece killed) {
    this.owner = owner;
    this.to = to;
    this.from = from;
    this.moved = moved;
    this.killed = killed;
  }

  public Move(Player owner, Location to, Location from, Piece moved, Location from_clone, Piece clone) {
    this.owner = owner;
    this.to = to;
    this.from = from;
    this.moved = moved;
    this.from_clone = from_clone;
    this.clone = clone;
  }
}