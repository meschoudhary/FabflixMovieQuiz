package edu.uci.CS122B.QuizApp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	public static final int QUIZ_START = 1;
	private Button startButton;
	private Button statsButton;
	private QuestionGenerator g = null;
	private MediaPlayer mp;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startButton = (Button) this.findViewById(R.id.startQuizButton);
		startButton.setOnClickListener(new startListener());

		statsButton = (Button) this.findViewById(R.id.statisticsButton);
		statsButton.setOnClickListener(new statsListener());

		mp = MediaPlayer.create(MainActivity.this, R.raw.fof98);
		mp.setLooping(true);
		mp.start();

		// new DbAdapter(this); // Used to create the database if needed

		g = new QuestionGenerator(((App) getApplicationContext()).queue,
				getApplicationContext());
		g.start();

	}

	@Override
	protected void onPause() {
		super.onPause();
		mp.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mp.seekTo(0);
		mp.start();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        ((ImageView) this.findViewById(R.id.mainView)).setImageResource(R.drawable.splash_land);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        ((ImageView) this.findViewById(R.id.mainView)).setImageResource(R.drawable.splash);
	    }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case QUIZ_START:
			switch (resultCode) {
			case RESULT_OK:
				Intent intent = new Intent(MainActivity.this,
						StatisticsActivity.class);
				intent.putExtra("allStats", false);
				startActivity(intent);
				break;
			case RESULT_CANCELED:
				// Do nothing
				break;
			}

			break;
		}
	}

	private class startListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
			Intent intent = new Intent(MainActivity.this, QuizActivity.class);
			// startActivity(intent);
			startActivityForResult(intent, QUIZ_START);
		}
	}

	private class statsListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
			Intent intent = new Intent(MainActivity.this,
					StatisticsActivity.class);
			intent.putExtra("allStats", true);
			startActivity(intent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
		g.done = true;
	}
}