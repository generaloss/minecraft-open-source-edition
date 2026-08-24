package minecraftose.client.network.pipeline.security;

import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.tcp.listener.CloseReason;
import generaloss.networkforge.tcp.pipeline.EventHandler;
import generaloss.networkforge.tcp.pipeline.EventInvocationContext;
import generaloss.resourceflow.stream.BinaryInputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class ClientSecureHandler extends EventHandler {

    public static final int AES_KEY_SIZE = 128;

    private volatile TCPConnection connection;
    private volatile SecretKey secretKey;
    private volatile boolean handshakeCompleted;
    private final ByteArrayOutputStream pendingData;

    public ClientSecureHandler() {
        this.pendingData = new ByteArrayOutputStream();
    }

    @Override
    public boolean handleConnect(EventInvocationContext context) {
        this.connection = context.getConnection();
        return false;
    }


    @Override
    public boolean handleDisconnect(EventInvocationContext context, CloseReason reason) {
        connection = null;
        secretKey = null;
        handshakeCompleted = false;
        pendingData.reset();
        return true;
    }

    @Override
    public boolean handleReceive(EventInvocationContext context, byte[] data) {
        if(handshakeCompleted)
            return true;

        try (final BinaryInputStream stream = new BinaryInputStream(data)) {
            final int binaryFrame = stream.readByte();

            if(binaryFrame == SecureBinaryFrames.PUBLIC_KEY.ordinal()) {
                // PUBLIC_KEY
                final byte[] publicKeyBytes = stream.readByteArray();

                final PublicKey publicKey = KeyFactory
                        .getInstance("RSA")
                        .generatePublic(new X509EncodedKeySpec(publicKeyBytes));

                this.onReceivePublicKey(context, publicKey);
            } else if(binaryFrame == SecureBinaryFrames.CONNECTION_ENCRYPTED_SIGNAL.ordinal()) {
                // CONNECTION_ENCRYPTED_SIGNAL
                this.onReceiveEncryptedSignal(context);
            } else {
                // ?
                throw new RuntimeException("Invalid binary frame " + binaryFrame);
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to read binary frame", e);
        }
        return false;
    }

    private void onReceivePublicKey(EventInvocationContext context, PublicKey publicKey) {
        try {
            // key
            final KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(AES_KEY_SIZE);
            secretKey = generator.generateKey();

            final Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            final byte[] encryptedSecretKey = encryptCipher.doFinal(secretKey.getEncoded());
            this.sendEncryptedSecretKey(context, encryptedSecretKey);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void sendEncryptedSecretKey(EventInvocationContext context, byte[] encryptedSecretKey) {
        final boolean success = context.send(stream -> {
            stream.writeByte(SecureBinaryFrames.ENCRYPTED_SECRET_KEY.ordinal());
            stream.writeByteArray(encryptedSecretKey);
        });
        if(!success)
            throw new RuntimeException("Failed to send encrypted secret key");
    }

    private void onReceiveEncryptedSignal(EventInvocationContext context) {
        try {
            // ciphers
            final Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            final Cipher decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

            connection.getCiphers().setCiphers(encryptCipher, decryptCipher);

            handshakeCompleted = true;

            if(pendingData.size() > 0) {
                final byte[] bufferedData = pendingData.toByteArray();
                pendingData.reset();
                context.send(bufferedData);
            }

            context.connect();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean handleSend(EventInvocationContext context, byte[] data) {
        if(handshakeCompleted)
            return true;

        pendingData.writeBytes(data);
        return false;
    }

}