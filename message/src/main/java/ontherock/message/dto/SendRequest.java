package ontherock.message.dto;

import java.util.List;

public record SendRequest(
    List<Long> recipientIds,
    String message
) {}
