package edu.uci.CS122B.QuizApp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DbAdapter extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "quiz";
	private static final int DATABASE_VERSION = 1;
	private static final String moviesSchema = "CREATE TABLE \"movies\" "
			+ "(\"_id\" INTEGER PRIMARY KEY  NOT NULL , "
			+ "\"title\" VARCHAR NOT NULL , " + "\"year\" INTEGER NOT NULL , "
			+ "\"director\" VARCHAR NOT NULL , " + "\"banner_url\" VARCHAR, "
			+ "\"trailer_url\" VARCHAR) ";
	private static final String starsSchema = "CREATE TABLE \"stars\" "
			+ "(\"_id\" INTEGER PRIMARY KEY  NOT NULL , "
			+ "\"first_name\" VARCHAR NOT NULL , "
			+ "\"last_name\" VARCHAR NOT NULL , "
			+ "\"dob\" DATETIME DEFAULT null, " + "\"photo_url\" VARCHAR)";
	private static final String starsInMoviesSchema = "CREATE TABLE \"stars_in_movies\" "
			+ "(\"star_id\" INTEGER NOT NULL , "
			+ "\"movie_id\" INTEGER NOT NULL , "
			+ "PRIMARY KEY (\"star_id\", \"movie_id\"))";
	private static final String statsSchema = "CREATE TABLE \"stats\" "
			+ "(\"_id\" INTEGER PRIMARY KEY NOT NULL, "
			+ "\"questions_correct\" INTEGER NOT NULL, "
			+ "\"questions_asked\" INTEGER NOT NULL, "
			+ "\"time_elapsed\" INTEGER NOT NULL, "
			+ "\"date_taken\" DATETIME NOT NULL)";
	private static final String[] tables = { "stars", "movies",
			"stars_in_movies", "stats" };

	private SQLiteDatabase mDb;
	private Context mContext;

	public DbAdapter(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = ctx;
		this.mDb = getWritableDatabase();
		if (!allTablesPresent())
			onUpgrade(mDb, 0, 0);

	}

	private boolean allTablesPresent() {
		Set<String> t = new HashSet<String>(Arrays.asList(tables));
		Cursor tablesInDB = mDb.query("sqlite_master", new String[] { "name" },
				"type = \"table\" AND name != \"android_metadata\"", null,
				null, null, null);
		if (!tablesInDB.moveToFirst())
			return false;
		do
			if (!t.remove(tablesInDB.getString(0)))
				return false;
		while (tablesInDB.moveToNext());
		return (t.isEmpty()) ? true : false;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CSVReader read;
		SQLiteStatement stmt;
		db.beginTransaction();
		try {
			db.execSQL(moviesSchema);
			stmt = db.compileStatement("INSERT INTO movies "
					+ "(_id, title, year, director, banner_url, trailer_url ) "
					+ "VALUES(?,?,?,?,?,?)");
			read = new CSVReader(mContext.getAssets().open("movies.csv"));
			while (read.next()) {
				String[] line = read.getLine();
				stmt.bindLong(1, Long.parseLong(line[0]));
				stmt.bindString(2, line[1]);
				stmt.bindString(3, line[2]);
				stmt.bindString(4, line[3]);
				stmt.bindString(5, line[4]);
				stmt.bindString(6, line[5]);
				stmt.executeInsert();
			}

			db.execSQL(starsSchema);
			stmt = db.compileStatement("INSERT INTO stars "
					+ "(_id, first_name, last_name, dob, photo_url ) "
					+ "VALUES(?,?,?,?,?)");
			read = new CSVReader(mContext.getAssets().open("stars.csv"));
			while (read.next()) {
				String[] line = read.getLine();
				stmt.bindLong(1, Long.parseLong(line[0]));
				stmt.bindString(2, line[1]);
				stmt.bindString(3, line[2]);
				stmt.bindString(4, line[3]);
				stmt.bindString(5, line[4]);
				stmt.executeInsert();
			}

			db.execSQL(starsInMoviesSchema);
			stmt = db.compileStatement("INSERT INTO stars_in_movies "
					+ "(star_id, movie_id ) " + "VALUES(?,?)");
			read = new CSVReader(mContext.getAssets().open(
					"stars_in_movies.csv"));
			while (read.next()) {
				String[] line = read.getLine();
				stmt.bindLong(1, Long.parseLong(line[0]));
				stmt.bindLong(2, Long.parseLong(line[1]));
				stmt.executeInsert();
			}

			db.execSQL(statsSchema);

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		for (String table : tables)
			db.execSQL("DROP TABLE IF EXISTS " + table);
		onCreate(db);
	}

	public Cursor fetchRandomMovie() {
		return mDb.query(true, "movies", new String[] { "_id", "title", "year",
				"director" }, null, null, null, null, "RANDOM()", "1");
	}

	public Cursor fetchRandomStar() {
		return mDb.query(true, "stars", new String[] { "_id",
				"first_name || ' ' || last_name AS name", "first_name",
				"last_name" }, null, null, null, null, "RANDOM()", "1");
	}

	public Cursor fetchRandomStars(String first_name, String last_name) {
		return mDb.query(true, "stars",
				new String[] { "first_name || ' ' || last_name AS name" },
				"first_name <> ? AND last_name <> ?", new String[] {
						first_name, last_name }, null, null, "RANDOM()", "3");
	}

	public Cursor fetchRandomDirector(String star_id) {
		return mDb.query(true, "movies m, stars s, stars_in_movies sim",
				new String[] { "m.director" },
				"m._id = sim.movie_id AND s._id = sim.star_id AND s._id = ?",
				new String[] { star_id }, null, null, "RANDOM()", "1");
	}

	public Cursor fetchRandomDirectors(String director) {
		return mDb.query(true, "movies", new String[] { "director" },
				"director <> ? AND trim(director) <> ''",
				new String[] { director }, null, null, "RANDOM()", "3");
	}

	public Cursor fetchRandomYears(String year) {
		return mDb.query(true, "movies", new String[] { "year" }, "year <> ?",
				new String[] { year }, null, null, "RANDOM()", "3");
	}

	public Cursor fetchRandomMovieAndStar() {
		return mDb
				.query(true,
						"movies, stars, stars_in_movies",
						new String[] { "movies._id", "title", "year",
								"director",
								"first_name || ' ' || last_name AS name" },
						"movies._id = stars_in_movies.movie_id AND stars._id = stars_in_movies.star_id AND trim(director) <> '' AND trim(name) <> ''",
						null, null, null, "RANDOM()", "1");
	}

	public Cursor fetchRandomStarsNotInMovie(String movie_id) {
		return mDb.rawQuery(""
				+ "SELECT first_name || ' ' || last_name AS name "
				+ "FROM stars WHERE NOT EXISTS ( " + "SELECT * "
				+ "FROM stars_in_movies "
				+ "WHERE stars._id = stars_in_movies.star_id "
				+ "AND stars_in_movies.movie_id = ?" + " )"
				+ "ORDER BY RANDOM() " + "LIMIT 3", new String[] { movie_id });

		// return mDb.rawQuery("" +
		// "SELECT DISTINCT first_name || ' ' || last_name AS name " +
		// "FROM stars " +
		// "WHERE first_name NOT IN ( " +
		// "SELECT DISTINCT s.first_name " +
		// "FROM stars s, stars_in_movies sim " +
		// "WHERE s._id = sim.star_id " +
		// "AND sim.movie_id = ?" +
		// " ) " +
		// "AND last_name NOT IN ( " +
		// "SELECT DISTINCT s.last_name " +
		// "FROM stars s, stars_in_movies sim " +
		// "WHERE s._id = sim.star_id " +
		// "AND sim.movie_id = ?" +
		// " ) " +
		// "ORDER BY RANDOM() " +
		// "LIMIT 3", new String[] { movie_id });
	}

	public Cursor fetchRandomMovieWithFourStars() {
		return mDb
				.query(true,
						"stars, movies, stars_in_movies",
						new String[] { "movies.title", "movies._id" },
						"stars._id = stars_in_movies.star_id AND movies._id = stars_in_movies.movie_id",
						null, "movies.title, movies._id", "COUNT(*) >= 4",
						"RANDOM()", "1");
	}

	public Cursor fetchRandomMovieWithoutStar() {
		return mDb.query(true, "movies m, stars s, stars_in_movies sim",
				new String[] { "m._id", "m.title" },
				"m._id = sim.movie_id AND s._id = sim.star_id", null, "m._id",
				"COUNT(m._id) >= 3", "RANDOM()", "1");
	}

	public Cursor fetchRandomStarInMovie(String movie_id) {
		return mDb
				.query(true,
						"stars, stars_in_movies",
						new String[] {
								"first_name || ' ' || last_name AS name",
								"first_name", "last_name", "stars._id" },
						"stars._id = stars_in_movies.star_id AND stars_in_movies.movie_id = ?",
						new String[] { movie_id }, null, null, "RANDOM()", "1");
	}

	public Cursor fetchRandomStarNotInMovie(String movie_id) {
		return mDb
				.query(true,
						"stars, stars_in_movies",
						new String[] { "first_name || ' ' || last_name AS name" },
						"stars._id = stars_in_movies.star_id AND trim(name) <> '' AND stars_in_movies.movie_id <> ?",
						new String[] { movie_id }, null, null, "RANDOM()", "1");
	}

	public Cursor fetchThreeRandomStarsInMovie(String movie_id) {
		return mDb
				.query(true,
						"stars, stars_in_movies",
						new String[] { "first_name || ' ' || last_name AS name" },
						"stars._id = stars_in_movies.star_id AND stars_in_movies.movie_id = ?",
						new String[] { movie_id }, null, null, "RANDOM()", "3");
	}

	public Cursor fetchThreeRandomStarsNotInMovie(String movie_id) {
		return mDb
				.query(true,
						"stars, stars_in_movies",
						new String[] { "first_name || ' ' || last_name AS name" },
						"stars._id = stars_in_movies.star_id AND stars_in_movies.movie_id <> ?",
						new String[] { movie_id }, null, null, "RANDOM()", "3");
	}

	public Cursor fetchRandomMovieWithTwoStars() {
		// return mDb
		// .query(true,
		// "movies, stars, stars_in_movies",
		// new String[] { "movies._id", "movies.title" },
		// "movies._id = stars_in_movies.movie_id AND stars._id = stars_in_movies.star_id",
		// null, "movies._id", "COUNT(movies._id) >= 2",
		// "RANDOM()", "1");
		return mDb
				.query(true,
						"stars, movies, stars_in_movies",
						new String[] { "movies.title", "movies._id" },
						"stars._id = stars_in_movies.star_id AND movies._id = stars_in_movies.movie_id",
						null, "movies.title, movies._id", "COUNT(*) >= 2",
						"RANDOM()", "1");
	}

	public Cursor fetchTwoRandomStarsInMovie(String movie_id) {
		return mDb
				.rawQuery(
						"SELECT DISTINCT first_name || ' ' || last_name AS name "
								+ "FROM stars, stars_in_movies "
								+ "WHERE stars._id = stars_in_movies.star_id AND stars_in_movies.movie_id = ? "
								+ "ORDER BY RANDOM() LIMIT 2",
						new String[] { movie_id });
		// return mDb
		// .query(true,
		// "stars, stars_in_movies",
		// new String[] { "first_name || ' ' || last_name AS name" },
		// "stars._id = stars_in_movies.star_id AND stars_in_movies.movie_id = ?",
		// new String[] { movie_id }, null, null, "RANDOM()", "2");
	}

	public Cursor fetchThreeRandomMovies(String movie_id) {
		return mDb.query(true, "movies", new String[] { "title" }, "_id <> ?",
				new String[] { movie_id }, null, null, "RANDOM()", "3");
	}

	public Cursor fetchRandomStarDirectedByThreeDirectors() {
		return mDb
				.rawQuery(
						""
								+ "SELECT _id, name, first_name, last_name, COUNT(*) "
								+ "FROM ( "
								+ "SELECT s._id, s.first_name || ' ' || s.last_name AS name, s.first_name, s.last_name "
								+ "FROM stars s, stars_in_movies sim, movies m "
								+ "WHERE m._id = sim.movie_id "
								+ "AND s._id = sim.star_id "
								+ "AND trim(name) <> '' "
								+ "GROUP BY name, m.director "
								+ "ORDER BY name, m.director" + ") "
								+ "GROUP BY name " + "HAVING COUNT(*) >= 3 "
								+ "ORDER BY RANDOM() " + "LIMIT 1",
						new String[] {});
	}

	public Cursor fetchRandomDirectorWhoDidNotDirectStar(String first_name,
			String last_name) {
		return mDb
				.query(true,
						"stars s, movies m, stars_in_movies sim",
						new String[] { "m.director" },
						"s._id = sim.star_id AND m._id = sim.movie_id AND s.first_name <> ? AND s.last_name <> ?",
						new String[] { first_name, last_name }, null, null,
						"RANDOM()", "1");
	}

	public Cursor fetchRandomDirectorsWhoDirectedStar(String first_name,
			String last_name) {
		return mDb
				.query(true,
						"movies m, stars_in_movies sim, stars s",
						new String[] { "m.director" },
						"m._id = sim.movie_id AND s._id = sim.star_id AND s.first_name = ? AND s.last_name = ?",
						new String[] { first_name, last_name }, null, null,
						"RANDOM()", "3");

	}

	public Cursor fetchRandomStarWhoAppearsInTwoMovies() {
		return mDb.query(true, "stars s, stars_in_movies sim, movies m",
				new String[] { "s.first_name || ' ' || s.last_name AS name",
						"s.first_name", "s.last_name" },
				"s._id = sim.star_id AND m._id = sim.movie_id ", null, "name",
				"COUNT( name ) >= 2", "RANDOM()", "1");
	}

	public Cursor fetchRandomStarsInSameMovieWithStar(String movie_id,
			String star_id) {
		return mDb
				.query(true,
						"stars, stars_in_movies",
						new String[] {
								"first_name || ' ' || last_name AS name",
								"first_name", "last_name", "_id" },
						"stars._id = stars_in_movies.star_id AND stars_in_movies.movie_id = ? AND stars_in_movies.star_id <> ?",
						new String[] { movie_id, star_id }, null, null,
						"RANDOM()", "3");

	}

	public Cursor fetchTwoRandomMoviesStarring(String first_name,
			String last_name) {
		return mDb
				.query(true,
						"movies m, stars s, stars_in_movies sim",
						new String[] { "m.title" },
						"m._id = sim.movie_id AND s._id = sim.star_id AND s.first_name = ? AND s.last_name = ?",
						new String[] { first_name, last_name }, null, null,
						"RANDOM()", "2");
	}

	public Cursor fetchRandomStarNotInSameMovieWithStar(String star_id) {
		return mDb
				.rawQuery(
						""
								+ "SELECT DISTINCT s.first_name || ' ' || s.last_name AS name, s._id "
								+ "FROM stars s, movies m, stars_in_movies sim "
								+ "WHERE s._id = sim.star_id "
								+ "AND m._id = sim.movie_id "
								+ "AND m._id NOT IN ( "
								+ "SELECT m1._id "
								+ "FROM stars s1, movies m1, stars_in_movies sim1 "
								+ "WHERE s1._id = sim1.star_id "
								+ "AND m1._id = sim1.movie_id "
								+ "AND s1._id = ?" + " ) "
								+ "ORDER BY RANDOM() " + "LIMIT 1",
						new String[] { star_id });
	}

	public Cursor fetchRandomStarsInSameMovieWithStar(String star_id) {
		return mDb
				.rawQuery(
						""
								+ "SELECT DISTINCT s.first_name || ' ' || s.last_name AS name, s._id "
								+ "FROM stars s, movies m, stars_in_movies sim "
								+ "WHERE s._id = sim.star_id "
								+ "AND m._id = sim.movie_id "
								+ "AND s._id <> ? "
								+ "AND m._id IN ( "
								+ "SELECT m1._id "
								+ "FROM stars s1, movies m1, stars_in_movies sim1 "
								+ "WHERE s1._id = sim1.star_id "
								+ "AND m1._id = sim1.movie_id "
								+ "AND s1._id = ?" + " ) "
								+ "ORDER BY RANDOM() " + "LIMIT 3",
						new String[] { star_id, star_id });
	}

	public Cursor fetchAll() {
		return mDb.query("movies", new String[] { "title" }, null, null, null,
				null, null);
	}

	public boolean insertQuizStat(int correct, int asked, int seconds_taken,
			String date) {
		SQLiteStatement stmt = mDb
				.compileStatement("INSERT INTO stats "
						+ "(questions_correct, questions_asked, time_elapsed, date_taken)"
						+ "VALUES (?,?,?,?)");
		stmt.bindLong(1, correct);
		stmt.bindLong(2, asked);
		stmt.bindLong(3, seconds_taken);
		stmt.bindString(4, date);
		return (stmt.executeInsert() != -1);
	}

	public Cursor fetchStats() {
		return mDb.query("stats", new String[] {
				"COUNT(questions_asked) AS total_quizzes",
				"SUM(questions_correct) AS total_correct",
				"SUM(questions_asked) AS total_asked",
				"SUM(time_elapsed) AS total_time" }, null, null, null, null,
				null);

	}

	public Cursor fetchLastStats() {
		return mDb.query("stats",
				new String[] { "COUNT(questions_asked) AS total_quizzes",
						"questions_correct AS total_correct",
						"questions_asked AS total_asked",
						"time_elapsed AS total_time" }, null, null, null, null,
				"_id DESC", "1");

	}

}
