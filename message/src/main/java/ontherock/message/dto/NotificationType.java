package ontherock.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public enum NotificationType {
    FOLLOW("follow"),
    NEW_POST("new_post"),
    STREAMING("streaming"),
    COMMENT("comment"),
    LIKE("like");

    private final String value;

    @JsonCreator
    public static NotificationType fromString(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
