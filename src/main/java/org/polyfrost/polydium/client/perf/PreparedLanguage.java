package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;

import java.util.List;
import java.util.Map;

public record PreparedLanguage(Map<String, LanguageInfo> languages, List<String> languageStack, boolean defaultRightToLeft, ClientLanguage language, RuntimeException failure) {
}
//?}
