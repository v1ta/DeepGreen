package com.defritza.model;

import com.defritza.util.Location;

/**
 * King object
 * @author Joseph, Chris
 */
public class King extends Piece {
  private static final long serialVersionUID = 1;

  public King(String owner, Location currentPos) {
    super(currentPos);
    if (owner.equals("Black")) {
      this.asciiModel = "bK";
    } else {
      this.asciiModel = "wK";
    }
    this.owner = owner;
    this.moveset = new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    this.sMoveset = new int[][]{{0, 1}, {0, -1}};
    this.numMoves = 1;
  }

  public King(Piece piece) {
    super(piece);
  }
}