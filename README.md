# 📃PaperWhitelist
A Paper whitelist, ported from [VelocityWhitelist](https://github.com/SkyboundLab/VelocityWhitelist) (`915d0d8`).

Same config files and UUID lookups as the Velocity
plugin — only the platform changes. Runs on Paper 1.20 and up. The
plugin's name is PaperWhitelist, the command is `/pwhitelist`
(`/vwhitelist` still works as an alias), the permissions are
`pwhitelist.admin` / `pwhitelist.bypass`, and the data folder is
`plugins/paperwhitelist/`.

## Permissions
| Permission | Purpose |
|--|--|
| `pwhitelist.admin` | Needed for all `/pwhitelist` commands |
| `pwhitelist.bypass` | Can bypass whitelist even if not in list |

## Commands
| Command | Response |
|--|--|
| `/pwhitelist` | Info command |
| `/pwhitelist on` | Turn the whitelist on |
| `/pwhitelist off` | Turn the whitelist off |
| `/pwhitelist add <username> [type]` | Add a user to the whitelist (type: microsoft, elyby, drasl) |
| `/pwhitelist remove <username> [type]` | Remove a user from the whitelist (type: microsoft, elyby, drasl) |
| `/pwhitelist reload` | Reload the whitelist config |

## Config
```toml
# Whether the whitelist should be on or off
enabled = true
# The message to be shown on user disconnect
message = "&cWhitelist enabled!"

# Auth server configurations (optional)
[auth.drasl]
url = "https://drasl.example.com"
token = "your-bearer-token"
```

## Migrating from VelocityWhitelist
Copy the two files from the Velocity plugin folder, no conversion needed:

```
plugins/VelocityWhitelist/config.toml   -> plugins/pwhitelist/config.toml
plugins/VelocityWhitelist/whitelist.json -> plugins/pwhitelist/whitelist.json
```

Old-format `whitelist.json` files (a plain list of UUIDs) are still migrated
to the new format on first load, exactly as the Velocity plugin did.

## Porting notes
- Velocity `LoginEvent` → Bukkit `PlayerLoginEvent`. The login event (not
  the async pre-login event) is used so the `pwhitelist.bypass` permission
  can still be checked; disallowing there still stops the login. One
  Paper gotcha the live test caught: at `PlayerLoginEvent` time an op's
  `hasPermission("pwhitelist.bypass")` still returns `false` — the Bukkit
  permission attachment only learns the op status after the login event —
  while `player.isOp()` (which reads the operator list directly) is
  already `true`. So the bypass check is `isOp() ||
  hasPermission("pwhitelist.bypass")`, matching how Paper's own whitelist
  lets ops through.
- Velocity Brigadier → Bukkit `CommandExecutor` + `TabCompleter`, with the
  same subcommands and the same tab suggestions (online players, auth
  types). Paper's Brigadier API (`BasicCommand`) does not exist in the
  Paper 1.20.4 API, so the Bukkit route is what keeps this working on
  every Paper 1.20+ build.
- UUID lookups run async and reply back on the main thread. The
  online-player shortcut runs on the main thread first, since Bukkit's
  player lookup is not thread-safe.
- Lombok and the Velocity/Guice dependencies are gone; `toml4j` is shaded
  into the jar, Gson and Adventure come from the Paper server.

## Build
```
./gradlew shadowJar
```
(`build.sh` is a javac fallback for sandboxed machines where Gradle
downloads fail; it produces the same jar.)
