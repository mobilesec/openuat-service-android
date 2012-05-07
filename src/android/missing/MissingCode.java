/* Copyright Hannes Markschläger
 * File created 22 Apr 2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package android.missing;

import android.content.ComponentName;
import android.content.Intent;

/**
 * 
 * @author Hannes Markschlaeger
 */
public class MissingCode {

    /**
     * Make an Intent that can be used to re-launch an application's task in its
     * base state. This is like {@link #makeMainActivity(ComponentName)}, but
     * also sets the flags {@link #FLAG_ACTIVITY_NEW_TASK} and
     * {@link #FLAG_ACTIVITY_CLEAR_TASK}.
     * 
     * @param mainActivity
     *            The activity component that is the root of the task; this is
     *            the activity that has been published in the application's
     *            manifest as the main launcher icon.
     * 
     * @return Returns a newly created Intent that can be used to relaunch the
     *         activity's task in its root state.
     */
    public static Intent makeRestartActivityTask(ComponentName mainActivity) {
	Intent intent = makeMainActivity(mainActivity);
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	return intent;
    }

    /**
     * Create an intent to launch the main (root) activity of a task. This is
     * the Intent that is started when the application's is launched from Home.
     * For anything else that wants to launch an application in the same way, it
     * is important that they use an Intent structured the same way, and can use
     * this function to ensure this is the case.
     * 
     * <p>
     * The returned Intent has the given Activity component as its explicit
     * component, {@link #ACTION_MAIN} as its action, and includes the category
     * {@link #CATEGORY_LAUNCHER}. This does <em>not</em> have
     * {@link #FLAG_ACTIVITY_NEW_TASK} set, though typically you will want to do
     * that through {@link #addFlags(int)} on the returned Intent.
     * 
     * @param mainActivity
     *            The main activity component that this Intent will launch.
     * @return Returns a newly created Intent that can be used to launch the
     *         activity as a main application entry.
     * 
     * @see #setClass
     * @see #setComponent
     */
    public static Intent makeMainActivity(ComponentName mainActivity) {
	Intent intent = new Intent(Intent.ACTION_MAIN);
	intent.setComponent(mainActivity);
	intent.addCategory(Intent.CATEGORY_LAUNCHER);
	return intent;
    }

}
