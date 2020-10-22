# StickyCommands
Light-weight essentials replacement for DumbDogDiner

# Commands
| Command | Description | Permission |
| --- | --- | --- |
| /stickycommands [reload/database/db] | Reloads the configuration and messages YAML file, or reconnects the database | stickycommands.reload |
| /speed \<speed> | Sets your flying or walking speed | stickycommands.speed |
| /sell \<hand> | Sells the stack of items in your hand | stickycommands.sell.hand |
| /sell \<inventory> | Sells all of the items in your inventory that match the one you're holding | stickycommands.sell.inventory |
| /powertool \<command>| Bind a command to an item! | stickycommands.powertool |
| /worth | Gives the worth of the item that your're holding | stickycommands.worth |
| /top | Takes you to the top! | stickycommands.top |
| /jump | Takes you to the block that you're lookint at | stickycommands.jump |
| /afk | Puts you in AFK mode | stickycommands.afk |
| /mem | Outputs server performance statistics | stickycommands.memory |

# GitHub Packages Setup

- Create a file at `~/gradle.properties` with the following details:

```
ghUser=your github username
ghPass=a github pat (personal access token) with the read:packages scope only
```

- Make sure that the `GRADLE_USER_HOME` env variable is set to your home folder