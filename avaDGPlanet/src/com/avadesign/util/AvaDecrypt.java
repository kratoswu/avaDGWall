package com.avadesign.util;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class AvaDecrypt {
    public static String decode(String data){
        try{
            String AESkey = "1266CamAvA";
            byte[] key = getMD5(AESkey).toLowerCase().substring(0, 16).getBytes();
            
            byte[] encrypted = new byte[data.toString().length()/2];
            for(int i=0; i<encrypted.length; i++){
                encrypted[i] =(byte)Integer.parseInt(data.substring(i*2, i*2+2), 16); //ex: String 0B -> byte 0B
            }
            
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            IvParameterSpec iv = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, spec, iv);
            
//          Cipher cipher = Cipher.getInstance("AES");
//          cipher.init(Cipher.DECRYPT_MODE, spec);
            
            byte[] original = cipher.doFinal(encrypted);
           
            return new String(original);
        }
        catch(Exception e){
            Log.e(AvaDecrypt.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());

            byte[] digest = md.digest();

            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < digest.length; ++i) {
                final byte b = digest[i];
                final int value = (b & 0x7F) + (b < 0 ? 128 : 0);
                buffer.append(value < 16 ? "0" : "");
                buffer.append(Integer.toHexString(value));
            }

            return buffer.toString();
        } catch (Exception e) {
            Log.e(AvaDecrypt.class.getSimpleName(), e.getMessage(), e);
        }

        return null;
    }
}
