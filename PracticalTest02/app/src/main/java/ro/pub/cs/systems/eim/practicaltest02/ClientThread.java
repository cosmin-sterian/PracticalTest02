package ro.pub.cs.systems.eim.practicaltest02;

import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends Thread {

	private TextView clientTextView;
	private String word;
	private String length;
	private int port;

	public ClientThread(String word, String length, TextView clientTextView, int port) {
		this.word = word;
		this.length = length;
		this.clientTextView = clientTextView;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			Socket socket = new Socket("localhost", port);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
