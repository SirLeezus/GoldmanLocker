package lee.code.locker.lists;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Values {

    CLICK_DELAY(5),
    MAX_TRUSTED(30),
    ;

    @Getter private final int value;
}