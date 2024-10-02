package kr.shihyeon.blurserveraddress.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import kr.shihyeon.blurserveraddress.client.BlurServerAddressClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AddServerScreen.class)
public abstract class AddServerScreenMixin {

    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Shadow
    private TextFieldWidget addressField;

    @Unique
    private String actualAddress = "";

    @Unique
    private static final Identifier EYE_TEXTURE = Identifier.of(BlurServerAddressClient.MODID, "textures/gui/eye.png");
    @Unique
    private static final Identifier BLUE_EYE_TEXTURE = Identifier.of(BlurServerAddressClient.MODID, "textures/gui/blur_eye.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void maskAddressField(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        actualAddress = addressField.getText();

        if (!isLCtrlAltPressed()) {
            String maskedAddress = "*".repeat(actualAddress.length());
            addressField.setText(maskedAddress);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void restoreAddressField(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        addressField.setText(actualAddress);

        RenderSystem.defaultBlendFunc();
        if (!isLCtrlAltPressed()) {
            context.drawTexture(BLUE_EYE_TEXTURE, addressField.getX() + addressField.getWidth() - addressField.getHeight() - 1, addressField.getY(), 0, 0, addressField.getHeight(), addressField.getHeight(), addressField.getHeight(), addressField.getHeight());
        } else {
            context.drawTexture(EYE_TEXTURE, addressField.getX() + addressField.getWidth() - addressField.getHeight() - 1, addressField.getY(), 0, 0, addressField.getHeight(), addressField.getHeight(), addressField.getHeight(), addressField.getHeight());
        }
        RenderSystem.disableBlend();

        if (addressField.isFocused() && isMouseOverAddressField(mouseX, mouseY)) {
            renderTooltip(context, mouseX, mouseY);
        }
    }

    @Unique
    private boolean isLCtrlAltPressed() {
        long windowHandle = client.getWindow().getHandle();
        return GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                && GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
    }

    @Unique
    private boolean isMouseOverAddressField(int mouseX, int mouseY) {
        int addressX = addressField.getX();
        int addressY = addressField.getY();
        int addressWidth = addressField.getWidth();
        int addressHeight = addressField.getHeight();

        return mouseX >= (addressX + addressWidth - addressHeight - 1) && mouseX <= (addressX + addressWidth - 1)
                && mouseY >= (addressY) && mouseY <= (addressY + addressHeight);
    }

    @Unique
    private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        String defaultText = "Press L-Ctrl+Alt to view original address!";
        MutableText tooltipText;
        String translatedText = I18n.translate("blurserveraddress.text.desc");
        if (translatedText.equals("blurserveraddress.text.desc")) {
            tooltipText = Text.literal(defaultText);
        } else {
            tooltipText = Text.translatable("blurserveraddress.text.desc");
        }
        context.drawTooltip(client.textRenderer, tooltipText.setStyle(Style.EMPTY.withColor(Formatting.RED)), mouseX, mouseY);
    }
}