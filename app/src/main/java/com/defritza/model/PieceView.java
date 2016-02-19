package com.defritza.model;

import android.content.Context;
import android.widget.ImageView;

/**
 * @author Joseph, Chris
 */
public class PieceView extends ImageView {
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1;
  private Piece piece;

  public PieceView(Context context, Piece piece) {
    super(context);
    this.setPiece(piece);
  }

  public Piece getPiece() {
    return piece;
  }

  public void setPiece(Piece piece) {
    this.piece = piece;
  }
}