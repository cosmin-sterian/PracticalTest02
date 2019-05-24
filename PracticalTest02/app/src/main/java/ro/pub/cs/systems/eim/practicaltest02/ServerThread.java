package ro.pub.cs.systems.eim.practicaltest02;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

public class ServerThread extends Thread {

    private boolean isRunning;

    private ServerSocket serverSocket;
    private int serverPort;
//    private TextView clientTextView;

    public ServerThread(int serverPort) {
        this.serverPort = serverPort;
//        this.clientTextView = clientTextView;
    }

    public boolean getStatus() {
        return isRunning;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void startServer() {
        isRunning = true;
        start();
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.v(Contract.TAG, "startServer() method invoked " + serverSocket);
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Log.v(Contract.TAG, "stopServer() method invoked");
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverPort);
            while (isRunning) {
            	Log.v(Contract.TAG, "Server Socket: " + serverSocket + " " + serverSocket.isBound());
                Socket socket = serverSocket.accept();
                Log.v(Contract.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());

                // TODO exercise 5c
                // simulate the fact the communication routine between the server and the client takes 3 seconds

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder input = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) {
                    input.append(line);
                }
//                br.close();
                Log.i(Contract.TAG, "server input: " + input);
                String[] words = input.toString().split(" ");

                String result = new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        HttpURLConnection httpURLConnection = null;
                        String word = params[0];
                        String length = params[1];
                        try {
                            URL url = new URL(Contract.BASE_URL + Contract.ANAGRAM_QUERY + word + Contract.MIN_LETTERS_QUERY + length);
                            URLConnection urlConnection = url.openConnection();
                            if (urlConnection instanceof HttpURLConnection) {
                                httpURLConnection = (HttpURLConnection) urlConnection;
                                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                StringBuilder result = new StringBuilder();
                                String currentLine;
                                while ((currentLine = br.readLine()) != null) {
                                    result.append(currentLine);
                                }
                                httpURLConnection.disconnect();
                                return result.toString();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        }
                        return null;
                    }

//                    @Override
//                    protected void onPostExecute(String s) {
//                        if (s == null) {
//                            return;
//                        }
//                        Log.i(Contract.TAG, "result: " + s);
//                        clientTextView.setText(s);
//                    }
                }.execute(words[0], words[1]).get();

                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                printWriter.println(result);
//                printWriter.close();

//                sleep(3000);

//                socket.shutdownOutput();
//                socket.close();
                Log.v(Contract.TAG, "Connection closed");

                // TODO exercise 5d
                // move the communication routine between the server and the client on a separate thread (each)

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }
}
