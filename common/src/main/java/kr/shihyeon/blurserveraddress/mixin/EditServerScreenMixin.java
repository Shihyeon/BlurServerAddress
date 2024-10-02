package kr.shihyeon.blurserveraddress.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import kr.shihyeon.blurserveraddress.client.BlurServerAddressClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditServerScreen.class)
public abstract class EditServerScreenMixin {

    @Unique
    private final Minecraft client = Minecraft.getInstance();

    @Shadow
    private EditBox ipEdit;

    @Unique
    private String actualAddress = "";

    @Unique
    private static final ResourceLocation EYE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlurServerAddressClient.MODID, "textures/gui/eye.png");
    @Unique
    private static final ResourceLocation BLUE_EYE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlurServerAddressClient.MODID, "textures/gui/blur_eye.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void maskAddressField(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        actualAddress = ipEdit.getValue();

        if (!isLCtrlAltPressed()) {
            String maskedAddress = "*".repeat(actualAddress.length());
            ipEdit.setValue(maskedAddress);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void restoreAddressField(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ipEdit.setValue(actualAddress);

        RenderSystem.defaultBlendFunc();
        if (!isLCtrlAltPressed()) {
            context.blit(BLUE_EYE_TEXTURE, ipEdit.getX() + ipEdit.getWidth() - ipEdit.getHeight() - 1, ipEdit.getY(), 0, 0, ipEdit.getHeight(), ipEdit.getHeight(), ipEdit.getHeight(), ipEdit.getHeight());
        } else {
            context.blit(EYE_TEXTURE, ipEdit.getX() + ipEdit.getWidth() - ipEdit.getHeight() - 1, ipEdit.getY(), 0, 0, ipEdit.getHeight(), ipEdit.getHeight(), ipEdit.getHeight(), ipEdit.getHeight());
        }
        RenderSystem.disableBlend();

        if (ipEdit.isFocused() && isMouseOverAddressField(mouseX, mouseY)) {
            renderTooltip(context, mouseX, mouseY);
        }
    }

    @Unique
    private boolean isLCtrlAltPressed() {
        long windowHandle = client.getWindow().getWindow();
        return GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                && GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
    }

    @Unique
    private boolean isMouseOverAddressField(int mouseX, int mouseY) {
        int addressX = ipEdit.getX();
        int addressY = ipEdit.getY();
        int addressWidth = ipEdit.getWidth();
        int addressHeight = ipEdit.getHeight();

        return mouseX >= (addressX + addressWidth - addressHeight - 1) && mouseX <= (addressX + addressWidth - 1)
                && mouseY >= (addressY) && mouseY <= (addressY + addressHeight);
    }

    @Unique
    private void renderTooltip(GuiGraphics context, int mouseX, int mouseY) {
        String defaultText = "Press L-Ctrl+Alt to view original address!";
        MutableComponent tooltipText;
        String translatedText = I18n.get("blurserveraddress.text.desc");
        if (translatedText.equals("blurserveraddress.text.desc")) {
            tooltipText = Component.literal(defaultText);
        } else {
            tooltipText = Component.translatable("blurserveraddress.text.desc");
        }
        context.renderTooltip(client.font, tooltipText.setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), mouseX, mouseY);
    }
}