package eu.xap3y.xagui.tracking;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;

/** Runtime screen identities, active sessions and bounded opening history. */
public final class ScreenTracker {

    public static final int HISTORY_LIMIT = 1000;
    private static final ScreenTracker INSTANCE = new ScreenTracker();

    private final Map<Object, String> identities = new WeakHashMap<>();
    private final Set<String> knownClasses = new TreeSet<>();
    private final Set<String> disabledClasses = new HashSet<>();
    private final Map<UUID, Session> active = new HashMap<>();
    private final Deque<ScreenEntry> history = new ArrayDeque<>();

    public static ScreenTracker getInstance() {
        return INSTANCE;
    }

    public static String formatClass(Class<?> type) {
        String name = type.getName();
        int separator = name.lastIndexOf('.');
        return (separator >= 0 ? name.substring(0, separator) : "")
                + "@" + name.substring(separator + 1) + ".java";
    }

    public synchronized void register(Object menu, Class<?> type) {
        String identity = formatClass(type);
        identities.put(menu, identity);
        knownClasses.add(identity);
    }

    public synchronized String getIdentity(Object menu) {
        String identity = identities.get(menu);
        return identity != null ? identity : formatClass(menu.getClass());
    }

    public synchronized List<String> getKnownClasses() {
        return new ArrayList<>(knownClasses);
    }

    /** Returns whether the class is enabled after toggling. */
    public synchronized boolean toggle(String identity) {
        if (!knownClasses.contains(identity)) {
            throw new IllegalArgumentException("Unknown GUI class");
        }
        if (disabledClasses.remove(identity)) {
            return true;
        }
        disabledClasses.add(identity);
        return false;
    }

    public synchronized boolean isDisabled(Object menu) {
        return disabledClasses.contains(getIdentity(menu));
    }

    public synchronized void opened(UUID playerId, String playerName, Object menu, String title) {
        Session previous = active.remove(playerId);
        if (previous != null) {
            previous.entry.finish();
        }
        ScreenEntry entry = new ScreenEntry(playerId, playerName, title, getIdentity(menu));
        active.put(playerId, new Session(menu, entry));
        history.addFirst(entry);
        while (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }
    }

    public synchronized void closed(UUID playerId, Object menu) {
        Session session = active.get(playerId);
        if (session != null && session.menu == menu) {
            session.entry.finish();
            active.remove(playerId);
        }
    }

    public synchronized List<ScreenEntry> getActive() {
        List<ScreenEntry> entries = new ArrayList<>();
        for (Session session : active.values()) {
            entries.add(session.entry);
        }
        Collections.sort(entries, (left, right) -> Long.compare(right.openedAt, left.openedAt));
        return entries;
    }

    public synchronized List<ScreenEntry> getHistory(int count, String player) {
        List<ScreenEntry> entries = new ArrayList<>();
        if (count <= 0) {
            return entries;
        }
        for (ScreenEntry entry : history) {
            if (player == null || entry.playerName.equalsIgnoreCase(player)
                    || entry.playerId.toString().equalsIgnoreCase(player)) {
                entries.add(entry);
                if (entries.size() >= count) {
                    break;
                }
            }
        }
        return entries;
    }

    private static final class Session {
        private final Object menu;
        private final ScreenEntry entry;

        private Session(Object menu, ScreenEntry entry) {
            this.menu = menu;
            this.entry = entry;
        }
    }

    public static final class ScreenEntry {
        private final UUID playerId;
        private final String playerName;
        private final String title;
        private final String className;
        private final long openedAt = System.currentTimeMillis();
        private final long startedNanos = System.nanoTime();
        private volatile Long finishedNanos;

        private ScreenEntry(UUID playerId, String playerName, String title, String className) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.title = title;
            this.className = className;
        }

        private void finish() {
            if (finishedNanos == null) {
                finishedNanos = System.nanoTime();
            }
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getTitle() {
            return title;
        }

        public String getClassName() {
            return className;
        }

        public long getOpenedAt() {
            return openedAt;
        }

        public boolean isOpen() {
            return finishedNanos == null;
        }

        public long getDurationSeconds() {
            Long finished = finishedNanos;
            long end = finished != null ? finished : System.nanoTime();
            return Math.max(0L, (end - startedNanos) / 1_000_000_000L);
        }
    }
}
