/*
 * DroidScript Plugin class.
 * This plugin was written by Laurent PROTT (laurent.prott@gmail.com).
 * This plugin is under MIT licence.
 */

package com.laurepro.plugins.user;

import android.os.*;
import android.util.Log;
import android.content.Context;
import android.net.ConnectivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.media.session.MediaSession;
import java.lang.reflect.*;
import android.media.*;

public class MediaCompanion
{
	public static String TAG = "MediaCompanion";
	public static float VERSION = 1.0f;
	private Method m_callscript;
	private Object m_parent;
	private Context m_ctx;
	private AudioManager mAudioManager;
	private MediaMetadata metadata;
	private MediaSession mMediaSession;
	private String m_onSignalStatusChangeCallback;
	BroadcastReceiver receiver;

	//broadcast receiver to catch the desired intent.
	private class CompanionReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
			{
				String state;
				if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
				{
					state = "Lost";
				}
				else
				{
					state = "Gain";
				}
				Log.d(TAG, "Network " + state);
				onSignalStatusChange(state);
			} //if
		} //onReceive
	} //receiver class

	//Construct plugin.
	public MediaCompanion()
	{
		Log.d(TAG, "Creating plugin object");
		this.receiver = null;
	} //constructor

	//Initialise plugin.
	public void Init(Context ctx, Object parent)
	{
		try
		{
			Log.d(TAG, "Initialising plugin object");
			m_ctx = ctx;
			mAudioManager = (AudioManager)(m_ctx).getSystemService(Context.AUDIO_SERVICE);
			m_parent = parent;
			m_callscript = parent.getClass().getMethod("CallScript", Bundle.class);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			intentFilter.setPriority(999);
			this.receiver = new CompanionReceiver();
			m_ctx.registerReceiver(this.receiver, intentFilter);	
		} //try
		catch (Exception e)
		{
			Log.e(TAG, "Failed to Initialise plugin!", e);
		} //catch
	} //init

	//Release plugin resources.
	public void Release()
	{
		if (this.receiver != null) try
			{
				m_ctx.unregisterReceiver(this.receiver);
				this.receiver = null;
			}
			catch (Exception e)
			{
				Log.e(TAG, "Error unregistering receiver: " + e.getMessage(), e);
			} //try
	} //Release

	//create SetOnSignalLost event
	public void SetOnSignalStatusChange(Bundle b)
	{
		m_onSignalStatusChangeCallback = b.getString("p1");
	} //SetOnDeviceLost

	//Use this method to call a function in the user's script.
	private void CallScript(Bundle b)
	{
		try
		{
			m_callscript.invoke(m_parent, b);
		} //try
		catch (Exception e)
		{
			Log.e(TAG, "Failed to call script function!", e);
		} //catch
	} //CallScript

	//Handle commands from DroidScript.
	public String CallPlugin(Bundle b)
	{
		//Extract command.
		String cmd = b.getString("cmd");
		//Process commands.
		String ret = null;
		try
		{
			if (cmd.equals("GetVersion"))
			{
				return GetVersion();
			}
			else if (cmd.equals("SendAvrcpMeta"))
			{
				String p1 = b.getString("p1");
				String p2 = b.getString("p2");
				String p3 = b.getString("p3");
				SendAvrcpMeta(p1, p2, p3);
			}
			else if (cmd.equals("SetOnSignalStatusChange"))
			{
				SetOnSignalStatusChange(b);
			} //if
		} //try
		catch (Exception e)
		{
			Log.e(TAG, "Plugin command failed!", e);
		} //catch
		return ret;
	} //CallPlugin

	//Handle the GetVersion command.
	public String GetVersion()
	{
		Log.d(TAG, "Got GetVersion");
		return Float.toString(VERSION);
	} //GetVersion

	//Handle the 'SendAvrcpMeta' command.
	private void SendAvrcpMeta(String title, String artist, String album)
	{
		Log.d(TAG, "Got 'SendAvrcpMeta'");
		// Return if Bluetooth A2DP is not in use.
		if (!mAudioManager.isBluetoothA2dpOn()) return;
		MediaMetadata metadata = new MediaMetadata.Builder()
			.putString(MediaMetadata.METADATA_KEY_TITLE, title)
			.putString(MediaMetadata.METADATA_KEY_ALBUM, album)
			.putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
			.build();
		mMediaSession.setMetadata(metadata);			
	} //SendAvrcpMeta

	//Handle the 'onSignalStatusChange' command.
	public void onSignalStatusChange(String data)
	{
		Log.d(TAG, "Calling OnSignalLost Callback");
		if (m_onSignalStatusChangeCallback == null) return;
		Bundle b = new Bundle();
		b.putString("cmd", m_onSignalStatusChangeCallback);
		b.putString("p1", data);
		CallScript(b);
	} //onSignalStatusChange

} //MediaCompanion


