package lee.code.locker.commands;

import lee.code.locker.commands.subcommands.Add;
import lee.code.locker.commands.subcommands.Admin;
import lee.code.locker.commands.subcommands.Remove;
import lee.code.locker.commands.subcommands.SignHelp;
import lee.code.locker.lists.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager() {
        subCommands.add(new SignHelp());
        subCommands.add(new Add());
        subCommands.add(new Remove());
        subCommands.add(new Admin());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender instanceof Player player) {

            if (args.length > 0) {
                for (SubCommand subCommand : subCommands) {
                    if (args[0].equalsIgnoreCase(subCommand.getName())) {
                        if (player.hasPermission(subCommand.getPermission())) subCommand.perform(player, args);
                        else player.sendMessage(Lang.PREFIX.getComponent(null).append(Lang.ERROR_NO_PERMISSION.getComponent(null)));
                        return true;
                    }
                }
            }

            int number = 1;
            List<Component> lines = new ArrayList<>();
            lines.add(Lang.MESSAGE_HELP_DIVIDER.getComponent(null));
            lines.add(Lang.MESSAGE_HELP_TITLE.getComponent(null));
            lines.add(Component.text(""));

            for (SubCommand subCommand : subCommands) {
                if (player.hasPermission(subCommand.getPermission())) {
                    String suggestCommand = subCommand.getSyntax().contains(" ") ? subCommand.getSyntax().split(" ")[0] : subCommand.getSyntax();
                    lines.add(Lang.MESSAGE_HELP_SUB_COMMAND.getComponent(new String[] { String.valueOf(number), subCommand.getSyntax() }).hoverEvent(Lang.MESSAGE_HELP_SUB_COMMAND_HOVER.getComponent(new String[] {  subCommand.getDescription() })).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand)));
                    number++;
                }
            }

            lines.add(Component.text(""));
            lines.add(Lang.MESSAGE_HELP_DIVIDER.getComponent(null));
            for (Component line : lines) player.sendMessage(line);

        } else if (args.length > 0) {
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    subCommand.performConsole(sender, args);
                    return true;
                }
            }
        }
        return true;
    }
}