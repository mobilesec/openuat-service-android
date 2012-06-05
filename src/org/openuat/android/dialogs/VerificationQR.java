/* Copyright Hannes Markschläger
 * File created 21 Apr 2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android.dialogs;

import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.Client;
import org.openuat.android.service.DiscoverService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Hannes Markschlaeger
 */
public class VerificationQR extends Activity {
	public static final String CLIENT_EXTRA = "CLIENT_EXTRA";
	private Client client = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String str = getIntent().getStringExtra(CLIENT_EXTRA);
		if (str == null) {
			Log.e(this.toString(), "no client passed!");
			finish();
			return;
		}
		OpenUAT_ID id = OpenUAT_ID.parseToken(str);
		if (id == null) {
			Log.e(this.toString(), "ID parsing failed!");
			finish();
			return;
		}
		client = id.getApp().getClientById(id);
		if (client == null) {
			Log.e(this.toString(), "Client not found!");
			finish();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Incoming Connection")
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(
						"Incoming Connection from [insert requesting IP.\nAccept?")
				.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						IntentIntegrator integrator = new IntentIntegrator(
								VerificationQR.this);
						integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
					}
				})
				.setNegativeButton(android.R.string.no, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						DiscoverService.oob_key = "fail";
						VerificationQR.this.finish();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			IntentResult result = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, data);
			String key = null;
			if (result != null) {
				key = result.getContents();
				Log.i("scanresult", key);
			}
			client.checkKeys(key);
			finish();
//			DiscoverService.oob_key = key;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		DiscoverService.oob_key = "back";
	}
}
