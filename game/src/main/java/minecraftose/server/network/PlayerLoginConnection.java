package minecraftose.server.network;

import generaloss.networkforge.tcp.TCPConnection;
import minecraftose.client.network.pipeline.security.AESKey;
import minecraftose.main.network.PlayerProfile;
import minecraftose.main.network.packet.s2c.game.S2CPacketDisconnect;
import minecraftose.main.network.packet.s2c.login.S2CPacketEncryptStart;
import minecraftose.main.network.packet.c2s.login.C2SPacketAuth;
import minecraftose.main.network.packet.c2s.login.C2SPacketEncryptEnd;
import minecraftose.main.network.packet.c2s.login.C2SPacketLogin;
import minecraftose.server.Server;

public class PlayerLoginConnection implements ServerPacketHandler {
    
    private final Server server;
    private final TCPConnection connection;
    
    private String profileName;
    private int versionID;
    
    public PlayerLoginConnection(Server server, TCPConnection connection){
        this.server = server;
        this.connection = connection;
    }
    
    
    public void login(C2SPacketLogin packet){
        profileName = packet.profileName;
        versionID = packet.clientVersionID;
        
        if(versionID != server.getConfiguration().getVersion().getID()){
            connection.send(new S2CPacketDisconnect("Server not available on your game version"));
            return;
        }
        
        if(PlayerProfile.isNameInvalid(profileName)){
            connection.send(new S2CPacketDisconnect("Invalid player name"));
            return;
        }
        
        if(server.getPlayerList().isPlayerOnline(profileName)){
            connection.send(new S2CPacketDisconnect("Player with your nickname already plays on the server"));
            return;
        }

        connection.send(new S2CPacketEncryptStart(server.getConnectionManager().getRsaKey().getPublic()));
    }
    
    public void encryptEnd(C2SPacketEncryptEnd packet){
        final AESKey decryptedClientKey = new AESKey(server.getConnectionManager().getRsaKey().decrypt(packet.encryptedClientKey));
        // connection.getCiphers().setCiphers(
        //     decryptedClientKey.getEncryptCipher(),
        //     decryptedClientKey.getDecryptCipher()
        // );
    }
    
    public void auth(C2SPacketAuth packet){
        if(!"54_54-iWantPizza-54_54".equals(packet.accountSessionToken)){
            connection.send(new S2CPacketDisconnect("Invalid session"));
            return;
        }
        
        server.getPlayerList().addNewPlayer(profileName, connection);
    }
    
}
