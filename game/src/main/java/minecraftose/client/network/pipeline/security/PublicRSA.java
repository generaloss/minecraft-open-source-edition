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
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class PublicRSA {

    private final PublicKey key;
    private final Cipher cipher;

    public PublicRSA(PublicKey key) {
        this.key = key;
        this.cipher = getEncryptCipher(key);
    }

    public PublicRSA(byte[] bytes) {
        this(generatePublicKey(bytes));
    }

    public PublicRSA(Resource res) {
        this(res.readBytes());
    }

    public PublicKey getKey() {
        return key;
    }


    public byte[] encrypt(byte[] bytes) {
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


    private static PublicKey generatePublicKey(byte[] bytes) {
        try{
            return KeyFactory
                .getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(bytes));
        }catch(InvalidKeySpecException | NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    private static Cipher getEncryptCipher(PublicKey publicKey) {
        try{
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher;
        }catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e){
            throw new RuntimeException(e);
        }
    }

}
