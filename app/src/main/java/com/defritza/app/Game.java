package com.defritza.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.defritza.chess.R;
import com.defritza.chess.R.drawable;
import com.defritza.control.Chess;
import com.defritza.control.Playable;
import com.defritza.model.Bishop;
import com.defritza.model.Board;
import com.defritza.model.ComputerPlayer;
import com.defritza.model.Enpassant;
import com.defritza.model.King;
import com.defritza.model.Knight;
import com.defritza.model.Pawn;
import com.defritza.model.Piece;
import com.defritza.model.PieceView;
import com.defritza.model.Player;
import com.defritza.model.Queen;
import com.defritza.model.RemotePlayer;
import com.defritza.model.Rook;
import com.defritza.util.Constants;
import com.defritza.util.History;
import com.defritza.util.Location;
import com.defritza.util.Move;
import com.defritza.util.Serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author Joseph, Chris
 */
@SuppressWarnings({"deprecation", "unused"})
@SuppressLint("NewApi")
public class Game extends ActionBarActivity implements View.OnClickListener, DialogInterface.OnClickListener, View.OnTouchListener {

  static Context context;
  private final CharSequence upgrade[] = {"Queen", "Bishop", "Knight", "Rook"};
  private BoardView boardView;
  private Chess game;
  private Board board;
  private Player playerWhite, playerBlack, playerTurn;
  private Playable automaton;
  private History replay;
  private Piece promoted;
  private GameType type;
  private Button buttonUndoMove, buttonNextMove, buttonAIMove, buttonDraw, buttonResign;
  private EditText gameName;
  private AlertDialog.Builder drawBuilder;
  private AlertDialog drawDialog, promoteDialog, gameOverDialog, gameSaveDialog;
  private RelativeLayout boardContainer;
  private RelativeLayout.LayoutParams layoutParams, originalLocation;
  private PieceView toMove;
  private Location origin;
  private int sDim, activePointerId;
  private float lastX, lastY;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_play);
    context = this;
    boardContainer = (RelativeLayout) findViewById(R.id.game_play_board_frame);
    boardView = new BoardView(boardContainer.getContext(), 8, Color.BLACK, Color.WHITE);
    boardContainer.addView(boardView);

    // Get arguments passed through the Intent object.
    Intent intent = getIntent();
    board = (Board) intent.getSerializableExtra(Constants.EXTRA_BOARD);
    board.setContainer(boardContainer);
    game = (Chess) intent.getSerializableExtra(Constants.EXTRA_CHESS);
    playerWhite = (Player) intent.getSerializableExtra(Constants.EXTRA_W_PLAYER);
    playerBlack = (Player) intent.getSerializableExtra(Constants.EXTRA_B_PLAYER);
    replay = (History) intent.getSerializableExtra(Constants.EXTRA_HISTORY);

    // Figure out what kind of game we're playing.
    if (replay != null) {
      type = GameType.REPLAY;
    } else if (playerBlack instanceof ComputerPlayer) {
      type = GameType.COMPUTER;
      automaton = (Playable) playerBlack;
    } else if (playerWhite instanceof RemotePlayer || playerBlack instanceof RemotePlayer) {
      type = GameType.REMOTE;
      automaton = playerWhite instanceof RemotePlayer ? (Playable) playerWhite : (Playable) playerBlack;
      automaton.start();
    } else {
      type = GameType.LOCAL;
    }

    // Perform necessary setup.
    playerTurn = playerWhite;
    board.loadPlayer(playerBlack);
    board.loadPlayer(playerWhite);
    game.calculateMoves(board);
    if (replay == null) replay = new History("2 player game");

    // Grab all of the buttons.
    buttonResign = (Button) findViewById(R.id.buttonResign);
    buttonDraw = (Button) findViewById(R.id.buttonDraw);
    buttonAIMove = (Button) findViewById(R.id.buttonAIMove);
    buttonUndoMove = (Button) findViewById(R.id.buttonUndoMove);
    buttonNextMove = (Button) findViewById(R.id.buttonNextMove);

    // Setup listeners.
    buttonAIMove.setOnClickListener(this);
    buttonUndoMove.setOnClickListener(this);
    buttonNextMove.setOnClickListener(this);
    buttonDraw.setOnClickListener(this);
    buttonResign.setOnClickListener(this);

    // Setup the draw dialog popup.
    drawBuilder = new AlertDialog.Builder(this);
    drawBuilder.setMessage("Opponent requests a draw. Accept?");
    drawBuilder.setCancelable(true);
    drawBuilder.setPositiveButton("Yes", this);
    drawBuilder.setNegativeButton("No", this);

    // Configure game features.
    setupGameFeatures();

    // Add a listener to get the size of the BoardView and setup pieces after initial layout.
    final Game that = this;
    boardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

      @Override
      public void onGlobalLayout() {
        // Ensure you call it only once :
        boardView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

        sDim = boardView.getMeasuredWidth();
        board.setDimension(sDim);
        board.setContainer(boardContainer);
        board.setOnTouchListener(that);

        loadBoard();
      }

    });

    if (type == GameType.REMOTE && playerWhite instanceof RemotePlayer) runOpponentMove(null);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {

    if (dialog == drawDialog) {

      if (which == -1) {

        dialog.cancel();
        gameOver("Draw");
        return;
      }
    } else if (dialog == gameOverDialog) {
      AlertDialog.Builder gameSaveBuilder = new AlertDialog.Builder(this);
      gameSaveBuilder.setTitle("Save Replay");
      gameName = new EditText(this);
      gameSaveBuilder.setView(gameName);
      gameSaveBuilder.setPositiveButton("Save", this);
      gameSaveBuilder.setNegativeButton("Cancel", this);
      gameSaveDialog = gameSaveBuilder.create();
      if (type != GameType.REPLAY) gameSaveDialog.show();
    } else if (dialog == gameSaveDialog) {
      if (which == -1) {
        replay.setName(gameName.getText().toString());
        if (replay.getName().isEmpty()) {
          replay.setName("DEFAULT REPLAY NAME");
        }
        Serializer.writeHistory(this, replay);
      }
      finish();
    } else if (dialog == promoteDialog) {
      board.updatePiece(promoted, upgrade[which].toString());
      Move old = replay.rollback();
      replay.addMove(new Move(old.owner, old.to, old.from, old.moved, old.to, board.getPiece(old.to)));
    }
    dialog.cancel();
  }

  @Override
  public boolean onTouch(View view, MotionEvent event) {
    // Perform setup and sanity checks.
    if (type == GameType.REPLAY) return false;
    if (view instanceof PieceView) {
      toMove = (PieceView) view;
      layoutParams = (RelativeLayout.LayoutParams) toMove.getLayoutParams();
    } else {
      return false;
    }
    int action = event.getAction();

    switch (action & MotionEvent.ACTION_MASK) {

      // We're beginning to draw a Piece.
      case MotionEvent.ACTION_DOWN:
        // Make sure the chosen Piece is on the right team..
        if (toMove.getPiece().getOwner().equals(game.playerTurn().toString())) {
          ArrayList<Location> validMoves = toMove.getPiece().getValidMoves();

				/* Make sure the selected piece has >= 1 move(s) */
          if (validMoves.size() > 0) {
            // Save starting location and layout constraints for chosen Piece.
            originalLocation = (RelativeLayout.LayoutParams) toMove.getLayoutParams();
            origin = new Location(originalLocation.topMargin, originalLocation.leftMargin, sDim);

            // Get the starting X and Y coordinates for this touch event.
            lastX = event.getX();
            lastY = event.getY();

            // Functionality allows for multiple pieces to be moved at once, while this isn't allowed having these events eliminated crashes .
            activePointerId = event.getPointerId(0);
          } else {
            // Piece has no valid moves, cancel touch event.
            return false;
          }
        } else {
          // Not this player's turn, cancel touch event.
          return false;
        }
        break;

      // We're in the middle of dragging a piece.
      case MotionEvent.ACTION_MOVE:
        // Get the new X and Y for this touch event and calculate the differential.
        int pointerIndex = event.findPointerIndex(activePointerId);
        float x = event.getX(pointerIndex), y = event.getY(pointerIndex);
        float dx = x - lastX, dy = y - lastY;

        // Update offsets.
        layoutParams.leftMargin += dx;
        layoutParams.topMargin += dy;
        toMove.setLayoutParams(layoutParams);
        break;

      // We're ending the process of dragging a piece.
      case MotionEvent.ACTION_UP:
        // Get initial state.
        Location from = toMove.getPiece().getPos();
        Location move = new Location(layoutParams.topMargin + ((sDim / 8) / 2), layoutParams.leftMargin + ((sDim / 8) / 2), sDim);
        Move tentative = performMove(new Move(game.playerTurn(), move, from, toMove.getPiece()));

        // Check if the move the user has selected is valid.
        if (tentative != null) {
          if ((toMove.getPiece() instanceof Pawn || toMove.getPiece() instanceof Enpassant) && toMove.getPiece().getUpgradeLoc() == move.getI()) {
            promoted = toMove.getPiece();
            updatePiece();
          }

          if (type == GameType.COMPUTER || type == GameType.REMOTE)
            runOpponentMove(tentative);
        } else {
          // User has selected an invalid move. Reset Piece offsets to their original values.
          layoutParams.leftMargin = (sDim / 8) * origin.getJ();
          layoutParams.topMargin = (sDim / 8) * origin.getI();
          toMove.setLayoutParams(layoutParams);
        }

        activePointerId = -1;
        break;

      // Drag event was canceled.
      case MotionEvent.ACTION_CANCEL:
        activePointerId = -1;
        break;

    }

    return true;
  }

  @Override
  public void onClick(View view) {
    Button clicked = (Button) view;
    RelativeLayout.LayoutParams layoutParams;

    if (clicked == buttonAIMove) {
      // Play a sound effect for the user.
      boardContainer.playSoundEffect(SoundEffectConstants.CLICK);

      // Calculate and execute a move.
      Move aiMove = ComputerPlayer.calculateMove(game.playerTurn(), game, board);
      performMove(aiMove);
      game.turn();
      // Update board and perform validation.
      game.turn();
      game.calculateMoves(board);
      if (game.blackCheck()) {
        if (game.checkMate(board, playerBlack))
          gameOver("Black Checkmate");
        else
          check("Black Check");
      } else if (game.whiteCheck()) {
        if (game.checkMate(board, playerWhite))
          gameOver("White Checkmate");
        else
          check("White Check");
      } else if (game.stalemate(board, playerTurn)) {
        gameOver("Stalemate");
      }
    } else if (clicked == buttonUndoMove) {
      int times = type == GameType.COMPUTER ? 2 : 1;

      for (int i = 0; i < times; i++) {
        // Get the previous move and perform sanity checks.
        Move toUndo = replay.rollback();
        if (toUndo == null) return;
        game.undo();
        boardContainer.playSoundEffect(SoundEffectConstants.CLICK);

        // Undo the move and redraw the piece's position on the board.
        board.getBoard()[toUndo.from.getI()][toUndo.from.getJ()] = toUndo.moved;
        toUndo.moved.updatePos(toUndo.from);
        toUndo.moved.resetValidMoves();
        if (type == GameType.REPLAY) {
          dumpPiece(board.getPiece(toUndo.to));
          loadPiece(toUndo.moved);
        }
        layoutParams = (RelativeLayout.LayoutParams) toUndo.moved.pieceMap.getLayoutParams();
        layoutParams.leftMargin = (sDim / 8) * toUndo.from.getJ();
        layoutParams.topMargin = (sDim / 8) * toUndo.from.getI();
        toUndo.moved.pieceMap.setLayoutParams(layoutParams);
        board.nukeCell(toUndo.to);

        // Check if a piece was captured by this move.
        if (toUndo.killed != null) {
          // Uncapture the piece and redraw it on the board.
          board.getBoard()[toUndo.to.getI()][toUndo.to.getJ()] = toUndo.killed;
          toUndo.killed.updatePos(toUndo.to);
          toUndo.killed.resurrect();
          toUndo.killed.resetValidMoves();
          if (type == GameType.REPLAY) loadPiece(toUndo.killed);
          layoutParams = (RelativeLayout.LayoutParams) toUndo.killed.pieceMap.getLayoutParams();
          layoutParams.leftMargin = (sDim / 8) * toUndo.to.getJ();
          layoutParams.topMargin = (sDim / 8) * toUndo.to.getI();
          toUndo.killed.pieceMap.setLayoutParams(layoutParams);
        } else if (toUndo.clone != null) {
          toUndo.clone.kill();
          toUndo.clone = null;
          toUndo.moved.resurrect();
        }

        // Reset pawns as having not moved if we're undoing a pawn.
        if (toUndo.moved instanceof Pawn || toUndo.moved instanceof Enpassant) {
          if (toUndo.moved.getOwner().equals("White") && toUndo.from.getI() == 6)
            toUndo.moved.reset();
          else if (toUndo.moved.getOwner().equals("Black") && toUndo.from.getI() == 1)
            toUndo.moved.reset();
        }

        // Having successfully undone the most recent move, recalculate moves for the board.
        game.calculateMoves(board);
      }
    } else if (clicked == buttonNextMove) {
      performMove(replay.advance());
    } else if (clicked == buttonDraw) {
      drawDialog = drawBuilder.create();
      drawDialog.show();
    } else if (clicked == buttonResign) {
      if (game.playerTurn().toString().equalsIgnoreCase("white"))
        gameOver("White has resigned");
      else
        gameOver("Black has resigned");
    }
  }

  private void loadBoard() {
    ArrayList<Piece> pieces = board.getStartSetup();
    String[] pieceIndex = {"r", "n", "b", "q", "k", "b", "n", "r", "p"};

    int i = 0, j = 0;
    for (Piece piece : pieces) {
      try {
        // Grab the resource class object.
        Class<drawable> res = R.drawable.class;

        // Calculate the current piece index.
        if (i < 8) j = i;
        else if (i >= 8 && i < 24) j = 8;
        else j = i - 24;

        if (j > 8) j = 8;
        // Get ID of drawable for current piece via reflection.
        Field field = null;
        if (i < 16) {
          field = res.getField("black" + pieceIndex[j]);
        } else {
          field = res.getField("white" + pieceIndex[j]);
        }
        int drawableId = field.getInt(null);

        // Instantiate and configure the current PieceView.
        loadPiece(piece, drawableId);

        // Calculate offset for current PieceView.
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) piece.pieceMap.getLayoutParams();
        if (i < 8) {
          layoutParams.leftMargin = (sDim / 8) * i;
          layoutParams.topMargin = 0;
        } else if (i < 16) {
          layoutParams.leftMargin = (sDim / 8) * (i - 8);
          layoutParams.topMargin = (sDim / 8);
        } else if (i < 24) {
          layoutParams.leftMargin = (sDim / 8) * (i - 16);
          layoutParams.topMargin = sDim - ((sDim / 8) * 2);
        } else {
          layoutParams.leftMargin = (sDim / 8) * (i - 24);
          layoutParams.topMargin = sDim - (sDim / 8);
        }

        // Finalize current PieceView.
        piece.pieceMap.setLayoutParams(layoutParams);
        piece.pieceMap.setOnTouchListener(this);

        i++;
      } catch (ReflectiveOperationException e) {
        // Oh Java...
      }

    }

  }

  private void loadPiece(Piece piece) {
    String base = piece.getOwner().equals("White") ? "white" : "black";
    if (piece instanceof Pawn) {
      base += "p";
    } else if (piece instanceof Rook) {
      base += "r";
    } else if (piece instanceof Knight) {
      base += "n";
    } else if (piece instanceof Bishop) {
      base += "b";
    } else if (piece instanceof Queen) {
      base += "q";
    } else if (piece instanceof King) {
      base += "k";
    }

    try {
      int drawableId = R.drawable.class.getField(base).getInt(null);
      loadPiece(piece, drawableId);
    } catch (ReflectiveOperationException e) {
      // Oh Java...
    }
  }

  private void loadPiece(Piece piece, int drawableId) {
    piece.pieceMap = new PieceView(boardContainer.getContext(), piece);
    piece.pieceMap.setImageResource(drawableId);
    piece.pieceMap.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    piece.pieceMap.getLayoutParams().height = sDim / 8;
    piece.pieceMap.getLayoutParams().width = sDim / 8;
    piece.pieceMap.requestLayout();
    boardContainer.addView(piece.pieceMap);
  }

  private void dumpPiece(Piece piece) {
    if (piece != null) boardContainer.removeView(piece.pieceMap);
  }

  private void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
    int childCount = viewGroup.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View view = viewGroup.getChildAt(i);
      view.setEnabled(enabled);
      if (view instanceof ViewGroup) {
        enableDisableViewGroup((ViewGroup) view, enabled);
      }
    }
  }

  private void gameOver(String condition) {
    // The game is over, so freeze the History object.
    replay.freeze();
    enableDisableViewGroup(boardContainer, false);
    // Open Game Over dialog.
    AlertDialog.Builder gameOverBuilder = new AlertDialog.Builder(this);
    gameOverBuilder.setView(makeText(condition));
    gameOverBuilder.setCancelable(true);
    gameOverBuilder.setNegativeButton("OK", this);
    gameOverDialog = gameOverBuilder.create();
    gameOverDialog.show();
    // Cleanup Playable.
    if (automaton != null) automaton.finish();
  }

  private void check(String condition) {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setView(makeText(condition));
    alert.setCancelable(true);
    alert.setNegativeButton("OK", this);
    alert.create().show();
  }

  private void updatePiece() {
    AlertDialog.Builder promoteBuilder = new AlertDialog.Builder(this);
    promoteBuilder.setView(makeText("Promote Pawn"));
    promoteBuilder.setItems(upgrade, this);
    promoteBuilder.setCancelable(true);
    promoteDialog = promoteBuilder.create();
    promoteDialog.show();
  }

  private void runOpponentMove(Move move) {
    if (move != null) automaton.updatePlayer(move);

    Move opponent = automaton.getMove(game, board);
    if (opponent == null && type == GameType.COMPUTER) {
      gameOver("Black checkmate");
    }

    performMove(opponent);
  }

  private Move performMove(Move move) {
    if (move == null) {
      return null;
    }
    int temp = game.playerTurn().numCapeed();
    Piece moved = board.getPiece(move.from);

    boolean status = game.movePiece(moved, board, move.to, game.playerTurn());
    if (!status) {
      return null;
    }
    int temp2 = game.playerTurn().numCapeed();
    if (type != GameType.REPLAY) {
      replay.addMove(move = new Move(game.playerTurn(), move.to, move.from, moved, temp != temp2 ? game.playerTurn().getCappedPiece() : null));
    }
    // Update the view with the new move.
    if (moved.pieceMap == null) {
      dumpPiece(board.getPiece(move.from));
      loadPiece(moved);
    }
    layoutParams = (RelativeLayout.LayoutParams) moved.pieceMap.getLayoutParams();
    layoutParams.leftMargin = (sDim / 8) * move.to.getJ();
    layoutParams.topMargin = (sDim / 8) * move.to.getI();
    moved.pieceMap.setLayoutParams(layoutParams);
    // Update the board and perform validation.
    game.turn();
    game.calculateMoves(board);
    if (game.blackCheck()) {
      if (game.checkMate(board, playerBlack)) {
        gameOver("Black Checkmate");
      } else
        check("Black Check");
    } else if (game.whiteCheck()) {
      if (game.checkMate(board, playerWhite)) {
        gameOver("White Checkmate");
      } else
        check("White Check");
    } else {
      if (game.stalemate(board, playerTurn)) {
        gameOver("Stalemate");
      }
    }
    return move;
  }

  private void setupGameFeatures() {
    if (type == GameType.COMPUTER) {
      buttonDraw.setVisibility(View.GONE);
    } else if (type == GameType.REMOTE) {
      buttonUndoMove.setVisibility(View.GONE);
      buttonDraw.setVisibility(View.GONE);
      buttonResign.setVisibility(View.GONE);
    } else if (type == GameType.REPLAY) {
      buttonAIMove.setVisibility(View.GONE);
      buttonDraw.setVisibility(View.GONE);
      buttonResign.setVisibility(View.GONE);
      buttonUndoMove.setText("Previous");
      buttonNextMove.setVisibility(View.VISIBLE);
    }
  }

  private TextView makeText(String message) {
    TextView myMsg = new TextView(this);
    myMsg.setText(message);
    myMsg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
    return myMsg;
  }

  private enum GameType {
    LOCAL,
    COMPUTER,
    REMOTE,
    REPLAY
  }

  private class BoardView extends View {

    private int nSquares, colorA, colorB;
    private int squareDim;
    private Paint paint;

    public BoardView(Context context, int nSquares, int colorA, int colorB) {
      super(context);
      this.nSquares = nSquares;
      this.colorA = colorA;
      this.colorB = colorB;
      paint = new Paint();

    }

    @Override
    protected void onDraw(Canvas canvas) {
      for (int row = 0; row < nSquares; row++) {
        paint.setColor(((row & 1) == 0) ? colorA : colorB);
        for (int col = 0; col < nSquares; col++) {
          int a = col * squareDim;
          int b = row * squareDim;
          canvas.drawRect(a, b, a + squareDim, b + squareDim, paint);
          paint.setColor((paint.getColor() == colorA) ? colorB : colorA);
        }
      }
    }

    @Override
    protected void onMeasure(int widthMeasuredSpec, int heightMeasuredSpec) {
      int width = MeasureSpec.getSize(widthMeasuredSpec);
      int height = MeasureSpec.getSize(heightMeasuredSpec);
      int d = (width == 0) ? height : (height == 0) ? width : (width < height) ? width : height;
      setMeasuredDimension(d, d);
      squareDim = width / nSquares;
    }

  }

}
