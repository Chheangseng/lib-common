package tcs.system.lib_common.uuid;

import java.util.UUID;

public class UuidUtil {
    public static boolean isValid (String value){
        try{
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception){
            return false;
        }
    }
}
