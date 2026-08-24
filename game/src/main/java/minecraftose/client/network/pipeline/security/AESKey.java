package minecraftose.client.network.pipeline.security;

import generaloss.resourceflow.resource.FileResource;
import generaloss.resourceflow.resource.Resource;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class AESKey {

    private final SecretKey key;
    private final Cipher encryptCipher, decryptCipher;

    public AESKey(SecretKey key) {
        this.key = key;
        this.encryptCipher = getEncryptCipher(key);
        this.decryptCipher = getDecryptCipher(key);
    }

    public AESKey(byte[] bytes) {
        this(new SecretKeySpec(bytes, "AES"));
    }

    public AESKey(Resource res) {
        this(res.readBytes());
    }

    public AESKey(int size) {
        this(generateSecretKey(size));
    }


    public SecretKey getKey() {
        return key;
    }

    public Cipher getEncryptCipher() {
        return encryptCipher;
    }

    public Cipher getDecryptCipher() {
        return decryptCipher;
    }


    public byte[] encrypt(byte[] bytes) {
        try{
            return encryptCipher.doFinal(bytes);
        }catch(IllegalBlockSizeException | BadPaddingException e){
            throw new IllegalStateException(e);
        }
    }

    public byte[] decrypt(byte[] bytes) {
        try{
            return decryptCipher.doFinal(bytes);
        }catch(IllegalBlockSizeException | BadPaddingException e){
            throw new IllegalStateException(e);
        }
    }


    public boolean save(FileResource res) {
        if(res.createWithParents()){
            res.writeBytes(key.getEncoded());
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null || getClass() != object.getClass())
            return false;
        final AESKey aesKey = (AESKey) object;

        final byte[] aesEncoded = aesKey.getKey().getEncoded();
        final byte[] thisEncoded = this.key.getEncoded();
        return Arrays.equals(aesEncoded, thisEncoded);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    private static SecretKey generateSecretKey(int size) {
        try{
            final KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(size);
            return generator.generateKey();
        }catch(NoSuchAlgorithmException ignored){
            return null;
        }
    }

    private static Cipher getEncryptCipher(SecretKey key) {
        try{
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;

        }catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e){
            throw new RuntimeException(e);
        }
    }

    private static Cipher getDecryptCipher(SecretKey key) {
        try{
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher;

        }catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e){
            throw new RuntimeException(e);
        }
    }

}
