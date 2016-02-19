package com.defritza.model;

import com.defritza.util.Location;

/**
 * Bishop Object
 *
 * @author Joseph, Chris
 */
public class Bishop extends Piece {
  private static final long serialVersionUID = 1;
  public Bishop(String owner, Location currentPos) {
    super(currentPos);
    if (owner.equals("Black")) {
      this.asciiModel = "bB";
    } else {
      this.asciiModel = "wB";
    }
    this.owner = owner;
    this.moveset = new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
    this.numMoves = 7;
  }
  public Bishop(Piece piece) {
    super(piece);
  }
}
