package edu.uci.CS122B.QuizApp;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

public class QuestionGenerator extends Thread {
	Context ctx;
	BlockingQueue<Bundle> queue;
	public volatile boolean done = false;

	private String[] questions = {
			"Who directed the movie <u>%s</u>?",
			"When was the movie <u>%s</u> released?",
			"Which star was in the movie <u>%s</u>?",
			"Which star was not in the movie <u>%s</u>?",
			"In which movie the stars <u>%s</u> and <u>%s</u> appear together?",
			"Who directed the star <u>%s</u>?",
			"Who did not direct the star <u>%s</u>?",
			"Which star appears in both movies <u>%s</u> and <u>%s</u>?",
			"Which star did not appear in the same movie with the star <u>%s</u>?",
			"Who directed the star <u>%s</u> in year <u>%s</u>?" };

	private String[] answers;
	private String question;
	private int answer;

	public QuestionGenerator(BlockingQueue<Bundle> q, Context c) {
		queue = q;
		ctx = c;

	}

	@Override
	public void run() {
		while (!done) {
			DbAdapter db = new DbAdapter(ctx);
			Random random = new Random();
			int randomQuestion = random.nextInt(questions.length);
			question = new String();
			answers = new String[4];
			answer = random.nextInt(answers.length);

			Cursor cursor;
			String movie_id = new String();
			String star_id = new String();
			switch (randomQuestion) {
			case 0:
				// Who directed the movie X?
				cursor = db.fetchRandomMovie();
				if (cursor.moveToFirst()) {
					question = String.format(questions[randomQuestion], cursor
							.getString(1).trim());
					answers[answer] = cursor.getString(3).trim();
					fillWrongButtons(answer,
							db.fetchRandomDirectors(cursor.getString(3).trim()));
				}
				cursor.close();
				break;
			case 1:
				// When was the movie X released?
				cursor = db.fetchRandomMovie();
				if (cursor.moveToFirst()) {
					question = String.format(questions[randomQuestion], cursor
							.getString(1).trim());
					answers[answer] = cursor.getString(2).trim();
					fillWrongButtons(answer,
							db.fetchRandomYears(cursor.getString(2).trim()));
				}
				cursor.close();
				break;
			case 2:
				// Which star was in the movie X?
				cursor = db.fetchRandomMovieAndStar();
				if (cursor.moveToFirst()) {
					question = String.format(questions[randomQuestion], cursor
							.getString(1).trim());
					answers[answer] = cursor.getString(4).trim();
					fillWrongButtons(answer,
							db.fetchRandomStarsNotInMovie(cursor.getString(0)
									.trim()));
				}
				cursor.close();
				break;
			case 3:
				// Which star was not in the movie X?
				cursor = db.fetchRandomMovieWithoutStar();
				if (cursor.moveToFirst()) {
					movie_id = cursor.getString(0).trim();
					question = String.format(questions[randomQuestion], cursor
							.getString(1).trim());
				}
				cursor = db.fetchRandomStarNotInMovie(movie_id);
				if (cursor.moveToFirst()) {
					answers[answer] = cursor.getString(0).trim();
					fillWrongButtons(answer,
							db.fetchThreeRandomStarsInMovie(movie_id));
				}
				cursor.close();
				break;
			case 4:
				// In which movie the stars %s and %s appear together?
				String[] stars = new String[2];
				cursor = db.fetchRandomMovieWithTwoStars();
				if (cursor.moveToFirst()) {
					movie_id = cursor.getString(1).trim();
					String movie_title = cursor.getString(0).trim();
					cursor = db.fetchTwoRandomStarsInMovie(movie_id);
					if (cursor.moveToFirst()) {
						stars[0] = cursor.getString(0).trim();
					}
					if (cursor.moveToNext()) {
						stars[1] = cursor.getString(0).trim();
					}
					question = String.format(questions[randomQuestion],
							stars[0], stars[1]);
					answers[answer] = movie_title;
					fillWrongButtons(answer,
							db.fetchThreeRandomMovies(movie_id));
				}
				cursor.close();
				break;
			case 5:
				// Who directed the star %s?
				cursor = db.fetchRandomMovieAndStar();
				if (cursor.moveToFirst()) {
					question = String.format(questions[randomQuestion], cursor
							.getString(4).trim());
					answers[answer] = cursor.getString(3).trim();
					fillWrongButtons(answer,
							db.fetchRandomDirectors(cursor.getString(3).trim()));
				}
				cursor.close();

				/*
				 * // Who directed the star %s? cursor = db.fetchRandomStar();
				 * if (cursor.moveToFirst()) { question =
				 * String.format(questions[randomQuestion], cursor
				 * .getString(1).trim()); star_id = cursor.getString(0).trim();
				 * cursor = db.fetchRandomDirector(star_id); String director =
				 * new String(); if (cursor.moveToFirst()) { director =
				 * cursor.getString(0).trim(); answers[answer] = director; }
				 * fillWrongButtons(answer, db.fetchRandomDirectors(director));
				 * } cursor.close();
				 */

				break;
			case 6:
				// Who did not direct the star %s?
				cursor = db.fetchRandomStarDirectedByThreeDirectors();
				if (cursor.moveToFirst()) {
					question = String.format(questions[randomQuestion], cursor
							.getString(1).trim());
					String first_name = cursor.getString(2).trim();
					String last_name = cursor.getString(3).trim();
					cursor = db.fetchRandomDirectorWhoDidNotDirectStar(
							first_name, last_name);
					if (cursor.moveToFirst()) {
						answers[answer] = cursor.getString(0).trim();
					}
					fillWrongButtons(answer,
							db.fetchRandomDirectorsWhoDirectedStar(first_name,
									last_name));
				}
				cursor.close();
				break;
			case 7:
				// Which star appears in both movies %s and %s?
				String[] movies = new String[2];
				String name = new String();
				String first_name = new String();
				String last_name = new String();
				cursor = db.fetchRandomStarWhoAppearsInTwoMovies();
				if (cursor.moveToFirst()) {
					name = cursor.getString(0).trim();
					first_name = cursor.getString(1).trim();
					last_name = cursor.getString(2).trim();
					cursor = db.fetchTwoRandomMoviesStarring(first_name,
							last_name);
					if (cursor.moveToFirst()) {
						movies[0] = cursor.getString(0).trim();
					}
					if (cursor.moveToNext()) {
						movies[1] = cursor.getString(0).trim();
					}
					question = String.format(questions[randomQuestion],
							movies[0], movies[1]);
					answers[answer] = name;
					fillWrongButtons(answer,
							db.fetchRandomStars(first_name, last_name));
				}
				cursor.close();
				break;
			case 8:
				// Which star did not appear in the same movie with the star %s?
				cursor = db.fetchRandomMovieWithFourStars();
				if (cursor.moveToFirst()) {
					movie_id = cursor.getString(1).trim();
					cursor = db.fetchRandomStarInMovie(movie_id);
					if (cursor.moveToFirst()) {
						star_id = cursor.getString(3).trim();
						question = String.format(questions[randomQuestion],
								cursor.getString(0).trim());
					}
					cursor = db.fetchRandomStarNotInSameMovieWithStar(star_id);
					if (cursor.moveToFirst()) {
						answers[answer] = cursor.getString(0).trim();
					}
					fillWrongButtons(answer,
							db.fetchRandomStarsInSameMovieWithStar(star_id));
				}
				cursor.close();
				break;
			case 9:
				// Who directed the star %s in year %s?
				cursor = db.fetchRandomMovieAndStar();
				if (cursor.moveToFirst()) {
					question = String.format(questions[randomQuestion], cursor
							.getString(4).trim(), cursor.getString(2).trim());
					answers[answer] = cursor.getString(3).trim();
					fillWrongButtons(answer,
							db.fetchRandomDirectors(cursor.getString(3).trim()));
				}
				cursor.close();
				break;
			}
			db.close();
			Bundle b = new Bundle();
			b.putString("question", question);
			b.putStringArray("buttons", answers);
			b.putInt("answer", answer);
			try {
				queue.put(b);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void fillWrongButtons(int correct, Cursor cursor) {
		if (cursor.moveToFirst()) {
			for (int i = 0; i < answers.length; i++) {
				if (i != correct) {
					answers[i] = cursor.getString(0);
					if (cursor.moveToNext())
						continue;
					else
						break;
				}
			}
		}
		cursor.close();
	}

}
