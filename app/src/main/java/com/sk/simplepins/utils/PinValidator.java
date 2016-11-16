package com.sk.simplepins.utils;

import com.sk.simplepins.data.Pin;

/**
 * Created by Sviat on Nov 15, 2016.
 */

public class PinValidator {
    /**
     * Checks if Pin object has uid (facebook id).
     *
     * @param pin pin to check
     * @throws IllegalArgumentException if uid null or empty
     */
    public static void validate(Pin pin) {
        if (pin == null) {
            throw new NullPointerException("Pin can't be null");
        }

        String uid = pin.getUid();

        if (uid == null || uid.isEmpty()) {
            throw new IllegalArgumentException("Facebook user id cannot be null or empty");
        }
    }
}