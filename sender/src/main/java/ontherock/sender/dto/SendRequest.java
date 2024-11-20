package ontherock.sender.dto;

import java.util.List;

public record SendRequest(
    List<Long> recipientIds,
    String message
) {}
