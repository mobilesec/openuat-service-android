/* Copyright Hannes Markschläger
 * File created 26 Apr 2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android.service;

import org.openuat.android.service.SecureChannel.VERIFICATION_STATUS;

/**
 * 
 * @author Hannes Markschlaeger
 */
public interface IVerificationStatusListener {
    void onVerificationStatusChanged(VERIFICATION_STATUS newStatus);
}
