package minecraftose.client.network.pipeline.security;

import generaloss.resourceflow.resource.FileResource;
import generaloss.resourceflow.resource.Resource;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class PrivateRSA {

    private final PrivateKey key;
    private final Cipher cipher;

    public PrivateRSA(PrivateKey key) {
        this.key = key;
        this.cipher = getDecryptCipher(key);
    }

    public PrivateRSA(byte[] bytes) {
        this(generatePrivateKey(bytes));
    }

    public PrivateRSA(Resource res) {
        this(res.readBytes());
    }

    public PrivateKey getKey() {
        return key;
    }


    public byte[] decrypt(byte[] bytes) {
        try{
            return cipher.doFinal(bytes);
        }catch(IllegalBlockSizeException | BadPaddingException e){
            throw new RuntimeException(e);
        }
    }


    public boolean save(FileResource res) {
        if(res.createWithParents()){
            res.writeBytes(key.getEncoded());
            return true;
        }
        return false;
    }


    private static PrivateKey generatePrivateKey(byte[] bytes) {
        try{
            return KeyFactory
                .getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(bytes));
        }catch(InvalidKeySpecException | NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    private static Cipher getDecryptCipher(PrivateKey privateKey) {
        try{
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher;
        }catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e){
            throw new RuntimeException(e);
        }
    }

}
