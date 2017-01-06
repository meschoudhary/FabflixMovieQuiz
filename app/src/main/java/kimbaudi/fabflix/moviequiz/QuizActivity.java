package kimbaudi.fabflix.moviequiz;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
//import java.util.HashMap;
//import java.util.Random;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.content.Context;
//import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends Activity {

	private static final long duration = 3 * 60 * 1000; // Standard duration
	// // private static final long duration = 12 * 1000; // Shorter duration
	// used
	// private static final long duration = 5 * 60 * 1000; // Longer duration
	// used
	private volatile long elapsed;
	private volatile boolean running;
	private Handler handler = new Handler();
	private Toast toast;
	private ImageView toastImage;
	private TextView toastText;
	private SoundPool soundPool;
	private SparseIntArray soundsMap;
	private int correct_sound = 1;
	private int error_sound = 2;
	private String[] answers;
	private String question;
	private int answer;
	private int score;
	private int total;

	private BlockingQueue<Bundle> queue;
	Activity a = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quiz);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		//soundsMap = new HashMap<Integer, Integer>();
		soundsMap = new SparseIntArray();
		soundsMap.put(correct_sound,
				soundPool.load(this, R.raw.correct_sound, 1));
		soundsMap.put(error_sound, soundPool.load(this, R.raw.error_sound, 1));

		queue = ((App) getApplicationContext()).queue;
		if (savedInstanceState == null) {
			elapsed = 0;
			running = true;
			question = "";
			answers = new String[4];
			answer = -1;
			score = 0;
			total = 0;
			generateQuiz();
		} else {
			loadDataBundle(savedInstanceState);
			loadStateBundle(savedInstanceState);
		}

		a.runOnUiThread(new UpdateInterface());
		setQuizButtonOnClickListeners();

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
				(ViewGroup) findViewById(R.id.toast_layout_root));
		toastImage = (ImageView) layout.findViewById(R.id.image);
		toastText = (TextView) layout.findViewById(R.id.text);
		toast = new Toast(getApplicationContext());
		// toast.setGravity(Gravity.CENTER_VERTICAL, toast.getXOffset() / 2,
		// toast.getYOffset() / 2);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("elapsed", elapsed);
		outState.putBoolean("running", running);
		outState.putString("question", question);
		outState.putStringArray("buttons", answers);
		outState.putInt("answer", answer);
		outState.putInt("score", score);
		outState.putInt("total", total);
	}

	@Override
	protected void onResume() {
		super.onResume();
		running = true;
		handler.post(new UpdateTimer());
	}

	@Override
	protected void onPause() {
		super.onPause();
		running = false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED);
		finish();
	}

	private void loadDataBundle(Bundle b) {
		question = b.getString("question");
		answers = b.getStringArray("buttons");
		answer = b.getInt("answer");
	}

	private void loadStateBundle(Bundle b) {
		elapsed = b.getLong("elapsed");
		running = b.getBoolean("running");
		score = b.getInt("score");
		total = b.getInt("total");
	}

	private void saveStats() {
		DbAdapter db = new DbAdapter(this);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String today = f.format(Calendar.getInstance().getTime());
		db.insertQuizStat(score, total, (int) duration / 1000, today);
		db.close();
	}

	private void generateQuiz() {
		try {
			loadDataBundle(queue.take());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void displayQuiz() {
		((TextView) this.findViewById(R.id.scoreView)).setText(String.format(Locale.US,
				"Score: %d of %d", score, total));
		((TextView) this.findViewById(R.id.quizView)).setText(Html
				.fromHtml(question));
		((Button) this.findViewById(R.id.buttonA)).setText(answers[0]);
		((Button) this.findViewById(R.id.buttonB)).setText(answers[1]);
		((Button) this.findViewById(R.id.buttonC)).setText(answers[2]);
		((Button) this.findViewById(R.id.buttonD)).setText(answers[3]);
	}

	private void setQuizButtonOnClickListeners() {
		this.findViewById(R.id.buttonA)
				.setOnClickListener(new ButtonClickHelper(0));
		this.findViewById(R.id.buttonB)
				.setOnClickListener(new ButtonClickHelper(1));
		this.findViewById(R.id.buttonC)
				.setOnClickListener(new ButtonClickHelper(2));
		this.findViewById(R.id.buttonD)
				.setOnClickListener(new ButtonClickHelper(3));
	}

	private class ButtonClickHelper implements OnClickListener {
		private int id = -1;

		ButtonClickHelper(int i) {
			id = i;
		}

		@Override
		public void onClick(View v) {

			v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

			if (answer == id) {
				score++;
				playSound(correct_sound, 1.0f);
				toastImage.setImageResource(R.drawable.correct);
				toastText.setText(getResources().getString(R.string.correct_response));
				toast.setGravity(Gravity.CENTER_VERTICAL, -175, 75); // offset
																		// left
																		// bottom

			} else {
				playSound(error_sound, 1.0f);
				toastImage.setImageResource(R.drawable.incorrect);
				toastText.setText(getResources().getString(R.string.incorrect_response));
				toast.setGravity(Gravity.CENTER_VERTICAL, 175, 75); // offset
																	// right
																	// bottom
			}
			total++;

			toast.show();

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					toast.cancel();
				}
			}, 10);

			generateQuiz();
			a.runOnUiThread(new UpdateInterface());
		}
	}

	private class UpdateTimer implements Runnable {
		@Override
		public void run() {
			if (!running) {
				handler.removeCallbacks(this);
				return;
			}
			elapsed += 1000;
			if (elapsed < duration) {
				long remaining = duration - elapsed;
				int seconds = (int) (remaining / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;

				((TextView) QuizActivity.this.findViewById(R.id.timerView))
						.setText(String.format(Locale.US, "Time Left: %d:%02d", minutes,
								seconds));
				if (running)
					handler.postDelayed(this, 1000);
				else
					handler.removeCallbacks(this);
			} else {
				handler.removeCallbacks(this);
				saveStats();
				setResult(RESULT_OK);
				finish();
			}
		}
	}

	private class UpdateInterface implements Runnable {
		@Override
		public void run() {
			displayQuiz();
		}
	}

	public void playSound(int sound, float fSpeed) {
		AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;
		soundPool.play(soundsMap.get(sound), volume, volume, 1, 0, fSpeed);
	}
}
