/* Copyright Hannes Markschl�ger
 * File created 21 Apr 2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android.dialogs;

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

    public static final String KEY_OOB = "oob_message";
    protected static final int ID = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

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
	    DiscoverService.oob_key = key;
	    finish();
	}

    }
}