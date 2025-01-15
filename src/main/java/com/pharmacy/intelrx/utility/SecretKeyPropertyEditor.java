package com.pharmacy.intelrx.utility;

import java.beans.PropertyEditorSupport;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecretKeyPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            // Check if the input is already a SecretKey
            if (text.startsWith("javax.crypto.spec.SecretKeySpec")) {
                setValue(text); // Set the value directly
            } else {
                // Otherwise, decode the Base64 string
                byte[] decodedKey = Base64.getDecoder().decode(text);
                SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                setValue(secretKey);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to decode Base64 string: " + text, e);
        }
    }


}
