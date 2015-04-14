package com.avadesign.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class AES {
    
    public static final String AES_KEY = "1266CamAvA";
    
    public static String encode(String data, String AESkey){
        try{
            byte[] key = getMD5(AESkey).toLowerCase().substring(0, 16).getBytes();
            
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            IvParameterSpec iv = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, spec, iv);
            
            // camera not support PKCS padding, so need padding ourselves
            int m = data.getBytes().length%16;
            if(m > 0){
                for(int i=0; i<16-m;i++){
                    data = data + new String(new byte[]{(byte)0x00});
                }
            }
            
            byte[] encrypted = cipher.doFinal(data.getBytes());
           
            StringBuffer sb = new StringBuffer("");
            for(byte e:encrypted){
                sb.append("%"); //for David's request
                sb.append(String.format("%02X", e)); //ex: byte 0B -> String 0B
            }
            return sb.toString();
        }
        catch(Exception e){
            Log.e(AES.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    public static String decode(String data, String AESkey){
        byte[] re = new String("").getBytes();
        try{
            byte[] key = getMD5(AESkey).toLowerCase().substring(0, 16).getBytes();
            
            byte[] encrypted = new byte[data.toString().length()/2];
            for(int i=0; i<encrypted.length; i++){
                encrypted[i] =(byte)Integer.parseInt(data.substring(i*2, i*2+2), 16); //ex: String 0B -> byte 0B
            }
            
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            IvParameterSpec iv = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, spec, iv);
            
            byte[] original = cipher.doFinal(encrypted);
            
            //找byte A0的位置, A0後面都是補碼, 這是David補位的方式
            int A0_position = -1;
            for(int i=0;i<original.length;i++){
                if(original[i]==(byte)0xA0){
                    A0_position = i;
                    break;
                }
            }
            
            //此project的paddingByte應只出現在最後面,所以回傳decrypted前面的byte[]
            re = new byte[A0_position];
            for(int i=0; i<re.length; i++){
                re[i] = original[i];
            }
            return new String(re);
        }
        catch(Exception e){
            Log.e(AES.class.getSimpleName(), e.getMessage(), e);
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
        } catch (NoSuchAlgorithmException e) {
            Log.e(AES.class.getSimpleName(), e.getMessage(), e);
        }

        return null;
    }
}
