package edu.jiit.lostlink.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.Base64;

public final class Passwords {
    private Passwords() {}
    public static String newSalt(){byte[] b=new byte[16]; new SecureRandom().nextBytes(b); return Base64.getEncoder().encodeToString(b);}
    public static String hash(String password,String salt){
        try { PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),Base64.getDecoder().decode(salt),120_000,256);
            byte[] key=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); spec.clearPassword(); return Base64.getEncoder().encodeToString(key);
        } catch(GeneralSecurityException e){throw new IllegalStateException("Password hashing is unavailable",e);}
    }
}
