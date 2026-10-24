/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import net.kyori.adventure.text.Component;

/**
 * Marks components that have to reach a viewer even while their messages are held.
 * <p>
 * The marker is an empty text child carrying {@link #TOKEN} as insertion. It renders as nothing and cannot be
 * shift-clicked, but survives serialization, so the packet interceptor finds it in the packet content.
 */
final class BypassMarker {
    /**
     * Insertion token carried by marked components.
     */
    static final String TOKEN = "de.eldoria.messageblocker:bypass";

    private static final Component MARKER = Component.text("").insertion(TOKEN);

    private BypassMarker() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Marks a component.
     *
     * @param component component to mark
     * @return a component containing the original component and the marker
     */
    static Component mark(Component component) {
        return Component.text().append(component).append(MARKER).build();
    }

    /**
     * Checks whether serialized component content contains the marker.
     *
     * @param serialized serialized component, for example json
     * @return true if the marker is present
     */
    static boolean isMarked(String serialized) {
        return serialized.contains(TOKEN);
    }
}
