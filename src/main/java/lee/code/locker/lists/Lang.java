package lee.code.locker.lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum Lang {
    PREFIX("&6&lLocker &e➔ &r"),
    MESSAGE_HELP_DIVIDER("&6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
    SIGN_INFO_HEADER("&6--------- &7[ &c&lLock Info &7] &6---------"),
    SIGN_INFO_OWNER("&e&lOwner&7: &6{0}"),
    SIGN_INFO_TRUSTED("&e&lTrusted&7: &6{0}"),
    SIGN_INFO_FOOTER("&6--------------------------------"),
    MESSAGE_HELP_TITLE("                      &e-== &c&l&nLock Help&r &e==-"),
    MESSAGE_HELP_ADD("&e/lock add &f<player> &c| &7Add a player to your lock sign."),
    MESSAGE_HELP_REMOVE("&e/lock remove &f<player> &c| &7Remove a player from your lock sign."),
    MESSAGE_LOCK_SUCCESSFUL("&aThis {0} block is now locked for you!"),
    MESSAGE_REMOVE_LOCK_SUCCESSFUL("&7That {0} block is no longer locked."),
    MESSAGE_ADD_TRUST_SUCCESSFUL("&aYou successfully added &6{0} &ato this {1} lock!"),
    MESSAGE_REMOVE_TRUST_SUCCESSFUL("&aYou successfully removed &6{0} &afrom your {1} lock."),
    ERROR_LOCKED("&cThis {0} block is locked by &6{1}&c."),
    ERROR_TRUST_ADD_TARGET_PLAYER("&cYou need to input a player to add to your lock sign."),
    ERROR_TRUST_REMOVE_TARGET_PLAYER("&cYou need to input a trusted player to remove them from your lock sign."),
    ERROR_PLAYER_NOT_ONLINE("&cThe player &6{0} &cis not online."),
    ERROR_LOCK_SIGN_NOT_FOUND("&cThe lock sign was unable to be found. Make sure you're looking at it when you run this command."),
    ERROR_TRUST_ADD_NOT_OWNER("&cThis lock sign is owned by &6{0}&c. That user will need to add you."),
    ERROR_TRUST_REMOVE_NOT_OWNER("&cThis lock sign is owned by &6{0}&c. Only the owner of the lock sign can removde trusted players."),
    ERROR_TRUST_REMOVE_NOT_TRUSTED("&cThe player &6{0} &cis not trusted to this {1} lock."),
    ERROR_TRUST_ADD_OWNER("&cYou can't add yourself as trusted if you're the owner of the lock sign. I hope you can see why that would be silly."),
    ERROR_TRUST_ADD_ALREADY_ADDED("&cThe player &6{0} &cis already added to this lock sign. You can right-click the lock sign to get the trusted info."),
    ERROR_BLOCK_ALREADY_HAS_LOCK("&cThis block already has a sign lock."),
    ;

    @Getter private final String string;
    public String getString(String[] variables) {
        String value = ChatColor.translateAlternateColorCodes('&', string);
        if (variables == null) return value;
        else if (variables.length == 0) return value;
        for (int i = 0; i < variables.length; i++) value = value.replace("{" + i + "}", variables[i]);
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}