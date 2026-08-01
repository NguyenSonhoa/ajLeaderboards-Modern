<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&height=230&color=0:09090f,45:451080,100:00d9ff&text=ajLeaderboards%20Modern&fontColor=ffffff&fontSize=48&fontAlignY=38&desc=Native%20holograms.%20Private%20pages.%20Smooth%20motion.&descAlignY=59&animation=fadeIn" width="100%" alt="ajLeaderboards Modern banner" />

[![Typing SVG](https://readme-typing-svg.demolab.com?font=JetBrains+Mono&weight=600&size=17&duration=2800&pause=900&color=BB6BFF&center=true&vCenter=true&width=760&lines=Modern+Paper+%2F+Leaf+leaderboards;Packet-only+Text+Display+holograms;Per-player+pages+%C2%B7+Java+%2B+Bedrock;Built+for+Minecraft+1.21.x)](https://git.io/typing-svg)

[![CI](https://img.shields.io/github/actions/workflow/status/NguyenSonhoa/ajLeaderboards-Modern/gradle.yml?branch=main&style=for-the-badge&logo=githubactions&logoColor=white&label=BUILD)](https://github.com/NguyenSonhoa/ajLeaderboards-Modern/actions)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Paper 1.21.x](https://img.shields.io/badge/Paper-1.21.x-F4F4F4?style=for-the-badge&logo=paper&logoColor=222222)](https://papermc.io/)
[![PacketEvents](https://img.shields.io/badge/PacketEvents-2.13-00D9FF?style=for-the-badge)](https://github.com/retrooper/packetevents)
[![GPL-3.0](https://img.shields.io/github/license/NguyenSonhoa/ajLeaderboards-Modern?style=for-the-badge&color=BB6BFF)](LICENSE)

**A modern fork of [ajLeaderboards](https://github.com/ajgeiss0702/ajLeaderboards), rebuilt around interactive native display entities.**

[Download Actions Build](https://github.com/NguyenSonhoa/ajLeaderboards-Modern/actions) · [Report Issue](https://github.com/NguyenSonhoa/ajLeaderboards-Modern/issues) · [Upstream Wiki](https://wiki.ajg0702.us/ajleaderboards/)

</div>

## `> signal`

```text
Runtime    Java 21 · Paper/Leaf 1.21.x
Display    PacketEvents Text Display + Item Display
Input      Ray targeting · private page state · left-click navigation
Bridge     PlaceholderAPI · Vault · LuckPerms · Geyser/Floodgate
Storage    MySQL · H2 · SQLite
Mission    Keep the proven ajLeaderboards core. Modernize the player-facing layer.
```

The original project already handles flexible PlaceholderAPI leaderboards, timed resets, signs, player heads, storage, and rich placeholders. This fork keeps that foundation while adding a native hologram runtime that does not require FancyHolograms, DecentHolograms, or persistent world entities.

## `> upstream delta`

| System | Original ajLeaderboards | Modern fork |
|:--|:--|:--|
| Hologram display | External hologram plugins / PAPI output | Native packet-only Text and Item Displays |
| Board rotation | External page systems | Built-in timed slide rotation |
| Viewer pages | Shared external state | Private page state per player |
| Interaction | Depends on display plugin | Full-hologram ray target + left click |
| Timer | Not part of native display runtime | Animated Item or Text timer with background |
| Motion | External implementation | Stagger, slide, smooth scale, fade-in/out |
| Personal data | PAPI where supported | Every viewer receives individually parsed text |
| Bedrock UX | No native specialization | Geyser/Floodgate subtitle prompt |
| Runtime target | Broad legacy support | Java 21 and modern Paper/Leaf 1.21.x |
| Build | Upstream pipeline | Portable PacketEvents dependency + JDK 21 CI |

## `> hologram engine`

### Packet-only displays

- Spawns Text Display and Item Display entities only through packets.
- Saves no display entities into the world.
- Renders viewer-specific placeholders, rank, value, and footer text.
- Supports `FIXED`, `CENTER`, `VERTICAL`, and `HORIZONTAL` billboards.
- Supports legacy color codes, `&#RRGGBB`, bold, reset, and strikethrough.
- Applies full-bright metadata to display entities.

### Rotation and motion

- Rotates any number of leaderboard slides at one location.
- Resets every viewer to page 1 whenever the timer changes the top.
- Staggers line entrances and exits.
- Smoothly scales and fades all lines, timer foreground, and timer background.
- Uses smoothstep easing for softer starts and stops.
- Offers Item Display and Text Display timer modes.

### Private pagination

- Defines multiple pages inside each slide.
- Keeps page index private per viewer.
- Targets the entire hologram from title through rewards and footer.
- Switches page when the targeting player left-clicks.
- Shows Java players a packet-only mounted Text Display tip.
- Shows Geyser/Floodgate players a configurable subtitle.
- Restores the tip mount while players move or server passenger packets resync.

## `> requirements`

| Dependency | Requirement |
|:--|:--|
| Java | 21 |
| Server | Paper-compatible 1.21.x; Leaf supported |
| PlaceholderAPI | Required |
| PacketEvents | 2.13.x required for native holograms |
| Vault | Optional prefixes/suffixes |
| Geyser/Floodgate | Optional Bedrock subtitle detection |

## `> quick config`

Native holograms live under `leaderboard-holograms` in `config.yml`.

```yaml
leaderboard-holograms:
  example:
    enabled: true
    duration-seconds: 10
    scale: 1.0
    line-spacing: 0.3
    billboard: CENTER
    text-shadow: true
    see-through: false

    animation:
      duration-ticks: 10
      line-delay-ticks: 1
      slide-distance: 1.5
      scale-duration-ticks: 10
      transition-scale: 0.65

    pagination:
      tip: "&aLeft click to change page"
      bedrock-subtitle: "&aPress attack to change page"
      tip-scale: 0.7
      tip-offset:
        x: 0.0
        y: 0.5
        z: 0.0
      target-distance: 12.0
      target-radius: 1.5

    bar:
      type: TEXT
      text: "&a&m              "
      background-text: "&8&m              "
      background-offset-z: 0.1
      text-height: 1.0
      max-width: 2.0
      auto-position: true
      gap: 0.15
      offset-y: 0.0

    location:
      world: world
      x: 0.5
      y: 80.0
      z: 0.5
      yaw: 0.0
      pitch: 0.0

    slides:
      - pages:
          - lines:
              - "&#00D9FF&lTOP EXAMPLE"
              - "&e1. &f%some_player_placeholder%"
          - lines:
              - "&#00D9FF&lTOP EXAMPLE"
              - "&e2. &fSecond page"
```

### Timer positioning

```yaml
bar:
  auto-position: true
  gap: 0.15
  offset-y: 0.0
```

`auto-position: true` places the timer below the longest page. `offset-y` then acts as an additional vertical adjustment. Set `auto-position: false` to position the timer directly from the hologram origin.

### Tip positioning

```yaml
pagination:
  tip-offset:
    x: 0.0 # left / right
    y: 0.5 # down / up
    z: 0.0 # back / forward
```

The Java tip uses local Text Display translation after mounting, so its offset stays stable while the player moves.

## `> command deck`

| Command | Purpose |
|:--|:--|
| `/ajleaderboards hologram create <id> <board>` | Create a basic native hologram |
| `/ajleaderboards hologram movehere <id>` | Persist the hologram at your current location |
| `/ajleaderboards hologram respawn [id]` | Respawn one or every native hologram |
| `/ajleaderboards hologram remove <id>` | Remove a native hologram config |
| `/ajleaderboards hologram list` | List configured holograms |
| `/ajleaderboards reload` | Reload config and respawn native holograms |

`movehere` reloads the disk config before editing, saves the new location, reloads again, and verifies the persisted coordinates before reporting success.

## `> retained core`

- Numeric PlaceholderAPI leaderboard sources
- All-time and timed statistics
- Automatic hourly, daily, weekly, monthly, and custom resets
- MySQL, H2, and SQLite storage
- Built-in signs, player heads, and Armor Stand heads
- Vault prefix/suffix integration
- Raw, formatted, relative, time, total, and extra placeholders
- Existing ajLeaderboards board management commands and API
- Folia metadata and inherited upstream integrations

Use the [upstream wiki](https://wiki.ajg0702.us/ajleaderboards/) for original board tracking, placeholders, databases, signs, and reset behavior. The native hologram systems documented here belong to this fork.

## `> build pipeline`

```bash
git clone https://github.com/NguyenSonhoa/ajLeaderboards-Modern.git
cd ajLeaderboards-Modern
./gradlew shadowJar
```

Artifact:

```text
build/libs/ajLeaderboards-2.11.0.jar
```

GitHub Actions uses JDK 21 and uploads the shaded plugin JAR after every successful build.

## `> lineage`

| Credit | Signal |
|:--|:--|
| [ajgeiss0702](https://github.com/ajgeiss0702) | Original ajLeaderboards project and core implementation |
| [NguyenSonhoa](https://github.com/NguyenSonhoa) | Modern fork, packet holograms, interaction, pagination, animation, and Bedrock UX |

This repository preserves the upstream [GPL-3.0 license](LICENSE). It is an independent fork, not the official upstream support channel.

> **Good mechanics are discovered. Great mechanics are refined.**

<div align="center">

[![Repository](https://img.shields.io/badge/GitHub-ajLeaderboards_Modern-171723?style=for-the-badge&logo=github&logoColor=white)](https://github.com/NguyenSonhoa/ajLeaderboards-Modern)
[![Developer](https://img.shields.io/badge/Developer-NguyenSonhoa-BB6BFF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/NguyenSonhoa)

<img src="https://capsule-render.vercel.app/api?type=waving&height=110&section=footer&color=0:09090f,45:451080,100:00d9ff" width="100%" alt="Footer" />

</div>
