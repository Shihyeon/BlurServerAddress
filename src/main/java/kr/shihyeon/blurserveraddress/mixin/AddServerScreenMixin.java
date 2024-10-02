package kr.shihyeon.blurserveraddress.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AddServerScreen.class)
public abstract class AddServerScreenMixin {

    @Shadow
    private TextFieldWidget addressField;

    @Unique
    private String actualAddress = "";

    @Inject(method = "render", at = @At("HEAD"))
    private void maskAddressField(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        actualAddress = addressField.getText();
        String maskedAddress = "*".repeat(actualAddress.length());
        addressField.setText(maskedAddress);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void restoreAddressField(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        addressField.setText(actualAddress);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void captureActualAddress(CallbackInfo ci) {
        actualAddress = addressField.getText();
    }
}