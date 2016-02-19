package com.defritza.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.defritza.chess.R;
import com.defritza.control.Chess;
import com.defritza.model.Board;
import com.defritza.model.ComputerPlayer;
import com.defritza.model.Piece;
import com.defritza.model.Player;
import com.defritza.util.Constants;


@SuppressWarnings("deprecation")
/**
 * @author Joseph, Chris
 */
public class GameSelect extends ActionBarActivity implements OnClickListener {

  private Button localButton, remoteButton, computerButton, historyButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_select);
    localButton = (Button) findViewById(R.id.localButton);
    remoteButton = (Button) findViewById(R.id.remoteButton);
    computerButton = (Button) findViewById(R.id.computerButton);
    historyButton = (Button) findViewById(R.id.historyButton);
    localButton.setOnClickListener(this);
    remoteButton.setOnClickListener(this);
    computerButton.setOnClickListener(this);
    historyButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    Button clicked = (Button) view;
    Intent intent = null;
    if (clicked == localButton || clicked == computerButton) {
      Board board = new Board(new Piece[8][8]);
      Player white = new Player("White"), black = clicked == localButton ? new Player("Black") : new ComputerPlayer("Black");
      Chess game = new Chess(white, black);
      board.loadPlayer(white);
      board.loadPlayer(black);
      intent = new Intent(this, Game.class);
      intent.putExtra(Constants.EXTRA_BOARD, board);
      intent.putExtra(Constants.EXTRA_W_PLAYER, white);
      intent.putExtra(Constants.EXTRA_B_PLAYER, black);
      intent.putExtra(Constants.EXTRA_CHESS, game);
    } else if (clicked == remoteButton) {
      intent = new Intent(this, RemoteSetup.class);
    } else if (clicked == historyButton) {
      intent = new Intent(this, HistorySelect.class);
    }
    startActivity(intent);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.game_select, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
