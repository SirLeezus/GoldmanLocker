package lee.code.locker.lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum Lang {
    PREFIX("&6&lLocker &e➔ &r"),
    ERROR_NO_PERMISSION("&cYou sadly do not have permission for this."),
    MESSAGE_HELP_SUB_COMMAND("&3{0}&b. &e{1} &c| &7{2}"),
    MESSAGE_HELP_DIVIDER("&6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
    SIGN_INFO_HEADER("&6--------- &7[ &c&lLock Info &7] &6---------"),
    SIGN_INFO_OWNER("&e&lOwner&7: &6{0}"),
    SIGN_INFO_TRUSTED("&e&lTrusted&7: &6{0}"),
    SIGN_INFO_FOOTER("&6--------------------------------"),
    MESSAGE_HELP_TITLE("                      &e-== &c&l&nLocker Help&r &e==-"),
    COMMAND_ADMIN_BYPASS_ENABLED("&aAdmin claim bypass is now &2enabled&a!"),
    COMMAND_ADMIN_BYPASS_DISABLED("&aAdmin claim bypass is now &cdisabled&a."),
    MESSAGE_LOCK_SUCCESSFUL("&aThat {0} block is now locked for you!"),
    MESSAGE_REMOVE_LOCK_SUCCESSFUL("&7That {0} block is no longer locked."),
    MESSAGE_ADD_TRUST_SUCCESSFUL("&aYou successfully added &6{0} &ato this lock!"),
    MESSAGE_REMOVE_TRUST_SUCCESSFUL("&aYou successfully removed &6{0} &afrom your {1} lock."),
    ERROR_COMMAND_ADMIN_ARGS("&cYou need to specify which admin command you would like to run."),
    ERROR_LOCKED("&cThat {0} block is locked by &6{1}&c."),
    ERROR_TRUST_ADD_TARGET_PLAYER("&cYou need to input a player to add to the lock sign you're looking at."),
    ERROR_TRUST_REMOVE_TARGET_PLAYER("&cYou need to input a trusted player to remove them from the lock sign you're looking at."),
    ERROR_PLAYER_NOT_FOUND("&cThe player &6{0} &chas never logged into this server."),
    ERROR_LOCK_SIGN_NOT_FOUND("&cThe lock sign was unable to be found. Make sure you're looking at it when you run this command."),
    ERROR_TRUST_ADD_NOT_OWNER("&cThat lock sign is owned by &6{0}&c. That user will need to add you."),
    ERROR_TRUST_REMOVE_NOT_OWNER("&cThat lock sign is owned by &6{0}&c. Only the owner of the lock sign can removde trusted players."),
    ERROR_TRUST_REMOVE_NOT_TRUSTED("&cThe player &6{0} &cis not trusted to this {1} lock."),
    ERROR_TRUST_ADD_OWNER("&cYou can't add yourself as trusted if you're the owner of the lock sign. I hope you can see why that would be silly."),
    ERROR_TRUST_ADD_ALREADY_ADDED("&cThe player &6{0} &cis already added to this lock sign. You can right-click the lock sign to get the trusted info."),
    ERROR_TRUSTED_MAX("&cYou already have a max of {0} players trusted to this lock."),
    ERROR_BLOCK_ALREADY_HAS_LOCK("&cThat block already has a sign lock."),
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