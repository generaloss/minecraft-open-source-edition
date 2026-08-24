package minecraftose.main.text;

import jpize.util.color.ImmutableColor;

public enum TextColor{
    
    BLACK        (new ImmutableColor(0.0F, 0.0F, 0.0F)),
    DARK_BLUE    (new ImmutableColor(0.0F, 0.0F, 0.7F)),
    DARK_GREEN   (new ImmutableColor(0.0F, 0.7F, 0.0F)),
    DARK_AQUA    (new ImmutableColor(0.0F, 0.7F, 0.7F)),
    DARK_RED     (new ImmutableColor(0.8F, 0.0F, 0.0F)),
    DARK_PURPLE  (new ImmutableColor(0.7F, 0.0F, 0.7F)),
    ORANGE       (new ImmutableColor(1.0F, 0.7F, 0.0F)),
    GRAY         (new ImmutableColor(0.7F, 0.7F, 0.7F)),
    DARK_GRAY    (new ImmutableColor(0.3F, 0.3F, 0.3F)),
    BLUE         (new ImmutableColor(0.1F, 0.6F, 1.0F)),
    GREEN        (new ImmutableColor(0.3F, 1.0F, 0.3F)),
    AQUA         (new ImmutableColor(0.3F, 1.0F, 1.0F)),
    RED          (new ImmutableColor(1.0F, 0.3F, 0.3F)),
    LIGHT_PURPLE (new ImmutableColor(1.0F, 0.3F, 1.0F)),
    YELLOW       (new ImmutableColor(1.0F, 1.0F, 0.3F)),
    WHITE        (new ImmutableColor(1.0F, 1.0F, 1.0F));
    
    public final ImmutableColor color;
    
    TextColor(ImmutableColor color){
        this.color = color;
    }
    
}
