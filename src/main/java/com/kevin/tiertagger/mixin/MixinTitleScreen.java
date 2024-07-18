package com.kevin.tiertagger.mixin;

import com.kevin.tiertagger.TierTagger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    @Unique
    private static boolean hasCheckedVersion = false;

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void showUpdateScreen(CallbackInfo ci) {
        if (hasCheckedVersion) return;

        Version currentVersion = FabricLoader.getInstance().getModContainer("tier-tagger").map(m -> m.getMetadata().getVersion()).orElse(null);
        Version latestVersion = TierTagger.getLatestVersion();

        if (currentVersion != null && latestVersion != null && currentVersion.compareTo(latestVersion) < 0) {
            Text newVersion = new LiteralText(latestVersion.getFriendlyString()).formatted(Formatting.GREEN);

            MinecraftClient.getInstance().setScreen(new ConfirmScreen(
                    b -> {
                        if (b) {
                            String url = "https://modrinth.com/mod/tiertagger/version/" + latestVersion.getFriendlyString();
                            Util.getOperatingSystem().open(url);
                        }

                        MinecraftClient.getInstance().setScreen(this);
                    },
                    new TranslatableText("tiertagger.outdated.title"),
                    new TranslatableText("tiertagger.outdated.desc", newVersion),
                    new TranslatableText("tiertagger.outdated.download"),
                    new TranslatableText("tiertagger.outdated.ignore")
            ));
        }

        hasCheckedVersion = true;
    }
}
