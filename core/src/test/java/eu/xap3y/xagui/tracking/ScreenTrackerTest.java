package eu.xap3y.xagui.tracking;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ScreenTrackerTest {

    @Test
    public void virtualClassOverridesBackingClassAndTogglesAllItsInstances() {
        ScreenTracker tracker = new ScreenTracker();
        Object first = new Object();
        Object second = new Object();
        Object other = new Object();
        tracker.register(first, Object.class);
        tracker.register(first, ScreenTrackerTest.class);
        tracker.register(second, ScreenTrackerTest.class);
        tracker.register(other, String.class);

        String identity = "eu.xap3y.xagui.tracking@ScreenTrackerTest.java";
        assertEquals(identity, tracker.getIdentity(first));
        assertFalse(tracker.toggle(identity));
        assertTrue(tracker.isDisabled(first));
        assertTrue(tracker.isDisabled(second));
        assertFalse(tracker.isDisabled(other));
        assertTrue(tracker.toggle(identity));
        assertFalse(tracker.isDisabled(first));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownClassCannotBeToggled() {
        new ScreenTracker().toggle("unknown@Menu.java");
    }

    @Test
    public void historyIsNewestFirstAndFiltersOfflinePlayersByNameOrUuid() {
        ScreenTracker tracker = new ScreenTracker();
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        Object menu = new Object();
        tracker.register(menu, ScreenTrackerTest.class);
        tracker.opened(alice, "Alice", menu, "First");
        tracker.closed(alice, menu);
        tracker.opened(bob, "Bob", menu, "Second");
        tracker.closed(bob, menu);
        tracker.opened(alice, "Alice", menu, "Third");
        tracker.closed(alice, menu);

        List<ScreenTracker.ScreenEntry> entries = tracker.getHistory(10, "aLiCe");
        assertEquals(2, entries.size());
        assertEquals("Third", entries.get(0).getTitle());
        assertEquals("First", entries.get(1).getTitle());
        assertEquals(1, tracker.getHistory(1, null).size());
        assertEquals("Second", tracker.getHistory(10, bob.toString()).get(0).getTitle());
        assertTrue(tracker.getHistory(10, "Missing").isEmpty());
        assertTrue(tracker.getHistory(0, null).isEmpty());
    }

    @Test
    public void staleCloseDoesNotRemoveNewSessionAndDurationFreezesOnClose() throws Exception {
        ScreenTracker tracker = new ScreenTracker();
        UUID player = UUID.randomUUID();
        Object first = new Object();
        Object second = new Object();
        tracker.opened(player, "Alice", first, "First");
        ScreenTracker.ScreenEntry firstEntry = tracker.getActive().get(0);
        tracker.opened(player, "Alice", second, "Second");
        assertFalse(firstEntry.isOpen());
        tracker.closed(player, first);
        assertEquals("Second", tracker.getActive().get(0).getTitle());

        ScreenTracker.ScreenEntry entry = tracker.getActive().get(0);
        Field started = entry.getClass().getDeclaredField("startedNanos");
        started.setAccessible(true);
        started.setLong(entry, System.nanoTime() - TimeUnit.SECONDS.toNanos(12));
        assertEquals(12L, entry.getDurationSeconds());
        tracker.closed(player, second);
        assertFalse(entry.isOpen());
        assertEquals(12L, entry.getDurationSeconds());
        assertTrue(tracker.getActive().isEmpty());
    }

    @Test
    public void historyEvictsOldestEntriesWithoutDroppingActiveSessions() {
        ScreenTracker tracker = new ScreenTracker();
        Object menu = new Object();
        UUID activePlayer = UUID.randomUUID();
        tracker.opened(activePlayer, "StillOpen", menu, "Active");
        UUID otherPlayer = UUID.randomUUID();
        for (int index = 0; index <= ScreenTracker.HISTORY_LIMIT; index++) {
            tracker.opened(otherPlayer, "Other", menu, "Menu " + index);
            tracker.closed(otherPlayer, menu);
        }
        List<ScreenTracker.ScreenEntry> entries = tracker.getHistory(Integer.MAX_VALUE, null);
        assertEquals(ScreenTracker.HISTORY_LIMIT, entries.size());
        assertEquals("Menu " + ScreenTracker.HISTORY_LIMIT, entries.get(0).getTitle());
        assertTrue(tracker.getHistory(10, "StillOpen").isEmpty());
        assertEquals("Active", tracker.getActive().get(0).getTitle());
        tracker.closed(activePlayer, menu);
        assertTrue(tracker.getActive().isEmpty());
    }
}
