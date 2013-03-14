package com.ericcurtin.fallMonitor;

import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.MultiAutoCompleteTextView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.holoeverywhere.widget.ToggleButton;

import de.ub0r.android.lib.apis.ContactsWrapper;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FallMonitorActivity extends FallMonitorAbstActivity {

	/** {@link Uri} for saving sent messages. */
	public static final Uri URI_SENT = Uri.parse("content://sms/sent");
	private SettingsData settingsData;
	private String to;
	private ToggleButton toggle;
	private Button calibrateButton, setContactButton, selectAlertButton;
	private MultiAutoCompleteTextView mtv;
	private Intent serviceIntent;
	private TextView contactsTextView;

	// private CalibrationData calibrationData;

	@Override
	protected void initializeUI() {
		setContentView(R.layout.main);
		new Eula(this).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// catch (IOException i) {
		// i.printStackTrace();
		// } catch (ClassNotFoundException c) {
		// System.out.println("CalibrationData class not found.");
		// c.printStackTrace();
		// }
		// try {
		// FileInputStream fileInputStream =
		// openFileInput(getString(R.string.calibFileName));
		// ObjectInputStream objectInputStream = new ObjectInputStream(
		// fileInputStream);
		// calibrationData = (CalibrationData) objectInputStream.readObject();
		// objectInputStream.close();
		// fileInputStream.close();
		// System.out.println("jonny " + calibrationData);
		// } catch (Exception e) {
		// calibrationData = new CalibrationData();
		// }
		settingsData = new SettingsData(this);

		// System.out.println(settingsData);
		calibrateButton = (Button) findViewById(R.id.calibrateButton);
		calibrateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(FallMonitorActivity.this,
						AccelerometerCalibrationActivity.class);
				startActivity(intent);
			}
		});
		contactsTextView = (TextView) findViewById(R.id.contactsTextView);
		setContactsText();
		contactsTextView.setTextSize(calibrateButton.getTextSize());
		setContactButton = (Button) findViewById(R.id.setContactButton);
		setContactButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// System.out.println(calibrationData);
				setEmerContact();
			}
		});
		selectAlertButton = (Button) findViewById(R.id.selectAlertSound);
		selectAlertButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				selectAlert();
			}

		});
		toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		toggle.setOnCheckedChangeListener(toggleListener);
		mtv = (MultiAutoCompleteTextView) this.findViewById(R.id.to);
		mtv.setAdapter(new MobilePhoneAdapter(this));
		mtv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		mtv.setText(this.to);
		if (!TextUtils.isEmpty(this.to)) {
			this.to = this.to.trim();
			if (this.to.endsWith(",")) {
				this.to = this.to.substring(0, this.to.length() - 1).trim();
			}
			if (this.to.indexOf('<') < 0) {
				// try to fetch recipient's name from phone book
				String n = ContactsWrapper.getInstance().getNameForNumber(
						this.getContentResolver(), this.to);
				if (n != null) {
					this.to = n + " <" + this.to + ">, ";
				}
			}
			mtv.setText(this.to);
			// setContactButton.requestFocus();
		} else {
			mtv.requestFocus();
		}
		toggle.setChecked(isMyServiceRunning());
		// System.out.println(settingsData);
	}

	private OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				if (settingsData.getContactList().length < 1) {
					toggle.setChecked(false);
					Toast.makeText(FallMonitorActivity.this,
							"Enter at least one emergency contact",
							Toast.LENGTH_LONG).show();
				} else if (isMyServiceRunning() == false)
					startAccelService();
				else
					setEnabled(true);

			} else
				stopAccelService();
			// stopService(new Intent(FallMonitorActivity.this,
			// AccelerometerService.class));
		}
	};

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ((getPackageName() + ".AccelerometerService")
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private boolean setEmerContact() {
		EditText et = (MultiAutoCompleteTextView) this.findViewById(R.id.to);
		this.to = et.getText().toString();
		settingsData.resetArrayList();
		if (TextUtils.isEmpty(this.to))
			return false;
		for (String r : this.to.split(",")) {
			r = MobilePhoneAdapter.cleanRecipient(r);
			if (TextUtils.isEmpty(r)) {
				continue;
			}
			settingsData.addContactNumber(r);
			// this.send(r, this.text);
		}
		// if (isMyServiceRunning()) {
		// stopService(serviceIntent);
		// startAccelService();
		// }
		setContactsText();
		return true;
	}

	private void selectAlert() {
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
				RingtoneManager.TYPE_NOTIFICATION);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
		this.startActivityForResult(intent, 5);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		if (resultCode == FallMonitorAbstActivity.RESULT_OK && requestCode == 5) {
			Uri uri = intent
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

			if (uri != null)
				settingsData.setChosenRingtone(uri.toString());
		}
	}

	private void startAccelService() {
		// serviceIntent.putExtra("settings", settings.getContactList());
		// serviceIntent.putExtra("soundFile", settings.getChosenRingtone());
		serviceIntent = new Intent(FallMonitorActivity.this,
				AccelerometerService.class);
		startService(serviceIntent);
		setEnabled(true);
	}

	private void stopAccelService() {
		// serviceIntent.putExtra("settings", settings.getContactList());
		// serviceIntent.putExtra("soundFile", settings.getChosenRingtone());
		serviceIntent = new Intent(FallMonitorActivity.this,
				AccelerometerService.class);
		stopService(serviceIntent);
		setEnabled(false);
	}

	private void setEnabled(boolean enabled) {
		calibrateButton.setEnabled(!enabled);
		setContactButton.setEnabled(!enabled);
		selectAlertButton.setEnabled(!enabled);
		mtv.setEnabled(!enabled);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isAccelOn", enabled);
		editor.commit();
	}

	private void setContactsText() {
		String contactsText = "Active Emergency Contacts:";
		int j = 0;
		for (int i = 0; i < settingsData.getContactList().length; i++, j++) {
			contactsText += "<br /><font color=#";
			if (j == 0)
				contactsText += Integer.toHexString(Color.GREEN).substring(2);
			else if (j == 1)
				contactsText += Integer.toHexString(Color.CYAN).substring(2);
			else if (j == 2)
				contactsText += Integer.toHexString(Color.BLUE).substring(2);
			else if (j == 3)
				contactsText += Integer.toHexString(Color.MAGENTA).substring(2);
			else if (j == 4) {
				contactsText += Integer.toHexString(Color.RED).substring(2);
				j -= 5;
			}
			contactsText += ">" + settingsData.getContactList()[i] + "</font>";
		}
		contactsTextView.setText(Html.fromHtml(contactsText));
	}
}