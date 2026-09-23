package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OwnChatGuardTest {
    private final OwnChatGuard guard = new OwnChatGuard();

    @Test
    void theFlagIsOffBeforeAnySend() {
        assertFalse(guard.sending());
    }

    @Test
    void theFlagIsOnForTheSendAndOffAgainAfterIt() {
        List<Boolean> seen = new ArrayList<>();
        guard.around(() -> seen.add(guard.sending()));
        assertEquals(List.of(true), seen);
        assertFalse(guard.sending());
    }

    @Test
    void theFlagIsClearedWhenTheSendThrows() {
        assertThrows(IllegalStateException.class, () -> guard.around(() -> {
            throw new IllegalStateException("another mod's listener blew up");
        }));
        assertFalse(guard.sending());
    }

    @Test
    void theFlagIsClearedWhenTheSendThrowsAnError() {
        assertThrows(NullPointerException.class, () -> guard.around(() -> {
            throw new NullPointerException("no player during a disconnect");
        }));
        assertFalse(guard.sending());
        List<Boolean> seen = new ArrayList<>();
        guard.around(() -> seen.add(guard.sending()));
        assertEquals(List.of(true), seen);
        assertFalse(guard.sending());
    }

    @Test
    void aNestedSendDoesNotClearTheFlagWhileTheOuterSendIsStillRunning() {
        List<Boolean> seen = new ArrayList<>();
        guard.around(() -> {
            seen.add(guard.sending());
            guard.around(() -> seen.add(guard.sending()));
            seen.add(guard.sending());
        });
        assertEquals(List.of(true, true, true), seen);
        assertFalse(guard.sending());
    }

    @Test
    void aNestedSendThatThrowsDoesNotClearTheFlagForTheOuterSend() {
        List<Boolean> seen = new ArrayList<>();
        guard.around(() -> {
            assertThrows(IllegalStateException.class, () -> guard.around(() -> {
                throw new IllegalStateException("nested send blew up");
            }));
            seen.add(guard.sending());
        });
        assertEquals(List.of(true), seen);
        assertFalse(guard.sending());
    }

    @Test
    void theFlagIsOffAgainAfterAThrowThatEscapesAnOuterSend() {
        assertThrows(IllegalStateException.class, () -> guard.around(() -> guard.around(() -> {
            throw new IllegalStateException("nested send blew up");
        })));
        assertFalse(guard.sending());
    }

    @Test
    void sendingStaysTrueForEveryDepthOfANestedSend() {
        guard.around(() -> guard.around(() -> guard.around(() -> assertTrue(guard.sending()))));
        assertFalse(guard.sending());
    }
}
