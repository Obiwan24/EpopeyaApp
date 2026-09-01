package com.example.epopeyaap.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class DniEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";
    private final SecretKey key;

    public DniEncryptor(@Value("${dni.encryption.key}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(), ALGORITHM);
    }

    @Override
    public String convertToDatabaseColumn(String dni) {
        //cifra al guardar
        if (dni == null) return null;
        try{
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(dni.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar el DNI", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dniCifrado) {
        //descifra al leer
        if (dniCifrado == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(dniCifrado);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar el DNI", e);
        }
    }
}
