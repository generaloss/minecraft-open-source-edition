package minecraftose.client.renderer.chat;

import generaloss.spatialmath.geometry.Intersector;
import generaloss.spatialmath.vector.Vec2f;
import jpize.context.Jpize;
import jpize.opengl.tesselation.GLScissor;
import jpize.opengl.texture.Texture2D;
import jpize.util.font.Font;
import jpize.util.mesh.TextureBatch;
import jpize.util.region.TextureRegion;
import minecraftose.client.chat.Chat;
import minecraftose.client.chat.ChatMessage;
import minecraftose.client.renderer.GameRenderer;
import minecraftose.client.renderer.text.TextComponentBatch;
import minecraftose.main.text.Component;
import generaloss.resourceflow.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRenderer implements Disposable{
    
    private static final float MSG_LIFE_TIME_SEC = 6;
    
    private final GameRenderer gameRenderer;
    private final TextureBatch batch;
    private final TextComponentBatch textBatch;
    private final GLScissor<Integer> scissor;
    private final int chatX, chatY;
    private float scroll;
    private float scrollMotion;
    
    private final Map<Integer, Texture2D> skins;
    private final TextureRegion headRegion, hatRegion;
    
    public ChatRenderer(GameRenderer gameRenderer){
        this.gameRenderer = gameRenderer;
        this.textBatch = new TextComponentBatch();
        this.batch = textBatch.getBatch();
        this.scissor = new GLScissor<>();
        
        chatX = 10;
        chatY = 10;
        
        skins = new HashMap<>();
        for(int i = 1; i <= 21; i++)
            skins.put(i, new Texture2D("/texture/skin/skin" + i + ".png"));
        
        headRegion = new TextureRegion(skins.get(1), 8, 8, 8, 8);
        hatRegion = new TextureRegion(skins.get(1), 40, 8, 8, 8);
    }
    
    
    public void render(){
        batch.setup();
        textBatch.updateScale();
        textBatch.setBackgroundColor(0, 0, 0, 0);
        
        final float chatHeight = Jpize.getHeight() / 2F;
        final float chatWidth = Jpize.getWidth() / 2F;
        final Font font = textBatch.getFont();
        final Vec2f scale = font.getOptions().scale();
        final float lineAdvance = font.getOptions().advanceFactor().y * scale.y;
        
        final Chat chat = gameRenderer.getMinecraft().getChat();
        final List<ChatMessage> messages = chat.getMessages();
        
        final float openedChatY = chatY + (chat.isOpened() ? lineAdvance + 10 : 0);
        float chatMessagesHeight = 0;
        font.getOptions().setLineBreakingWidth(chatWidth);
        for(ChatMessage message: messages)
            chatMessagesHeight += font.getTextBounds(message.getComponents().toString()).y;
        
        // Enter
        if(chat.isOpened()){
            final String enteringText = chat.getEnteringText();
            final float lineWidth = font.getTextWidth(enteringText);
            
            batch.drawRectBlack(chatX, chatY + font.getDescentScaled(), Math.max(lineWidth, chatWidth), lineAdvance,  0.4);
            
            final float cursorLineWidth = font.getTextWidth(enteringText.substring(0, chat.getCursorX()));
            
            textBatch.drawComponent(new Component().text(enteringText), chatX, chatY);

            //{} if(chat.getInput().isCursorRender())
            batch.drawRect(chatX + cursorLineWidth, chatY + font.getDescentScaled(), scale.x, lineAdvance,  1F, 1F, 1F, 1F);
        }
        
        // Scroll
        if(chat.isOpened() && Intersector.isPointOnRect(Jpize.getX(), Jpize.getY(), chatX, openedChatY, chatWidth, chatHeight))
            scrollMotion -= Jpize.getScroll() * Jpize.getDeltaTime() * lineAdvance * 10;
        
        scroll += scrollMotion;
        scrollMotion *= 0.95F;
        
        if(!chat.isOpened())
            scroll = 0;
        
        scroll = Math.min(0, scroll);
        scroll = Math.max(Math.min(0, chatHeight - chatMessagesHeight), scroll);
        
        // Chat
        final float headSize = lineAdvance + 0;
        final float headAdvance = headSize * 1.5F;
        final float headDrawX = (headAdvance - headSize) / 2;
        final float headDrawY = (lineAdvance - headSize) / 2;
        // Scissors begin
        scissor.put(0, chatX, openedChatY + font.getDescentScaled(), chatWidth + headAdvance, chatHeight);
        scissor.apply();
        font.getOptions().setLineBreakingWidth(chatWidth + headAdvance);

        int textAdvanceY = 0;
        for(int i = messages.size() - 1; i >= 0; i--){
            final ChatMessage message = messages.get(i);
            
            float alpha = 1F;
            if(!chat.isOpened()){
                if(message.getSeconds() < MSG_LIFE_TIME_SEC)
                    alpha = (float) Math.min(1, MSG_LIFE_TIME_SEC - message.getSeconds());
                else
                    continue;
            }
            
            final float textHeight = textBatch.getFont().getTextHeight(message.getComponents().toString());
            final float renderChatY = openedChatY + textAdvanceY + scroll;
            final float lineWrapAdvanceY = textHeight - lineAdvance;
            
            final boolean isPlayer = message.getSource().isPlayer();

            // Render background
            batch.drawRectBlack(chatX, renderChatY + font.getDescentScaled(), chatWidth + headAdvance, textHeight,  0.4 * alpha);
            
            // Render head
            if(isPlayer){
                final int skinID = Math.abs(message.getSource().asPlayer().getPlayerName().hashCode()) % 20 + 1;
                final Texture2D skin = skins.get(skinID);
                
                batch.setAlpha(alpha);
                batch.draw(skin, headRegion,  chatX + headDrawX, renderChatY + lineWrapAdvanceY + headDrawY, headSize, headSize);
                
                batch.setTransformOrigin(0.5, 0.5);
                batch.scale().set(1.1);
                batch.draw(skin, hatRegion,  chatX + headDrawX, renderChatY + lineWrapAdvanceY + headDrawY, headSize, headSize);
                batch.scale().set(1);
            }
            // Render text
            textBatch.drawComponents(message.getComponents(), chatX + headAdvance, renderChatY + lineWrapAdvanceY, chatWidth, alpha);
            
            textAdvanceY += (int) textHeight;
        }
        
        batch.render();
        // Scissors end
        scissor.remove(0);
        scissor.apply();
    }
    
    @Override
    public void dispose(){
        batch.dispose();
        textBatch.dispose();
    }
    
}
