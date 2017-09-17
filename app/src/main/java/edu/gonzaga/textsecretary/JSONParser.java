package edu.gonzaga.textsecretary;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

// inspired by: http://danielnugent.blogspot.com/2015/06/updated-jsonparser-with.html
public class JSONParser {

	private final static String charset = "UTF-8";
	private HttpURLConnection connection;
	private StringBuilder result;
	private JSONObject json = null;

	public JSONObject makeHttpRequest(String url, String method,
									  HashMap<String, String> params) {

		StringBuilder sbParams = new StringBuilder();
		int i = 0;
		for (String key : params.keySet()) {
			try {
				if (i != 0){
					sbParams.append("&");
				}
				sbParams.append(key).append("=")
						.append(URLEncoder.encode(params.get(key), charset));

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			i++;
		}

		try {

			if (method.equals("POST")) {
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.setReadTimeout(10000);

				String paramsString = sbParams.toString();

				DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
				outputStream.writeBytes(paramsString);
				outputStream.flush();
				outputStream.close();

			}
			else if (method.equals("GET")) {
				connection.setDoOutput(false);
				connection.setRequestMethod("GET");
			}

			URL urlObj;
			urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setConnectTimeout(15000);
			connection.connect();

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			InputStream inputStream = new BufferedInputStream(connection.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}

			Log.d("JSON Parser", "result: " + result.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}

		connection.disconnect();

		try {
			json = new JSONObject(result.toString());
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		return json;
	}
}