# GridMates: Sudoku — Offline Nearby Play Without a Wi-Fi Router

**Recorded:** 2026-11-06  
**Status:** **New design/research idea; NOT implemented.** The user asks whether two Android phones could connect and play over Bluetooth without internet or a shared Wi-Fi network. Answer: **Yes.**  
**Current source:** `feat/gridmates-nearby-animals` draft PR #28, protocol version 6, uses nearby LAN discovery and host-authoritative TCP sessions. Basic owner-reported real two-phone smoke test exists for v0.11B, but renewed v0.11C/full reconnect/race physical QA remains important.

## The actual player goal

Two people who know each other can play Sudoku together **anywhere close together**, even at a park, in a waiting room, on a trip or in a building without usable Wi-Fi. No global matchmaking, online accounts or paid hosted servers necessary. Nearby connection should feel as straightforward as **Host → friend taps Join**. Keep LAN as a working choice and offline Solo fully usable.

**The most important requirement is _offline, no-router nearby play_, not necessarily forcing a specific radio technology.** If the owner later insists on Bluetooth-only (Wi-Fi radios off), native Classic Bluetooth is feasible.

## Options, honest trade-offs

| Technology | Works with no internet / shared router? | Good for | Limitations |
|---|---|---|---|
| Existing same-Wi-Fi LAN | Yes internet not required, **router/hotspot local network required** | Already-built baseline and fallback | Both phones must join same reachable Wi-Fi network |
| **Bluetooth Classic RFCOMM sockets** | **Yes**, direct Bluetooth | True Bluetooth-only two-phone puzzle play; low data requirements | Pairing/discovery UX, Bluetooth permissions and OEM behavior; separate stream transport work |
| **Google Nearby Connections** | **Yes**, direct peer-to-peer | Recommended **prototype first** for friendly automatic nearby discovery and encrypted data exchange | Uses Google Play services; chooses Bluetooth, BLE **and/or Wi-Fi** internally, so don't market as strictly Bluetooth-only; permissions and device compatibility testing |
| Wi-Fi Direct / P2P | **Yes**, two phones connect without router | Alternative router-free higher-range local play | Additional Wi-Fi/nearby permissions and peer discovery complexity |

Official references: 
- [Nearby Connections overview](https://developers.google.com/nearby/connections/overview)
- [Nearby Connections Android setup](https://developers.google.com/nearby/connections/android/get-started)
- [Bluetooth classic connection API](https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices)
- [Android Bluetooth permission model](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions)
- [Wi-Fi Direct connections](https://developer.android.com/develop/connectivity/wifi/wifi-direct)

Google Nearby Connections describes **fully offline** discovery and communication, and automatically selects among several nearby radio transports. It's a Google Play services API, not a custom host/server service. There is no obvious per-match backend to pay for in a direct peer-to-peer design; review any applicable SDK and app policy terms when implementing rather than promising universal zero cost.

## Proposed simple UX

```text
Play Together
   [Host nearby game]      [Join nearby game]
        ↓                         ↓
   Room opens           Available nearby rooms appear
        ↓                 ↓
   Friend taps          Optional confirmation / device code
        ↓                         ↓
           Play co-op or versus
```

**Avoid demanding a technical choice** among LAN, Bluetooth, Wi-Fi Direct when possible. A user can have one **Nearby** joining screen and an unobtrusive advanced connection/fallback control.

**Recommended staged experiment**:
1. Keep existing same-Wi-Fi LAN unchanged; build a small isolated Nearby Connections **2-device prototype**, not a mass refactor.
2. Test hosting and joining with **mobile data off and neither device connected to a Wi-Fi router**. Depending on the underlying transport, Wi-Fi radios may still need to be on.
3. Verify actual sudoku synchronization and player identity: authoritative host, edits/notes/undo, race completion, chat, reconnect/rejoin, app background/return and friendly cancellation. Use existing protocol messages where possible, but **do not assume current TCP socket bytes fit the Nearby Connections API unchanged**; introduce a small transport/message adapter.
4. Request system permissions **only when needed**, with plain explanation. On Android 12+, Bluetooth commonly needs Nearby Devices runtime permissions; Google Nearby Connections can also require Wi-Fi/location permissions by OS and transport. Denied permissions must show actionable recovery, not break Solo or existing LAN.
5. Test on both real phones first, then additional Android versions. Confirm if Play services availability causes failure, and ensure graceful same-Wi-Fi fallback.
6. Add clear "Connected nearby" status, version compatibility check and harmless safe disconnect. No app signing/package/save/reset to try a new transport.
7. Only after proof, decide whether to make Nearby automatic and LAN a fallback or to keep explicit modes based on reliability and ease of use.

**Safety/compatibility constraints:** Do not silently join unknown nearby hosts; use explicit peer confirmation and existing room admission/optional code policy. Existing protocol6/game validation, hidden player data and host authority must remain intact. No request to build internet lobbies, public friend graphs or new cloud accounts.

### What this is not

- **Not** a statement that current v0.11C Bluetooth play already works.
- **Not** a decision that Nearby Connections is the only solution; Bluetooth Classic may be the better fallback if the owner values BT-only and Google Play services independence.
- **Not** a reason to drop working LAN or bypass current physical two-phone acceptance.
- **Not** a commitment to add online multiplayer, service fees, or server infrastructure.

**Work chat priority:** Put this behind active stability/Play-release work until owner requests an implementation experiment. Aim for an isolated prototype + QA checkpoint before changing user-facing flow.

**Related:** [Project Handoff](PROJECT_HANDOFF.md) · [GridMates Brand](GRIDMATES_BRAND_AND_SERIES.md).
