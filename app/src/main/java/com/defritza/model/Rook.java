package com.defritza.model;

import com.defritza.util.Location;


/**
 * Rook object
 * @author Joseph, Chris
 */
public class Rook extends Piece {
  private static final long serialVersionUID = 1;

  public Rook(String owner, Location currentPos) {
    super(currentPos);
    if (owner.equals("Black")) {
      this.asciiModel = "bR";
    } else {
      this.asciiModel = "wR";
    }
    this.owner = owner;
    this.moveset = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    this.numMoves = 7;
  }

  public Rook(Piece piece) {
    super(piece);
  }
}