# StickyCommands
Light-weight essentials replacement for DumbDogDiner

# Commands
| Command | Description | Permission |
| --- | --- | --- |
| /stickycommands [reload/database/db] | Reloads the configuration and messages YAML file, or reconnects the database | stickycommands.reload |
| /seen <player/UUID/ip> | Gives the info on the specified target, as long as it exists | sticktycommands.seen |
| /speed \<speed> | Sets your flying or walking speed | stickycommands.speed |
| /smite \<player> [damage] | Smites a player with damage (default 5hp) | stickycommands.smite |
| /sell \<hand> | Sells the stack of items in your hand | stickycommands.sell.hand |
| /sell \<inventory> | Sells all of the items in your inventory that match the one you're holding | stickycommands.sell.inventory |
| /hat [rem/off/0] | Add or remove a hat! | stickycommands.hat |
| /item \<item> [name:name] [enchantment:enchant:level] | Give yourself items, with names and/or enchants! | stickycommands.item |
| /worth | Gives the worth of the item that your're holding | stickycommands.worth |
| /top | Takes you to the top! | stickycommands.top |
| /jump | Takes you to the block that you're lookint at | stickycommands.jump |
| /afk | Puts you in AFK mode | stickycommands.afk |
| /mem | Outputs server performance statistics | stickycommands.memory |

# Building

You need to have a `~/.m2/settings.xml` with your credentials for GitHub, or a token with `read:packages` to use our Spigot repository.

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>github username</username>
      <password>password / token</password>
    </server>
  </servers>
</settings>
```