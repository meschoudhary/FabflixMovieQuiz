package edu.uci.CS122B.QuizApp;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import android.app.Application;
import android.os.Bundle;

public class App extends Application {
	private static final int size = 10;
	public BlockingQueue<Bundle> queue = new ArrayBlockingQueue<Bundle>(size);

}
