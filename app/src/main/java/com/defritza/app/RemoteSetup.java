package com.defritza.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.defritza.chess.R;
import com.defritza.control.Chess;
import com.defritza.model.Board;
import com.defritza.model.Piece;
import com.defritza.model.Player;
import com.defritza.model.RemotePlayer;
import com.defritza.util.Constants;

@SuppressWarnings("deprecation")
/**
 * 
 * @author Joseph, Chris
 *
 */
public class RemoteSetup extends ActionBarActivity implements OnClickListener {
	
	private Button hostButton, clientButton, remoteGameButton, cancelButton;
	private TextView addressLabel, portLabel, errorLabel;
	private EditText addressField, portField;
	private boolean server;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_setup);
		
		hostButton = (Button) findViewById(R.id.hostButton);
		clientButton = (Button) findViewById(R.id.clientButton);
		remoteGameButton = (Button) findViewById(R.id.remoteGameButton);
		cancelButton = (Button) findViewById(R.id.remoteCancelButton);
		
		addressLabel = (TextView) findViewById(R.id.addressLabel);
		portLabel = (TextView) findViewById(R.id.portLabel);
		addressField = (EditText) findViewById(R.id.addressField);
		portField = (EditText) findViewById(R.id.portField);
		errorLabel = (TextView) findViewById(R.id.errorLabel);
		
		hostButton.setOnClickListener(this);
		clientButton.setOnClickListener(this);
		remoteGameButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		Button clicked = (Button) view;
		if (clicked == hostButton || clicked == clientButton) {
			hostButton.setVisibility(View.GONE);
			clientButton.setVisibility(View.GONE);

			if (clicked == clientButton) {
				addressLabel.setVisibility(View.VISIBLE);
				addressField.setVisibility(View.VISIBLE);
				server = false;
			} else {
				server = true;
			}
			portLabel.setVisibility(View.VISIBLE);
			portField.setVisibility(View.VISIBLE);
			remoteGameButton.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		} else if (clicked == remoteGameButton) {
			String address = addressField.getText().toString();
			short port = Short.parseShort(portField.getText().toString());
			Player white, black;

			if (server) {
				white = new Player("White");
				black = new RemotePlayer(port);
			} else {
				white = new RemotePlayer(address, port);
				black = new Player("Black");
			}

			Intent intent = new Intent(this, Game.class);
			Chess game = new Chess(white, black);
			Board board = new Board(new Piece[8][8]);
			board.loadPlayer(white);
			board.loadPlayer(black);
			game.calculateMoves(board);
			intent.putExtra(Constants.EXTRA_W_PLAYER, white);
			intent.putExtra(Constants.EXTRA_B_PLAYER, black);
			intent.putExtra(Constants.EXTRA_BOARD, board);
			intent.putExtra(Constants.EXTRA_CHESS, game);
			
			startActivity(intent);
		} else if (clicked == cancelButton) {
			hostButton.setVisibility(View.VISIBLE);
			clientButton.setVisibility(View.VISIBLE);
			
			addressLabel.setVisibility(View.GONE);
			addressField.setVisibility(View.GONE);
			portLabel.setVisibility(View.GONE);
			portField.setVisibility(View.GONE);
			remoteGameButton.setVisibility(View.GONE);
			cancelButton.setVisibility(View.GONE);
			errorLabel.setText("");
			errorLabel.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.remote_setup, menu);
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
