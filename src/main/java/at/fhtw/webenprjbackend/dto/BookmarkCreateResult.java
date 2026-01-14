package at.fhtw.webenprjbackend.dto;

/** Result of bookmark creation with creation status for idempotent operations. */
public record BookmarkCreateResult(BookmarkResponse bookmark, boolean created) {}
