package edu.uci.CS122B.QuizApp;

import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class StatisticsActivity extends Activity {
	private String[] results = new String[6];
	private boolean allStats = true;
	private MediaPlayer mp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);

		getExtraData();

		if (savedInstanceState == null)
			getStats();
		else
			restoreStats(savedInstanceState);

		updateInterface();

		playQuizResultAudio();

		// this.findViewById(R.id.layoutBody).setOnClickListener(new
		// OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		// finish();
		// }
		// });

		this.findViewById(R.id.statisticsButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						finish();
					}
				});
	}

	private void playQuizResultAudio() {
		if (!allStats) {
			float score = Float.valueOf(results[0]);
			if (score == 100f) {
				mp = MediaPlayer.create(StatisticsActivity.this,
						R.raw.congratulations_perfect_score_lauren);
			} else if (90f <= score && score < 100f) {
				mp = MediaPlayer.create(StatisticsActivity.this,
						R.raw.great_job_lauren);
			} else if (80f <= score && score < 90f) {
				mp = MediaPlayer.create(StatisticsActivity.this,
						R.raw.good_job_lauren);
			} else if (70f <= score && score < 80f) {
				mp = MediaPlayer.create(StatisticsActivity.this,
						R.raw.not_bad_lauren);
			} else if (60f <= score && score < 70f) {
				mp = MediaPlayer.create(StatisticsActivity.this,
						R.raw.nice_try_lauren);
			} else if (score < 60f) {
				mp = MediaPlayer.create(StatisticsActivity.this,
						R.raw.better_luck_next_time_lauren);
			}
			mp.start();
		}
	}

	private void getExtraData() {
		Bundle b = getIntent().getExtras();
		allStats = true;
		if (b == null)
			allStats = true;
		else
			allStats = b.getBoolean("allStats");
	}

	private void getStats() {
		DbAdapter db = new DbAdapter(this);
		Cursor c;
		if (allStats)
			c = db.fetchStats();
		else
			c = db.fetchLastStats();
		c.moveToFirst();

		int quizzes = c.getInt(0);
		int correct = c.getInt(1);
		int asked = c.getInt(2);
		int time = c.getInt(3);
		c.close();
		db.close();

		float score = (asked != 0) ? ((float) correct) / ((float) asked) * 100
				: 0;
		int incorrect = asked - correct;
		float average = (asked != 0) ? ((float) time) / ((float) asked) : 0;

		results[0] = String.valueOf(roundFloatToDecimalPlaces(score, 1));
		results[1] = String.valueOf(quizzes);
		results[2] = String.valueOf(correct);
		results[3] = String.valueOf(incorrect);
		results[4] = String.valueOf(average);
		results[5] = (allStats) ? this.getString(R.string.total_stat) : this
				.getString(R.string.current_stat);
	}

	private float roundFloatToDecimalPlaces(float value, int places) {
		float p = (float) Math.pow(10, places);
		return (float) Math.round(value *= p) / p;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putStringArray("results", results);
	}

	private void restoreStats(Bundle s) {
		results = s.getStringArray("results");
	}

	private void updateInterface() {
		if (allStats) {
			((TextView) this.findViewById(R.id.statsTitleView))
					.setText(results[5]);

			((TextView) this.findViewById(R.id.row1Label)).setText(this
					.getString(R.string.score));
			((TextView) this.findViewById(R.id.row1Result)).setText(String
					.format("%s%%", results[0]));

			((TextView) this.findViewById(R.id.row2Label)).setText(this
					.getString(R.string.taken));
			((TextView) this.findViewById(R.id.row2Result)).setText(results[1]);

			((TextView) this.findViewById(R.id.row3Label)).setText(this
					.getString(R.string.correct));
			((TextView) this.findViewById(R.id.row3Result)).setText(results[2]);

			((TextView) this.findViewById(R.id.row4Label)).setText(this
					.getString(R.string.incorrect));
			((TextView) this.findViewById(R.id.row4Result)).setText(results[3]);

			((TextView) this.findViewById(R.id.row5Label)).setText(this
					.getString(R.string.timePerQuestion));
			((TextView) this.findViewById(R.id.row5Result)).setText(String
					.format("%.1f seconds", Float.valueOf(results[4])));
		} else {
			((TextView) this.findViewById(R.id.statsTitleView))
					.setText(results[5]);

			((TextView) this.findViewById(R.id.row1Label)).setText(this
					.getString(R.string.score));
			((TextView) this.findViewById(R.id.row1Result)).setText(String
					.format("%s%%", results[0]));

			((TextView) this.findViewById(R.id.row2Label)).setText(this
					.getString(R.string.correct));
			((TextView) this.findViewById(R.id.row2Result)).setText(results[2]);

			((TextView) this.findViewById(R.id.row3Label)).setText(this
					.getString(R.string.incorrect));
			((TextView) this.findViewById(R.id.row3Result)).setText(results[3]);

			((TextView) this.findViewById(R.id.row4Label)).setText(this
					.getString(R.string.timePerQuestion));
			((TextView) this.findViewById(R.id.row4Result)).setText(String
					.format("%.1f seconds", Float.valueOf(results[4])));

			((TextView) this.findViewById(R.id.row5Label)).setText(this
					.getString(R.string.empty));
			((TextView) this.findViewById(R.id.row5Result)).setText(this
					.getString(R.string.empty));
		}
	}
}
