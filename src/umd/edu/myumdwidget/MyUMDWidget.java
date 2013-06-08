package umd.edu.myumdwidget;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Map;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.math.RoundingMode;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyUMDWidget extends AppWidgetProvider {	

	private static final int POINTS_OFFSET = 39;
	private static final int BUCKS_OFFSET = 29;
	private static final int EXPRESS_OFFSET = 26;
	private static final int NORTH = 0;
	private static final int SOUTH = 1;
	private static final int REGULAR = 0;
	private static final int PLUS = 1;
	private static final int RED = 2;
	public static String ACTION_WIDGET_RECEIVE = "ActionReceiverWidget";
	int side, plan;
	int counter = 0;
	String[] amounts;
	RemoteViews remoteViews;
	AppWidgetManager appWidgetManager;
	ComponentName thisWidget;
	int[] NorthReg = {532,466,400,334,268,202,136,74,0}, 
			NorthRed = {532,466,400,334,268,202,136,74,0}, 
			NorthPlus = {576,503,430,357,284,211,137,65,0}, 
			SouthReg = {589,515,441,367,293,219,245,71,0}, 
			SouthRed = {589,515,441,367,293,219,245,71,0}, 
			SouthPlus = {651,561,481,411,321,241,161,81,0};
	Context context;
	int[] ids;

	//Called when the widget is created and at every update interval, calls all relevant functions to display most up-to-date information
	public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		context = ctx;
		ids = appWidgetIds;
		//Gain access
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		this.appWidgetManager = appWidgetManager;
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		thisWidget = new ComponentName(context, MyUMDWidget.class);

		//Read in users settings from mainactivity
		getSettings();

		//Find date information using calendar
		Calendar c = Calendar.getInstance();
		c.set(2013, Calendar.APRIL, 23);//Calendar.getInstance();
		Calendar next = Calendar.getInstance();
		next.add(Calendar.DAY_OF_MONTH, 7);
		int dayOfYear = 84;//c.get(Calendar.DAY_OF_YEAR);
		double target = getWeekTarget(dayOfYear, side, plan);

		//Access widget and change textviews to display relevant information
		remoteViews.setTextViewText(R.id.widget_textview, displayInformation());
		remoteViews.setTextViewText(R.id.update, "Last Updated: " + getDate() + ", " + getTime());
		remoteViews.setTextColor(R.id.widget_textview, Color.BLACK);
		remoteViews.setTextViewText(R.id.target, "Target for " + getFirstOfWeek(c) + ": " + target + 
				"\nTarget for " + getFirstOfWeek(next) + ": " + getWeekTarget(dayOfYear+7, side, plan));
		remoteViews.setTextColor(R.id.target, Color.BLACK);
		remoteViews.setTextViewText(R.id.overunder, getOverUnder(target, Double.parseDouble(amounts[0].substring(1))));
		remoteViews.setTextViewText(R.id.daily, "Target for Today: $" + getSpecificDaily(dayOfYear, Double.parseDouble(amounts[0].substring(1))));
		remoteViews.setTextColor(R.id.daily, Color.BLACK);

		Reader r = new Reader();
		int[] settings = r.readSettingsDetails();
		if(settings[3] == 1){
			Intent timerIntent = new Intent(context, MyUMDWidget.class);
			checkTime(c, timerIntent);
		}
		
		Intent configIntent = new Intent(context, MainActivity.class);
		PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widgetLayout, configPendingIntent);

		Intent active = new Intent(context, MyUMDWidget.class);
		active.setAction(ACTION_WIDGET_RECEIVE);
		active.putExtra("msg", "Updating...");
		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.imageButton1, actionPendingIntent);

		//Update the widget to reflect changes
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
	}

	public void checkTime(Calendar c, Intent timer){
		int day = c.get(Calendar.DAY_OF_WEEK);
		int hours = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		if(day == Calendar.MONDAY || day == Calendar.TUESDAY || day == Calendar.WEDNESDAY || day == Calendar.THURSDAY || day == Calendar.SUNDAY){
			if(hours == 22 || hours == 23){
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, timer, 0);
				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification noty = new Notification(R.drawable.flag, "Diner closing soon!", System.currentTimeMillis());
				String left;
				if(hours == 22){
					left = "You have 1 hour and " + (60-minutes) + "minutes left.";
				} else {
					left = "You have " + (60-minutes) + "minutes left.";	
				}
				noty.setLatestEventInfo(context, "Diner closing soon!", left, contentIntent);
				notificationManager.notify(1,noty);
			}
		} else if(day == Calendar.FRIDAY || day == Calendar.SATURDAY){
			if(hours == 17 || hours == 18){
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, timer, 0);
				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification noty = new Notification(R.drawable.flag, "Diner closing soon!", System.currentTimeMillis());
				String left;
				if(hours == 17){
					left = "You have 1 hour and " + (60-minutes) + "minutes left.";
				} else {
					left = "You have " + (60-minutes) + "minutes left.";	
				}
				noty.setLatestEventInfo(context, "Diner closing soon!", left, contentIntent);
				notificationManager.notify(1,noty);
			}
	
		}
	}
	//Makes any double into #.## format to be used as money
	public double truncate(double i){
		DecimalFormat format = new DecimalFormat("#.##"); 
		format.setRoundingMode(RoundingMode.FLOOR);
		String s = format.format(i);
		return Double.parseDouble(s);
	}

	//Reads in settings from main activity
	public void getSettings(){
		int[] settings = new int[2];
		Reader r = new Reader();
		settings = r.readSettingsDetails();
		side = settings[0];
		plan = settings[1];
	}

	//Determines the target one should be at for the week based on their settings
	public double getWeekTarget(int dayOfYear, int side, int plan){
		double target = 0;

		if(dayOfYear >= 83 && dayOfYear < 90){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[0]; break;
				case PLUS: target = NorthPlus[0]; break;
				case RED: target = NorthRed[0]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[0]; break;
				case PLUS: target = SouthPlus[0]; break;
				case RED: target = SouthRed[0]; break;
				}
			}
		} else if(dayOfYear >= 90 && dayOfYear < 97){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[1]; break;
				case PLUS: target = NorthPlus[1]; break;
				case RED: target = NorthRed[1]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[1]; break;
				case PLUS: target = SouthPlus[1]; break;
				case RED: target = SouthRed[1]; break;
				}
			}
		} else if(dayOfYear >= 97 && dayOfYear < 104){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[2]; break;
				case PLUS: target = NorthPlus[2]; break;
				case RED: target = NorthRed[2]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[2]; break;
				case PLUS: target = SouthPlus[2]; break;
				case RED: target = SouthRed[2]; break;
				}
			}
		} else if(dayOfYear >= 104 && dayOfYear < 111){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[3]; break;
				case PLUS: target = NorthPlus[3]; break;
				case RED: target = NorthRed[3]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[3]; break;
				case PLUS: target = SouthPlus[3]; break;
				case RED: target = SouthRed[3]; break;
				}
			}

		} else if(dayOfYear >= 111 && dayOfYear < 118){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[4]; break;
				case PLUS: target = NorthPlus[4]; break;
				case RED: target = NorthRed[4]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[4]; break;
				case PLUS: target = SouthPlus[4]; break;
				case RED: target = SouthRed[4]; break;
				}
			}

		} else if(dayOfYear >= 118 && dayOfYear < 125){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[5]; break;
				case PLUS: target = NorthPlus[5]; break;
				case RED: target = NorthRed[5]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[5]; break;
				case PLUS: target = SouthPlus[5]; break;
				case RED: target = SouthRed[5]; break;
				}
			}

		} else if(dayOfYear >= 125 && dayOfYear < 132){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[6]; break;
				case PLUS: target = NorthPlus[6]; break;
				case RED: target = NorthRed[6]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[6]; break;
				case PLUS: target = SouthPlus[6]; break;
				case RED: target = SouthRed[6]; break;
				}
			}

		} else if(dayOfYear >= 132 && dayOfYear < 139){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[7]; break;
				case PLUS: target = NorthPlus[7]; break;
				case RED: target = NorthRed[7]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[7]; break;
				case PLUS: target = SouthPlus[7]; break;
				case RED: target = SouthRed[7]; break;
				}
			}

		} else if(dayOfYear == 139){
			if(side == NORTH){
				switch(plan){
				case REGULAR: target = NorthReg[8]; break;
				case PLUS: target = NorthPlus[8]; break;
				case RED: target = NorthRed[8]; break;
				}
			} else if(side == SOUTH){
				switch(plan){
				case REGULAR: target = SouthReg[8]; break;
				case PLUS: target = SouthPlus[8]; break;
				case RED: target = SouthRed[8]; break;
				}
			}
		}
		return target;
	}	

	//Determines how much one should spend per week based on their settings
	public int getWeekly(int side, int plan){
		int weekly = 0;
		if(side == NORTH){
			switch(plan){
			case REGULAR: weekly = 66; break;
			case PLUS: weekly = 66; break;
			case RED: weekly = 73; break;
			}
		} else if(side == SOUTH){
			switch(plan){
			case REGULAR: weekly = 74; break;
			case PLUS: weekly = 74; break;
			case RED: weekly = 80; break;
			}
		}
		return weekly;
	}

	//Determines how much one should spend per day assuming they follow the normal schedule
	public double getNormalDaily(int weekly){
		double daily = 0;
		switch(weekly){
		case 66: daily = 9.43; break;
		case 73: daily = 10.43; break;
		case 74: daily = 10.57; break;
		case 80: daily = 11.43; break;
		}
		return daily;
	}

	//Determines how much one should spend per day in order to be at 0 on the last day, based on how much they've spent so far
	public double getSpecificDaily(int dayOfYear, double current){
		int lastDay = 139;
		int daysLeft = lastDay - dayOfYear;
		double daily = current / daysLeft;
		return truncate(daily);
	}

	//Gets the amount over or under the user is from the current weekly target
	public String getOverUnder(double thisWeek, double current){
		double overunder = truncate(current - thisWeek);

		if(overunder < 0){
			remoteViews.setTextColor(R.id.overunder, Color.RED);
			return "- $"+ overunder*(-1);
		} else if(overunder == 0){
			remoteViews.setTextColor(R.id.overunder, Color.BLACK);
			return "$0.00";
		} else {
			remoteViews.setTextColor(R.id.overunder, Color.GREEN);
			return "+ $" + overunder;
		}
	}

	//Gets the current time
	public static String getTime(){
		Calendar c = Calendar.getInstance(); 
		String min;
		int minutes = c.get(Calendar.MINUTE);
		int hours = c.get(Calendar.HOUR);
		int ampm = c.get(Calendar.AM_PM);
		String tod;
		if(ampm==Calendar.AM){
			tod="AM";
		} else {
			tod="PM";
		}
		if(hours == 0){
			hours = 12;
		}
		if(minutes < 9){
			if(minutes == 0){
				min = "00";
			}
			min = "0" + minutes;
		} else {
			min = Integer.toString(minutes);
		}


		return  hours+":"+min+" "+tod;
	}

	//Gets the current day in abbreviated form
	public String getDate(){
		Calendar c = Calendar.getInstance();
		int month = c.get(Calendar.MONTH);
		String mth = null;
		switch(month){
		case Calendar.JANUARY: mth = "Jan"; break;
		case Calendar.FEBRUARY: mth = "Feb"; break;
		case Calendar.MARCH: mth = "Mar"; break;
		case Calendar.APRIL: mth = "Apr"; break;
		case Calendar.MAY: mth = "May"; break;
		case Calendar.JUNE: mth = "Jun"; break;
		case Calendar.JULY: mth = "Jul"; break;
		case Calendar.AUGUST: mth = "Aug"; break;
		case Calendar.SEPTEMBER: mth = "Sep"; break;
		case Calendar.OCTOBER: mth = "Oct"; break;
		case Calendar.NOVEMBER: mth = "Nov"; break;
		case Calendar.DECEMBER: mth = "Dec"; break;

		}
		int day = c.get(Calendar.DATE);

		return mth + " " + day;
	}

	//Gets the date of the first day of the current week 
	public String getFirstOfWeek(Calendar c){

		int month = c.get(Calendar.MONTH);
		String mth = null;
		switch(month){
		case Calendar.JANUARY: mth = "Jan"; break;
		case Calendar.FEBRUARY: mth = "Feb"; break;
		case Calendar.MARCH: mth = "Mar"; break;
		case Calendar.APRIL: mth = "Apr"; break;
		case Calendar.MAY: mth = "May"; break;
		case Calendar.JUNE: mth = "Jun"; break;
		case Calendar.JULY: mth = "Jul"; break;
		case Calendar.AUGUST: mth = "Aug"; break;
		case Calendar.SEPTEMBER: mth = "Sep"; break;
		case Calendar.OCTOBER: mth = "Oct"; break;
		case Calendar.NOVEMBER: mth = "Nov"; break;
		case Calendar.DECEMBER: mth = "Dec"; break;

		}

		int day = c.get(Calendar.DATE);
		switch(c.get(Calendar.DAY_OF_WEEK)){
		case Calendar.MONDAY: day -= 1; break; 
		case Calendar.TUESDAY: day -= 2; break; 
		case Calendar.WEDNESDAY: day -= 3; break; 
		case Calendar.THURSDAY: day -= 4; break; 
		case Calendar.FRIDAY: day -= 5; break; 
		case Calendar.SATURDAY: day -= 6; break; 
		}

		return mth + " " + day;
	}

	//Displays the information about money in all 3 accounts
	public String displayInformation(){
		//Initialize variables
		int[] starts = new int[3];
		int[] ends = new int[3];
		String[] amounts = new String[3];

		//Gather login data
		Reader r = new Reader();
		String[] login = r.readLoginDetails();

		//Establish connection to page and scrape html data
		Document doc = null;
		while(doc == null){
			doc = makeConnection(login[0],login[1]);
		}


		String plainHTML = doc.toString(); //Convert document to string
		starts = getStarts(plainHTML); //Get starting indices of balance text
		ends = getEnds(starts, plainHTML); //Get ending indices of balance text
		amounts = getAmounts(starts,ends,plainHTML); //Substring between start and end which is the balance text

		return printData(amounts); //Print balance text


	}

	//Gets the starting index of the amounts from the HTML
	public static int[] getStarts(String html){
		int[] starts = new int[3];
		//Resident Dining Points Indicator
		starts[0] = html.indexOf("Resident Dining Plan Balance:</strong>")+POINTS_OFFSET;
		//Terp Bucks Indicator
		starts[1] = html.indexOf("Terp Bucks Balance:</strong>")+BUCKS_OFFSET;
		//Terrapin Express Indicator
		starts[2] = html.indexOf("<strong>Balance:</strong>")+EXPRESS_OFFSET;
		return starts;
	}

	//Takes substrings of the HTML that represents the amounts and stores in a global array 
	public String[] getAmounts(int[] starts, int[] ends, String html){
		amounts = new String[3];
		amounts[0] = html.substring(starts[0],ends[0]);
		amounts[1] = html.substring(starts[1],ends[1]);
		amounts[2] = html.substring(starts[2],ends[2]);
		return amounts;
	}

	//Gets the ending index of the amounts from the HTML
	public static int[] getEnds(int[] starts, String html){
		int[] ends = new int[3];
		ends[0] = html.substring(starts[0]).indexOf("<")+starts[0];
		ends[1] = html.substring(starts[1]).indexOf("<")+starts[1];
		ends[2] = html.substring(starts[2]).indexOf("<")+starts[2];
		return ends;
	}

	//Returns a 3-line string containing information about the current moeny in all 3 accounts
	public static String printData(String[] amounts){
		return ("Dining Points: " + amounts[0] + "\n" +
				"Terp Bucks: " + amounts[1] + "\n" +
				"Terrapin Express: " + amounts[2]);
	}

	//Makes the connection to MyUMD with relevant HTTP headers, and remains in-session by managing cookies
	public static Document makeConnection(String username, String password){
		Response res = null;
		try {
			res = Jsoup
					.connect("https://my.umd.edu/portal/server.pt/community/student_services/209")
					.data("in_hi_space", "Login", "in_hi_spaceID", "", "in_hi_control", "Login", "in_hi_dologin", "true", "in_tx_username", username, "in_pw_userpass", password)
					.timeout(0)
					.method(Method.POST)
					.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Obtain cookies
		Map<String, String> cookies = res.cookies();

		//Remain in session
		Document doc = null;
		try {
			doc = Jsoup.connect("https://my.umd.edu/portal/server.pt/community/student_services/209").cookies(cookies).get();
		} catch (IOException e) {


		}
		return doc;
	}

	public void onReceive (Context context, Intent intent){
		final String action = intent.getAction();
		if(intent.getAction().equals(ACTION_WIDGET_RECEIVE)) {
			String msg = "null";
			try {
				msg = intent.getStringExtra("msg");
			} catch (NullPointerException e) {
				Log.e("Error", "msg = null");
			}

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification noty = new Notification(R.drawable.flag, "Updating...", System.currentTimeMillis());

			noty.setLatestEventInfo(context, "MyUMD Info Updated", "Updated.", contentIntent);
			notificationManager.notify(1,noty);
		}

		super.onReceive(context, intent);
	}

	public void update(){
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy); 
//		
//		System.out.println("Updating!");
//		Calendar c = Calendar.getInstance();
//		Calendar next = Calendar.getInstance();
//		next.add(Calendar.DAY_OF_MONTH, 7);
//		int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
//		double target = getWeekTarget(dayOfYear, side, plan);
//
//		
//		//Access widget and change textviews to display relevant information
//		remoteViews.setTextViewText(R.id.widget_textview, displayInformation());
//		remoteViews.setTextViewText(R.id.update, "Last Updated: " + getDate() + ", " + getTime());
//		remoteViews.setTextColor(R.id.widget_textview, Color.BLACK);
//		remoteViews.setTextViewText(R.id.target, "Target for " + getFirstOfWeek(c) + ": " + target + 
//				"\nTarget for " + getFirstOfWeek(next) + ": " + getWeekTarget(dayOfYear+7, side, plan));
//		remoteViews.setTextColor(R.id.target, Color.BLACK);
//		remoteViews.setTextViewText(R.id.overunder, getOverUnder(target, Double.parseDouble(amounts[0].substring(1))));
//		remoteViews.setTextViewText(R.id.daily, "Target for Today: $" + getSpecificDaily(dayOfYear, Double.parseDouble(amounts[0].substring(1))));
//		remoteViews.setTextColor(R.id.daily, Color.BLACK);
//		
//		//Update the widget to reflect changes
//				appWidgetManager.updateAppWidget(thisWidget, remoteViews);
//				
//		System.out.println("SUCCESESSSS");
		
	}

}