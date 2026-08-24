package minecraftose.client.renderer.text;

import jpize.context.Jpize;
import jpize.util.color.Color;
import jpize.util.font.Font;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec2f;
import jpize.util.font.GlyphIterator;
import jpize.util.font.GlyphSprite;
import jpize.util.mesh.TextureBatch;
import jpize.util.region.TextureRegion;
import minecraftose.main.text.Component;
import minecraftose.main.text.ComponentText;
import minecraftose.main.text.TextStyle;
import generaloss.resourceflow.Disposable;

import java.util.List;

public class TextComponentBatch implements Disposable{
    
    private final TextureBatch batch;
    private final Font font;
    private final Color background;
    
    public TextComponentBatch() {
        this.batch = new TextureBatch();
        this.background = new Color();

        this.font = new Font().loadFNT("/font/minecraft/default.fnt", false);
        //this.font = new Font().loadTrueType("/font/faithful/faithful.ttf", 64, false);
        this.font.getOptions().newLineGap = 1;
    }

    public void updateScale() {
        font.getOptions().scale().set(Maths.round(Jpize.getHeight() / 330F));
    }
    
    
    public void drawComponents(List<ComponentText> components, float x, float y, float width, float alpha){
        // Подготовим batch и font
        batch.setup();

        final Vec2f scale = font.getOptions().scale();

        // Позиция следующего символа
        // Фон
        if(background.alpha != 0){
            final StringBuilder text = new StringBuilder();
            for(ComponentText component: components)
                text.append(component.toString());
            
            final Vec2f bounds = font.getTextBounds(text.toString());
            batch.drawRect(x, y, bounds.x, bounds.y,  background);
        }

        float cursorX = 0;
        float cursorY = 0;

        // Пройдемся по каждому компоненту дерева, разложенному в плоский список
        for(ComponentText component: components){
            // Стиль компонента
            final TextStyle style = component.getStyle();
            batch.shear(style.isItalic() ? 15F : 0F, 0F);
            
            // Цвет компонента
            final Color color = component.getColor().copy();
            color.setAlpha(alpha);
            
            // Текст
            final String text = component.getText();
            final Iterable<GlyphSprite> iterable = font.iterable(text);

            // Рендерим каждый символ
            for(GlyphSprite glyph: iterable){
                final float renderX = (glyph.getX() + x + cursorX);
                final float renderY = (glyph.getY() + y + cursorY);
                final TextureRegion region = new TextureRegion(glyph.getPage(), glyph.getRegion());
                // Рендерим тень
                batch.draw(region, renderX + scale.x, renderY - scale.y, glyph.getWidth(), glyph.getHeight(), color.copy().mulRGB(0.1));
                // Рендерим основной символ
                batch.draw(region, renderX, renderY, glyph.getWidth(), glyph.getHeight(), color);
            }

            // Прибавляем позицию для следующей части текста
            final GlyphIterator iterator = (GlyphIterator) iterable.iterator();
            cursorX += iterator.nextCursorX() * scale.x;
            cursorY += iterator.nextCursorY() * scale.y;
        }
        
        // Закончим отрисовку
        batch.shear(0, 0);
        batch.resetColor();
        batch.render();
    }
    
    
    public void drawComponents(List<ComponentText> components, float x, float y, float alpha){
        drawComponents(components, x, y, -1, alpha);
    }
    
    public void drawComponents(List<ComponentText> components, float x, float y){
        drawComponents(components, x, y, 1F);
    }
    
    
    public void drawComponent(Component component, float x, float y, float width, float alpha){
        drawComponents(component.toFlatList(), x, y, width, alpha);
    }
    
    public void drawComponent(Component component, float x, float y, float alpha){
        drawComponents(component.toFlatList(), x, y, -1, alpha);
    }
    
    public void drawComponent(Component component, float x, float y){
        drawComponents(component.toFlatList(), x, y, 1F);
    }
    
    
    public TextureBatch getBatch(){
        return batch;
    }
    
    public Font getFont(){
        return font;
    }
    
    
    public void setBackgroundColor(double r, double g, double b, double a){
        background.set(r, g, b, a);
    }
    
    public void setBackgroundColor(double r, double g, double b){
        background.set(r, g, b);
    }
    
    public void setBackgroundColor(Color color){
        background.set(color);
    }
    
    
    @Override
    public void dispose(){
        batch.dispose();
        font.dispose();
    }

}
