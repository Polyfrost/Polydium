package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterDirection;

public final class LtrText {
    private static final int FIRST_RTL_CODE_POINT = 0x0590;

    private static final int REORDERING_CLASSES = 1 << UCharacterDirection.RIGHT_TO_LEFT
            | 1 << UCharacterDirection.RIGHT_TO_LEFT_ARABIC
            | 1 << UCharacterDirection.ARABIC_NUMBER
            | 1 << UCharacterDirection.LEFT_TO_RIGHT_EMBEDDING
            | 1 << UCharacterDirection.LEFT_TO_RIGHT_OVERRIDE
            | 1 << UCharacterDirection.RIGHT_TO_LEFT_EMBEDDING
            | 1 << UCharacterDirection.RIGHT_TO_LEFT_OVERRIDE
            | 1 << UCharacterDirection.POP_DIRECTIONAL_FORMAT
            | 1 << UCharacterDirection.LEFT_TO_RIGHT_ISOLATE
            | 1 << UCharacterDirection.RIGHT_TO_LEFT_ISOLATE
            | 1 << UCharacterDirection.FIRST_STRONG_ISOLATE
            | 1 << UCharacterDirection.POP_DIRECTIONAL_ISOLATE;

    private LtrText() {
    }

    public static boolean isSingleLtrRun(String text) {
        for (int i = 0, length = text.length(); i < length; ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint >= FIRST_RTL_CODE_POINT && (REORDERING_CLASSES & 1 << UCharacter.getDirection(codePoint)) != 0) {
                return false;
            }
        }
        return true;
    }
}
//?}
