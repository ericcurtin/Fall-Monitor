package com.ericcurtin.fallMonitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;

public class SettingsData implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<String> contactList;
	private String chosenRingtone;
	private final String defaultRingtone;
	private Context context;
	private SettingsData settings;

	// public SettingsData() {
	// contactList = new ArrayList<String>();
	// chosenRingtone =
	// "android.resource://com.ericcurtin.fallMonitor/raw/fall";
	// changeOccurred();
	// }

	private SettingsData() {
		defaultRingtone = "android.resource://com.ericcurtin.fallMonitor/raw/fall";
	}

	public SettingsData(Context context) {
		this();
		this.context = context;
		try {
			FileInputStream fileInputStream = context.openFileInput(context
					.getString(R.string.settFileName));
			ObjectInputStream objectInputStream = new ObjectInputStream(
					fileInputStream);
			settings = (SettingsData) objectInputStream.readObject();
			this.chosenRingtone = settings.getChosenRingtone();
			setContactList(settings.getContactList());
			objectInputStream.close();
			fileInputStream.close();
			// System.out.println("gher " + settings + " gher " + to);
		} catch (Exception e) {
			contactList = new ArrayList<String>();
			chosenRingtone = "android.resource://com.ericcurtin.fallMonitor/raw/fall";
		}
	}

	private SettingsData(ArrayList<String> contactList, String chosenRingtone) {
		this();
		this.contactList = contactList;
		this.chosenRingtone = chosenRingtone;
	}

	public String[] getContactList() {
		return contactList.toArray(new String[contactList.size()]);
	}

	private void setContactList(String[] contactList) {
		this.contactList = new ArrayList<String>(Arrays.asList(contactList));
		changeOccurred();
	}

	public String getChosenRingtone() {
		return chosenRingtone;
	}

	public void addContactNumber(String contactNumber) {
		this.contactList.add(contactNumber);
		// System.out.println("gher   12");
		changeOccurred();
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setChosenRingtone(String chosenRingtone) {
		this.chosenRingtone = chosenRingtone;
		changeOccurred();
	}

	public String getDefaultRingtone() {
		return defaultRingtone;
	}

	private void changeOccurred() {
		// System.out.println("SettingsData change");
		try {
			FileOutputStream fileOutputStream = context.openFileOutput(
					context.getString(R.string.settFileName),
					Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					fileOutputStream);
			objectOutputStream.writeObject(new SettingsData(contactList,
					chosenRingtone));
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (IOException ioE) {
			// ioE.printStackTrace();
		}
	}

	public void resetArrayList() {
		contactList = new ArrayList<String>();
		// System.out.println(this + "112");
	}

	@Override
	public String toString() {
		return "SettingsData [chosenRingtone=" + chosenRingtone
				+ ", getContactList()=" + Arrays.toString(getContactList())
				+ "]";
	}
}