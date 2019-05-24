package ro.pub.cs.systems.eim.practicaltest02;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class PracticalTest02MainActivity extends AppCompatActivity {

	private EditText clientWordEditText, clientLengthEditText;
	private Button clientSumbitButton;
	private TextView clientTextView;

	private EditText serverPortEditText;
	private Button serverStartButton;
	private ServerThread serverThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);

		clientWordEditText = (EditText) findViewById(R.id.client_word_et);
		clientLengthEditText = (EditText) findViewById(R.id.client_length_et);
		clientSumbitButton = (Button) findViewById(R.id.client_submit_button);
		clientTextView = (TextView) findViewById(R.id.client_tv);
		serverPortEditText = (EditText) findViewById(R.id.server_port_number_et);
		serverStartButton = (Button) findViewById(R.id.server_start_button);

		clientSumbitButton.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("StaticFieldLeak")
			@Override
			public void onClick(View view) {
				String word = clientWordEditText.getText().toString();
				String length = clientLengthEditText.getText().toString();
				if (word.equals("") || length.equals("")) {
					Toast.makeText(PracticalTest02MainActivity.this, "both fields must be filled", Toast.LENGTH_SHORT).show();
					return;
				}

				if (serverThread == null) {
					Toast.makeText(PracticalTest02MainActivity.this, "Please start the server first", Toast.LENGTH_SHORT).show();
					return;
				}

				new AsyncTask<String, Void, String>() {
					@Override
					protected String doInBackground(String... strings) {
						String word = strings[0];
						String length = strings[1];
						try {
							Socket socket = new Socket("127.0.0.1", serverThread.getServerPort());
							PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
							BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							printWriter.write(word + " " + length);
							printWriter.close();
							StringBuilder result = new StringBuilder();
							String line;
							while((line = bufferedReader.readLine()) != null) {
								result.append(line);
							}
							bufferedReader.close();
							socket.close();
							return result.toString();
						} catch (IOException e) {
							e.printStackTrace();
//							Toast.makeText(PracticalTest02MainActivity.this, "An error occurred while talking to the server", Toast.LENGTH_SHORT).show();
						}
						return null;
					}

					@Override
					protected void onPostExecute(String s) {
						if (s != null) {
							clientTextView.setText(s);
						} else {
							Toast.makeText(PracticalTest02MainActivity.this, "An error occurred while talking to the server", Toast.LENGTH_SHORT).show();
						}
					}
				}.execute(word, length);


			}
		});

		serverStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String port = serverPortEditText.getText().toString();
				if (port.equals("")) {
					Toast.makeText(PracticalTest02MainActivity.this, "please set port", Toast.LENGTH_SHORT).show();
					return;
				}
				int portNo = Integer.parseInt(port);
				if (serverThread == null) {
					serverThread = new ServerThread(portNo);
					serverThread.startServer();
					Toast.makeText(PracticalTest02MainActivity.this, "Server started", Toast.LENGTH_SHORT).show();
					((Button) view).setText("Stop");
				} else {
					serverThread.stopServer();
					serverThread = null;
					Toast.makeText(PracticalTest02MainActivity.this, "Server stopped", Toast.LENGTH_SHORT).show();
					((Button) view).setText("Start");
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (serverThread != null) {
			serverThread.stopServer();
			serverThread = null;
		}
		super.onDestroy();
	}
}
