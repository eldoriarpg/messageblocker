---
title: What is held
sidebar_position: 4
---

# What is held

What the blocker can hold depends on whether ProtocolLib is installed and enabled before your plugin.

| Message | Without ProtocolLib | With ProtocolLib |
|---|---|---|
| Player chat (through Paper's chat event) | held | held |
| System messages, including plain `player.sendMessage` from plugins | not held | held |
| Disguised chat (unsigned chat with a chat type) | not held | held |
| Action bar messages | not held | not held |
| Signed command messages, such as `/msg` | not held | not held |
| Messages sent with `sendBypassing` | not held | not held |

ProtocolLib is used when two things are true: `interceptSystemMessages` is on (the default), and ProtocolLib is
already enabled when you create the blocker. List it in your `plugin.yml` so the server enables it first. See
[Setup](./setup.mdx).

## Player chat

Player chat is held with Paper's own chat event, so it works without ProtocolLib.

The blocker listens to `AsyncChatEvent` at the `HIGHEST` priority. By then, other plugins have already changed the
viewers and the renderer. For every blocked viewer, the blocker takes the player out of the viewers and puts a
stand-in in their place. Paper renders the message for the stand-in as it would for the player, and the stand-in
puts it into the player's queue. The server does not send anything to that player yet.

If a plugin cancels the chat event, the blocker does not touch it.

## Signed chat

Since Minecraft 1.19, players sign their chat messages. The game client checks those signatures. Messages from one
sender must arrive in order, must belong to the sender's current session and must not be too old.

Held chat is not sent as an old copy of a packet. On unblock, the blocker sends it through the server's normal
chat path, as if it were new. The server builds the signature chain for the player at that moment, so the
signatures stay valid.

Sometimes a signature can no longer be accepted. Then the blocker sends the message as unsigned chat instead.
It sends the content the player would have seen, with the same chat type, just without the signature. This
happens when:

- the sender has left or joined again since the message was held,
- the message was signed more than seven minutes ago,
- a newer signed message from the same sender has already reached the player directly, for example a `/msg`.

:::warning
The last case is only detected with ProtocolLib. Without it, the blocker cannot see signed messages that go
around the chat event. If a blocked player gets a `/msg` from someone whose chat is also held, that held chat is
later sent signed, after the newer `/msg`. This breaks the order the client expects for that sender.
:::

## System messages and disguised chat

With ProtocolLib, the blocker also watches system chat packets and disguised chat packets to a blocked player.
It holds them and sends the same packets again on unblock.

These messages always pass:

- action bar messages,
- messages sent with `sendBypassing`.

`sendBypassing` adds an invisible marker to your message. It shows no text and cannot be shift-clicked. The
blocker looks for this marker in the packet and lets the message through.

Signed player chat packets, for example from `/msg`, are never held. By the time such a packet is sent, the
server has already counted it for the player. The blocker only notes who sent it, for the signature check above.

## Limits

- Each player's queue holds 100 messages by default. When it is full, the oldest message is dropped. Change the
  size with `queueSize` on the [builder](./usage.md).
- Held messages of a player are dropped when that player leaves.
- If ProtocolLib cannot read a chat packet, the blocker lets that packet through and logs a warning once. With a
  ProtocolLib version that cannot read chat packets at all, system messages are not held. Player chat is still held.

## Things to know

- **Plugins that rebroadcast chat.** Some chat plugins cancel `AsyncChatEvent` and send the message on their own.
  That chat is not held as player chat. If they send it as system messages, it is held only with ProtocolLib.
- **Renderers that look at the viewer.** A chat renderer gets the stand-in instead of the `Player` for a blocked
  viewer. Its UUID, name and display name are the player's, but it is not a `Player`. Code that checks
  `viewer instanceof Player` will not match it.
- **Several plugins with Message Blocker.** Relocating the library also changes the marker that `sendBypassing`
  adds, so every plugin's copy has its own. If two plugins block the same player at the same time and ProtocolLib
  is installed, each one holds the other's `sendBypassing` messages until it unblocks the player.
