
# 📃VelocityWhitelist
A Proxy wide Whitelist for Velocity

## Permissions
| Permission | Purpose |
|--|--|
| `vwhitelist.admin` | Needed for all `/vwhitelist` commands |
| `vwhitelist.bypass` | Can bypass whitelist even if not in list |

## Commands
| Command | Response |
|--|--|
| `/vwhitelist` | Info command |
| `/vwhitelist on` | Turn the whitelist on |
| `/vwhitelist off` | Turn the whitelist off |
| `/vwhitelist add <username> [type]` | Add a user to the whitelist (type: microsoft, elyby, drasl) |
| `/vwhitelist remove <username> [type]` | Remove a user from the whitelist (type: microsoft, elyby, drasl) |
| `/vwhitelist reload` | Reload the whitelist config |

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