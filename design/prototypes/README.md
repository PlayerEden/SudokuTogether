# Player presence and menu prototype

Review-only interaction sketch for the November 4, 2026 GridMates design. It does not change the Android app. See the [design](../../docs/superpowers/specs/2026-11-04-player-presence-and-menu-design.md) and [update/save preparation](../../docs/GRIDMATES_UPDATE_AND_SAVE_PREPARATION.md).

`player-presence.fragment.html` contains the layout, interactions and embedded preview copies of six existing Woodland portraits. The conversation preview provides the Lucide icons. The headings, navigation labels and accessibility names also work when opening the fragment locally.

Sample data only. Use the bottom navigation, icon family/category filters and preview cards. Available icons can be equipped locally in the sketch; locked icons show requirements. The outside opponent control advances the sample progress; the connection selector shows connected/waiting/reconnecting. No live multiplayer, persistence, external upload or actual Android backup occurs. Board input is illustrative, not a validated Sudoku game.

November 7 validation exercised 12 interaction scenarios with a local JavaScript harness. They cover both players' progress, connection states, icon filtering, locked-item details, equipping, name editing, keypad actions, reactions and preferences. Long names can wrap in both Profile and Versus. Browser layout verification is pending because the preview file was blocked by browser policy.
