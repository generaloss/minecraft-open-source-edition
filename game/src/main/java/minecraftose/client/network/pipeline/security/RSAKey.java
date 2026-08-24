package minecraftose.client.network.pipeline.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAKey {

    private final PublicRSA publicKey;
    private final PrivateRSA privateKey;

    public RSAKey(int size) {
        final KeyPair pair = generateKeyPair(size);
        this.publicKey = new PublicRSA(pair.getPublic());
        this.privateKey = new PrivateRSA(pair.getPrivate());
    }

    public PublicRSA getPublic() {
        return publicKey;
    }

    public PrivateRSA getPrivate() {
        return privateKey;
    }


    public byte[] encrypt(byte[] bytes) {
        return publicKey.encrypt(bytes);
    }

    public byte[] decrypt(byte[] bytes) {
        return privateKey.decrypt(bytes);
    }


    private static KeyPair generateKeyPair(int size) {
        try{
            final KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            pairGenerator.initialize(size);
            return pairGenerator.generateKeyPair();
        }catch(NoSuchAlgorithmException ignored){
            throw new RuntimeException("Failed to generate RSA key pair");
        }
    }

}
