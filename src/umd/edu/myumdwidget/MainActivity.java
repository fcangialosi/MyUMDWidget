package umd.edu.myumdwidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	EditText username, password, freq;
	RadioGroup sidegroup, plangroup;
	CheckBox closing;
	int side, plan, frequency, notif = 0;
	private Button save;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		
		Reader r = new Reader();
		String[] login = r.readLoginDetails();
		if(login != null){
			username.setText(login[0]);
			password.setText(login[1]);
		}
//		int[] settings = r.readSettingsDetails();
//		if(settings != null){
//			if(settings[0] == 0){
//				sidegroup.check(R.id.radio3);
//			} else {
//				sidegroup.check(R.id.radio4);
//			}
//			if(settings[1] == 0){
//				plangroup.check(R.id.radio0);
//			} else if(settings[1] == 1){
//				plangroup.check(R.id.radio1);
//			} else {
//				plangroup.check(R.id.radio2);
//			}
//			freq.setText(settings[2]);
//			if(settings[3] == 0){
//				closing.setChecked(false);
//			} else {
//				closing.setChecked(true);
//			}
//		}
		
	
		this.save = (Button)this.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getSettingsInfo();
				Reader r = new Reader();
				r.writeLoginDetails(username.getText().toString(),password.getText().toString());
				r.writeSettingsDetails(side, plan, frequency, notif);
				displayToast();
			}
		});
		
		setFonts();
		
		
		
//		save.setOnTouchListener(new OnTouchListener() {
//
//			public boolean onTouch(View v, MotionEvent event){
//				if (event.getAction() == MotionEvent.ACTION_DOWN){
//					save.setBackgroundResource(R.drawable.redpressed);
//					System.out.println("down");
//				} else if (event.getAction() == MotionEvent.ACTION_UP) {
//					save.setBackgroundResource(R.drawable.rednormal);
//					System.out.println("up");
//				}
//				return false;
//			}
//		});

		
	}
	
	private void displayToast() {
		Toast.makeText(this, new StringBuilder().append("Login Data Saved!"),   Toast.LENGTH_SHORT).show();	
	}
	
	private void getSettingsInfo(){
	
		sidegroup = (RadioGroup) findViewById(R.id.radioGroup2);
		side = sidegroup.getCheckedRadioButtonId();
		plangroup = (RadioGroup) findViewById(R.id.radioGroup1);
		plan = plangroup.getCheckedRadioButtonId();
		System.out.println(side+ " "+plan);
		System.out.println(R.id.radio3+ " " + R.id.radio0);
		switch(side){
		case R.id.radio3: side = 0; break;
		case R.id.radio4: side = 1; break;
		}
		
		switch(plan){
		case R.id.radio0: plan = 0; break;
		case R.id.radio1: plan = 1; break;
		case R.id.radio2: plan = 2; break;
		}
		
		freq = (EditText) findViewById(R.id.editText3);
		closing = (CheckBox) findViewById(R.id.checkBox1);
		
		frequency = Integer.parseInt(freq.getText().toString());
		if(closing.isChecked()){
			notif = 1;
		}
	}
	private void setFonts(){

        TextView txt = (TextView) findViewById(R.id.textView1);
        TextView txt2 = (TextView) findViewById(R.id.textView2);
        TextView txt3 = (TextView) findViewById(R.id.textView3);
        TextView txt4 = (TextView) findViewById(R.id.textView4);
        TextView txt5 = (TextView) findViewById(R.id.textView5);
        TextView txt6 = (TextView) findViewById(R.id.textView6);
        EditText hours = (EditText) findViewById(R.id.editText3);
        RadioButton r0 = (RadioButton) findViewById(R.id.radio0);
        RadioButton r1 = (RadioButton) findViewById(R.id.radio1);
        RadioButton r2 = (RadioButton) findViewById(R.id.radio2);
        RadioButton r3 = (RadioButton) findViewById(R.id.radio3);
        RadioButton r4 = (RadioButton) findViewById(R.id.radio4);
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox1);
        Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        txt.setTypeface(font);
        txt2.setTypeface(font);
        txt3.setTypeface(font);
        txt4.setTypeface(font);
        txt5.setTypeface(font);
        txt6.setTypeface(font);
        r0.setTypeface(font);
        r1.setTypeface(font);
        r2.setTypeface(font);
        r3.setTypeface(font);
        r4.setTypeface(font);
        cb.setTypeface(font);
        username.setTypeface(font);
        password.setTypeface(font);
        hours.setTypeface(font);
        save.setTypeface(font);
        
	}

}
