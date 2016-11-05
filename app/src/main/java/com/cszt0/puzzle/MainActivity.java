package com.cszt0.puzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.FileNotFoundException;

public class MainActivity extends Activity implements PuzzleView.OnFinishListener,SeekBar.OnSeekBarChangeListener
{
	PuzzleView puzzle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		puzzle = (PuzzleView) findViewById(R.id.puzzleview);
		puzzle.setOnFinishListener(this);
	}

	@Override
	public void onFinish() {
		Toast.makeText(this, R.string.finish, 0).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		new MenuInflater(this).inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
			default:
				return false;
			case R.id.restart:
				puzzle.restartGame();
				break;
			case R.id.picture:
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, 1);
				break;
			case R.id.difficulty:
				SeekBar seekbar = new SeekBar(this);
				seekbar.setMax(8);
				seekbar.setProgress(puzzle.getSegmentationNumber() - 2);
				seekbar.setOnSeekBarChangeListener(this);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(seekbar);
				builder.show();
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			try {
				puzzle.setBitmapSource(BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData())));
			} catch (FileNotFoundException e) {}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar p1) {
	}

	@Override
	public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
		puzzle.setSegmentationNumber(p2 + 2);
	}

	@Override
	public void onStopTrackingTouch(SeekBar p1) {
	}
}
