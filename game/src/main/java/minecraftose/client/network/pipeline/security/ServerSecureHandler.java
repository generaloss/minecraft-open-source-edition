package minecraftose.client.network.pipeline.security;

import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.tcp.listener.CloseReason;
import generaloss.networkforge.tcp.pipeline.EventHandler;
import generaloss.networkforge.tcp.pipeline.EventInvocationContext;
import generaloss.resourceflow.stream.BinaryInputStream;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerSecureHandler extends EventHandler {

    public static final int RSA_KEY_SIZE = 2048;

    private final KeyPair keyPair;
    private final Map<TCPConnection, ByteArrayOutputStream> pendingDataMap;
    private final ConcurrentLinkedQueue<TCPConnection> handshakeCompleted;

    public ServerSecureHandler() {
        try {
            final KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            pairGenerator.initialize(RSA_KEY_SIZE);
            keyPair = pairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException ignored) {
            throw new RuntimeException("Failed to generate RSA key pair");
        }

        this.pendingDataMap = new ConcurrentHashMap<>();
        this.handshakeCompleted = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean handleConnect(EventInvocationContext context) {
        this.sendPublicKey(context);
        return false;
    }

    @Override
    public boolean handleDisconnect(EventInvocationContext context, CloseReason reason) {
        final TCPConnection connection = context.getConnection();
        handshakeCompleted.remove(connection);
        pendingDataMap.remove(connection);
        return true;
    }

    private void sendPublicKey(EventInvocationContext context) {
        final boolean success = context.send(stream -> {
            stream.writeByte(SecureBinaryFrames.PUBLIC_KEY.ordinal());
            stream.writeByteArray(keyPair.getPublic().getEncoded());
        });
        if(!success)
            throw new RuntimeException("Failed to send public key");
    }

    @Override
    public boolean handleReceive(EventInvocationContext context, byte[] data) {
        if(handshakeCompleted.contains(context.getConnection()))
            return true;

        try (final BinaryInputStream stream = new BinaryInputStream(data)) {
            final int binaryFrame = stream.readByte();
            if(binaryFrame == SecureBinaryFrames.ENCRYPTED_SECRET_KEY.ordinal()) {
                // ENCRYPTED_SECRET_KEY
                final byte[] encryptedSecretKey = stream.readByteArray();
                this.onReceiveEncryptedSecretKey(context, encryptedSecretKey);
            } else {
                // ?
                throw new RuntimeException("Invalid binary frame " + binaryFrame);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read binary frame", e);
        }
        return false;
    }

    private void onReceiveEncryptedSecretKey(EventInvocationContext context, byte[] encryptedSecretKeyBytes) {
        try {
            final TCPConnection connection = context.getConnection();

            final PrivateKey privateKey = keyPair.getPrivate();

            final Cipher privateEncryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            privateEncryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

            final byte[] keyBytes = privateEncryptCipher.doFinal(encryptedSecretKeyBytes);

            try {
                // key
                final SecretKey key = new SecretKeySpec(keyBytes, "AES");

                // ciphers
                final Cipher encryptCipher = Cipher.getInstance("AES");
                encryptCipher.init(Cipher.ENCRYPT_MODE, key);

                final Cipher decryptCipher = Cipher.getInstance("AES");
                decryptCipher.init(Cipher.DECRYPT_MODE, key);

                this.sendConnectionEncryptedSignal(context);

                connection.getCiphers().setCiphers(encryptCipher, decryptCipher);

                handshakeCompleted.add(context.getConnection());

                if(pendingDataMap.containsKey(connection)) {
                    final ByteArrayOutputStream pendingData = pendingDataMap.remove(connection);
                    final byte[] bufferedData = pendingData.toByteArray();
                    pendingData.reset();

                    context.send(bufferedData);
                }

                context.connect();

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e){
                throw new RuntimeException(e);
            }

        } catch (GeneralSecurityException e){
            throw new RuntimeException(e);
        }
    }

    private void sendConnectionEncryptedSignal(EventInvocationContext context) {
        final boolean success = context.send(stream ->
            stream.writeByte(SecureBinaryFrames.CONNECTION_ENCRYPTED_SIGNAL.ordinal()));
        if(!success)
            throw new RuntimeException("Failed to send connection encrypted signal");
    }

    @Override
    public boolean handleSend(EventInvocationContext context, byte[] data) {
        if(handshakeCompleted.contains(context.getConnection()))
            return true;

        final TCPConnection connection = context.getConnection();
        if(!pendingDataMap.containsKey(connection))
            pendingDataMap.put(connection, new ByteArrayOutputStream());

        pendingDataMap.get(connection).writeBytes(data);
        return false;
    }

}