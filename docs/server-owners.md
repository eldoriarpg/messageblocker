---
title: What server owners see
sidebar_position: 5
---

# What server owners see

Message Blocker has no config file, no commands and no permissions. Server owners only notice it through the
chat pausing and through a few lines in the console. Those lines carry your plugin's name, because the blocker
logs with your plugin's logger.

## Log lines

| Level | Message | When |
|---|---|---|
| WARNING | `Paper chat API not found. Messages will not be blocked.` | The server has no Paper chat API. You got a dummy blocker. |
| WARNING | `ProtocolLib is not listed as depend or softdepend. System messages are only held when ProtocolLib is enabled before this plugin.` | `interceptSystemMessages` is on and your `plugin.yml` does not list ProtocolLib. Printed even if ProtocolLib is not installed. |
| INFO | `ProtocolLib is not enabled. Only player chat will be held.` | `interceptSystemMessages` is on and ProtocolLib is not enabled when the blocker is created. |
| WARNING | `Could not read chat packet content. System messages will not be held.` | ProtocolLib could not read a chat packet. Printed once, with the error. |
| WARNING | `Could not deliver held message to <player>` | Sending one held message failed. The other held messages are still sent. |

The first three lines can only appear when your plugin creates the blocker. To keep the ProtocolLib lines out
of the console:

- list ProtocolLib as `softdepend` in your `plugin.yml`, or
- turn system messages off with `interceptSystemMessages(false)` if you do not want them held.

At the `FINE` level, the blocker also logs each block, each unblock and how many held messages it delivered.
Servers do not show `FINE` messages by default.

## Tell your users

Your users are server owners. They cannot see from the outside that your plugin uses Message Blocker. A short
note on your plugin page helps them:

- Chat is paused for a player while your menu is open, and comes back afterwards.
- With ProtocolLib installed, system messages from other plugins are paused too. Without it, only player chat is.
