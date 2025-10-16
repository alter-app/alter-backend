package com.dreamteam.alter.common.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class NotificationMessageBuilder {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * 앱 사용자가 다른 사용자에게 평판 요청하는 메시지 생성
     */
    public static String buildAppToUserReputationMessage(String requesterName) {
        return String.format(NotificationMessageConstants.Reputation.APP_TO_USER_BODY, requesterName);
    }
    
    /**
     * 업장에서 사용자에게 평판 요청하는 메시지 생성
     */
    public static String buildWorkspaceToUserReputationMessage(String workspaceName) {
        return String.format(NotificationMessageConstants.Reputation.WORKSPACE_TO_USER_BODY, workspaceName);
    }
    
    /**
     * 앱 사용자가 업장에 평판 요청하는 메시지 생성
     */
    public static String buildAppToWorkspaceReputationMessage(String requesterName, String workspaceName) {
        return String.format(NotificationMessageConstants.Reputation.APP_TO_WORKSPACE_BODY, requesterName, workspaceName);
    }
    
    /**
     * 근무 스케줄 배정 메시지 생성
     */
    public static String buildScheduleAssignmentMessage(String workspaceName, LocalDateTime startTime, LocalDateTime endTime, String position) {
        String date = startTime.format(DATE_FORMATTER);
        String time = String.format("%s - %s", startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER));
        return String.format(NotificationMessageConstants.Schedule.ASSIGNMENT_BODY, workspaceName, date, time, position);
    }
    
    /**
     * 근무 스케줄 제거 메시지 생성
     */
    public static String buildScheduleRemovalMessage(String workspaceName, LocalDateTime startTime, LocalDateTime endTime, String position) {
        String date = startTime.format(DATE_FORMATTER);
        String time = String.format("%s - %s", startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER));
        return String.format(NotificationMessageConstants.Schedule.REMOVAL_BODY, workspaceName, date, time, position);
    }
    
    /**
     * 새로운 공고 지원 메시지 생성
     */
    public static String buildNewApplicationMessage(String postingTitle) {
        return String.format(NotificationMessageConstants.PostingApplication.NEW_APPLICATION_BODY, postingTitle);
    }
    
    /**
     * 공고 지원 결과 메시지 생성
     */
    public static String buildApplicationResultMessage(String businessName, String resultType) {
        return String.format("%s 지원 결과: %s", businessName, resultType);
    }
    
}
