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

## Build
```
./gradlew shadowJar
```
(`build.sh` is a javac fallback for sandboxed machines where Gradle
downloads fail; it produces the same jar.)
