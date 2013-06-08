package umd.edu.myumdwidget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Reader extends Activity{
	private static final String FILENAME = "login.txt";
	private static final String TAG = MainActivity.class.getName();

	public String[] readLoginDetails(){

		String login[] = new String[2];	

		File dir = Environment.getExternalStorageDirectory();
		//File yourFile = new File(dir, "path/to/the/file/inside/the/sdcard.ext");

		//Get the text file
		File file = new File(dir,"login.txt");
		// i have kept text.txt in the sd-card

		if(file.exists())   // check if file exist
		{

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				login[0] =  br.readLine();
				login[1] = br.readLine();

			}
			catch (IOException e) {
				//You'll need to add proper error handling here
			}
		} else {
			return null;
		}
		return login;
	}

	public void writeLoginDetails(String user, String pass){
		String filename = "login.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream fos;
		byte[] data = new String(user+"\n"+pass).getBytes();
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// handle exception
		} catch (IOException e) {
			// handle exception
		}
	}
	
	public void writeSettingsDetails(int side, int plan, int frequency, int notif){
		
		String filename = "settings.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream fos;
		byte[] data = new String(side+"\n"+plan+"\n"+frequency+"\n"+notif).getBytes();
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// handle exception
		} catch (IOException e) {
			// handle exception
		}
	}
	
	public int[] readSettingsDetails(){
		int settings[] = new int[4];	

		File dir = Environment.getExternalStorageDirectory();
		//File yourFile = new File(dir, "path/to/the/file/inside/the/sdcard.ext");

		//Get the text file
		File file = new File(dir,"settings.txt");
		// i have kept text.txt in the sd-card

		if(file.exists())   // check if file exist
		{

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				settings[0] =  Integer.parseInt(br.readLine());
				settings[1] = Integer.parseInt(br.readLine());
				settings[2] = Integer.parseInt(br.readLine());
				settings[3] = Integer.parseInt(br.readLine());
			}
			catch (IOException e) {
				//You'll need to add proper error handling here
			}
		} else {
			return null;
		}
		return settings;
	}
}
