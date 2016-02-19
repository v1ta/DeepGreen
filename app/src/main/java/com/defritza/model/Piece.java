package com.defritza.model;

import android.view.View;
import android.widget.ImageView;

import com.defritza.util.Location;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Abstract piece object
 * @author Joseph, Chris
 */

public class Piece implements Cloneable, Serializable {
  private static final long serialVersionUID = 1;
  protected static int pieceCount;
  public transient ImageView pieceMap;
  protected Boolean alive = true;
  protected String asciiModel;
  protected String owner;
  protected int startRow;
  protected ArrayList<Location> validMoves;
  protected int[][] moveset; //unit vectors for standard moves
  protected int[][] sMoveset; //units vectors for special moves (castle, inital pawn moves, etc...)
  protected int numMoves;
  protected Location currentPos;
  protected boolean atStart = true; //flag that the piece hasn't moved
  protected Location ghost; // field for enpassant/pawn functionality
  protected int turnsAlive;
  protected int upgradeLoc;
  protected int pieceID;
  protected boolean isCopy = false; //flag if the piece is a copy

  /**
   * Default constructor
   * @param currentPos
   */
  public Piece(Location currentPos) {

    this.alive = true;
    this.validMoves = new ArrayList<Location>();
    this.currentPos = currentPos;
    this.pieceID = pieceCount;
    this.pieceMap = null;
    pieceCount++;
  }

  /**
   * Deep copy constructor. Allows for +1 moves to be calculated without manipulating the actual board
   * @param piece
   */
  public Piece(Piece piece) {
    this.isCopy = true;
    this.pieceID = piece.pieceID;
    this.alive = piece.alive;
    this.asciiModel = piece.asciiModel;
    this.owner = piece.owner;
    this.numMoves = piece.numMoves;
    this.currentPos = new Location(piece.currentPos.getI(), piece.currentPos.getJ());
    this.atStart = piece.atStart;
    if (piece.getGhost() != null) {
      this.ghost = new Location(piece.getGhost().getI(), piece.getGhost().getJ());
    }
    this.turnsAlive = piece.turnsAlive;
    this.upgradeLoc = piece.upgradeLoc;
    this.validMoves = new ArrayList<>();
    for (int i = 0; i < piece.validMoves.size(); i++) {
      this.validMoves.add(new Location(piece.validMoves.get(i).getI(), piece.validMoves.get(i).getJ()));
    }
    if (!(piece instanceof Enpassant)) {
      this.moveset = new int[piece.moveset.length][2];
      for (int i = 0; i < piece.moveset.length; i++) {
        System.arraycopy(piece.moveset[i], 0, this.moveset[i], 0, 2);
      }
      if (piece.asciiModel.charAt(1) == 'p' || piece.asciiModel.charAt(1) == 'K') {
        this.sMoveset = new int[piece.sMoveset.length][2];
        for (int i = 0; i < piece.sMoveset.length; i++) {
          System.arraycopy(piece.sMoveset[i], 0, this.sMoveset[i], 0, 2);
        }
      }
    }
  }

  public void kill() {
    this.alive = false;
    if (this.pieceMap != null) {
      this.pieceMap.setEnabled(false);
      this.pieceMap.setVisibility(View.INVISIBLE);
    }
  }

  public void resurrect() {
    this.alive = true;
    if (pieceMap != null) {
      pieceMap.setEnabled(true);
      pieceMap.setVisibility(View.VISIBLE);
    }
  }

  public String toString() {
    return this.asciiModel;
  }

  public int[][] getMoveSet() {
    return this.moveset;
  }

  public int getMoves() {
    return this.numMoves;
  }

  public void addValidMove(Location location) {
    this.validMoves.add(location);
  }

  public String getOwner() {
    return this.owner;
  }

  public ArrayList<Location> getValidMoves() {
    return this.validMoves;
  }

  public void resetValidMoves() {
    this.validMoves.clear();
  }

  public Location getPos() {
    return this.currentPos;
  }

  public void updatePos(Location pos) {
    this.currentPos = pos;
  }

  public boolean atStart() {
    return this.atStart;
  }

  public void hasMoved() {
    this.atStart = false;
  }

  public void reset() {
    this.atStart = true;
  }

  public int[][] getSMoveSet() {

    return this.sMoveset;
  }

  public Location getGhost() {
    return this.ghost;
  }

  public int getTurns() {
    return this.turnsAlive;
  }

  public void incrementTurn() {
    this.turnsAlive++;
  }

  public void delMove(Location moveToDel) {
    this.validMoves.remove(moveToDel);
  }

  public int getUpgradeLoc() {
    return this.upgradeLoc;
  }

  public boolean isCopy() {
    return this.isCopy;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    Piece cloned = (Piece) super.clone();
    return cloned;
  }
}