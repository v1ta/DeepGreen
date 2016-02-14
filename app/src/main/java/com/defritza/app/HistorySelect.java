package com.defritza.app;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.defritza.chess.R;
import com.defritza.control.Chess;
import com.defritza.model.Board;
import com.defritza.model.Piece;
import com.defritza.model.Player;
import com.defritza.util.Constants;
import com.defritza.util.History;
import com.defritza.util.Serializer;
/**
 * 
 * @author Joseph, Chris
 *
 */
@SuppressWarnings("deprecation")
public class HistorySelect extends ActionBarActivity implements OnItemClickListener {
	
	private class ThemedAdapter<T> extends ArrayAdapter<T> {

		private LayoutInflater inflater;
		
		public ThemedAdapter(Context context, int resourceId, ArrayList<T> items) {
			super(context, resourceId, items);
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			
			if (position == 0) {
				convertView.setBackgroundResource(R.drawable.rounded_top_rect);
			} else if (position == getCount() - 1) {
				convertView.setBackgroundResource(R.drawable.rounded_bottom_rect);
			} else {
				convertView.setBackgroundResource(R.drawable.rect);
			}
			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setText(getItem(position).toString());
			
			return convertView;
		}

	}
	
	private ArrayAdapter<History> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_selection);

		ListView list = (ListView) findViewById(R.id.historyList);
		TextView error = (TextView) findViewById(R.id.no_game_message);
		ArrayList<History> games = Serializer.readHistories(this);
		adapter = new ThemedAdapter<History>(this, android.R.layout.simple_list_item_1, games);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		if (games.size() == 0) error.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		History chosen = adapter.getItem(position);
		Board board = new Board(new Piece[8][8]);
		Player white = new Player("White"), black = new Player("Black");
		Chess game = new Chess(white, black);
		board.loadPlayer(white);
		board.loadPlayer(black);
		
		Intent intent = new Intent(this, Game.class);
		intent.putExtra(Constants.EXTRA_HISTORY, chosen);
		intent.putExtra(Constants.EXTRA_BOARD, board);
		intent.putExtra(Constants.EXTRA_CHESS, game);
		intent.putExtra(Constants.EXTRA_W_PLAYER, white);
		intent.putExtra(Constants.EXTRA_B_PLAYER, black);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history_selection, menu);
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
