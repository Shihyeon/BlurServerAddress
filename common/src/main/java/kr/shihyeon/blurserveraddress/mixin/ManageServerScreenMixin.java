package kr.shihyeon.blurserveraddress.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kr.shihyeon.blurserveraddress.client.BlurServerAddressClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ManageServerScreen.class)
public abstract class ManageServerScreenMixin {

    @Shadow
    protected abstract void updateAddButtonStatus();

    @Unique
    private static final Identifier EYE_TEXTURE = Identifier.fromNamespaceAndPath(BlurServerAddressClient.MODID, "textures/gui/eye.png");
    @Unique
    private static final Identifier BLUE_EYE_TEXTURE = Identifier.fromNamespaceAndPath(BlurServerAddressClient.MODID, "textures/gui/blur_eye.png");

    @WrapOperation(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            ordinal = 1
        )
    )
    private void wrapIpEditRender(EditBox editBox, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, Operation<Void> original) {
        boolean masked = !this.isLCtrlAltPressed();
        String actualValue = editBox.getValue();
        if (masked) {
            int cursorPos = editBox.getCursorPosition();
            int highlightPos = ((EditBoxAccessor) editBox).getHighlightPos();
            editBox.setResponder(_ -> {});
            editBox.setValue("*".repeat(actualValue.length()));
            editBox.setCursorPosition(cursorPos);
            editBox.setHighlightPos(highlightPos);
        }
        original.call(editBox, graphics, mouseX, mouseY, delta);
        if (masked) {
            int cursorPos = editBox.getCursorPosition();
            int highlightPos = ((EditBoxAccessor) editBox).getHighlightPos();
            editBox.setValue(actualValue);
            editBox.setResponder(_ -> this.updateAddButtonStatus());
            editBox.setCursorPosition(cursorPos);
            editBox.setHighlightPos(highlightPos);
        }
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            masked ? BLUE_EYE_TEXTURE : EYE_TEXTURE,
            editBox.getX() + editBox.getWidth() - editBox.getHeight() - 1,editBox.getY(),
            0, 0,
            editBox.getHeight(), editBox.getHeight(),
            editBox.getHeight(), editBox.getHeight()
        );

        if (editBox.isFocused() && this.isMouseOverEyeIcon(editBox, mouseX, mouseY)) {
            this.extractTooltip(graphics, mouseX, mouseY);
        }
    }

    @Unique
    private boolean isLCtrlAltPressed() {
        long windowHandle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
            && GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
    }

    @Unique
    private boolean isMouseOverEyeIcon(EditBox editBox, int mouseX, int mouseY) {
        int iconX = editBox.getX() + editBox.getWidth() - editBox.getHeight() - 1;
        int iconY = editBox.getY();
        int iconSize = editBox.getHeight();
        return mouseX >= iconX && mouseX <= iconX + iconSize
            && mouseY >= iconY && mouseY <= iconY + iconSize;
    }

    @Unique
    private void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        MutableComponent text = Component.translatable("blurserveraddress.text.desc").withStyle(ChatFormatting.RED);
        graphics.setTooltipForNextFrame(Minecraft.getInstance().font, text, mouseX, mouseY);
    }
}
