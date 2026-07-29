package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleForm;

@Environment(EnvType.CLIENT)
public final class TitleNames {
    private TitleNames() {
    }

    public static MutableComponent styled(Title title, TitleForm form) {
        MutableComponent name = base(title, form);
        return LegacyClientConfig.titleColors ? name.withStyle(title.getRarity().getColor()) : name;
    }

    public static MutableComponent plain(Title title, TitleForm form) {
        return base(title, form);
    }

    public static String string(Title title, TitleForm form) {
        return base(title, form).getString();
    }

    private static MutableComponent base(Title title, TitleForm form) {
        if (!title.hasExplicitTranslationKey() && title.literalName() != null) {
            return Component.literal(title.literalName());
        }
        String variant = title.getTranslationKey() + form.suffix();
        return Component.translatable(I18n.exists(variant) ? variant : title.getTranslationKey());
    }
}
