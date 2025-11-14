package com.dreamteam.alter.common.notification;

public final class NotificationMessageConstants {
    
    /**
     * 평판 요청 관련 알림 메시지
     */
    public static final class Reputation {
        public static final String REQUEST_TITLE = "새로운 평판 요청";
        public static final String APP_TO_USER_BODY = "%s님이 평판을 요청했습니다.";
        public static final String WORKSPACE_TO_USER_BODY = "%s에서 평판을 요청했습니다.";
        public static final String APP_TO_WORKSPACE_BODY = "%s님이 %s으로 평판을 요청했습니다.";
    }
    
    /**
     * 근무 스케줄 관련 알림 메시지
     */
    public static final class Schedule {
        public static final String ASSIGNMENT_TITLE = "새로운 근무 스케줄이 배정되었습니다";
        public static final String ASSIGNMENT_BODY = "%s - %s %s %s 근무가 배정되었습니다";
        public static final String REMOVAL_TITLE = "근무 스케줄이 취소되었습니다";
        public static final String REMOVAL_BODY = "%s - %s %s %s 근무가 취소되었습니다";
    }
    
    /**
     * 공고 지원 관련 알림 메시지
     */
    public static final class PostingApplication {
        public static final String NEW_APPLICATION_TITLE = "새로운 지원자가 있습니다";
        public static final String NEW_APPLICATION_BODY = "%s에 새로운 지원자가 있습니다!";
        
        public static final String SHORTLISTED_TITLE = "서류 합격을 축하합니다!";
        public static final String SHORTLISTED_BODY = "%s 지원 결과: 서류 합격";
        public static final String ACCEPTED_TITLE = "최종 합격을 축하합니다!";
        public static final String ACCEPTED_BODY = "%s 지원 결과: 최종 합격";
        public static final String REJECTED_TITLE = "지원 결과를 안내드립니다";
        public static final String REJECTED_BODY = "%s 지원 결과: 불합격";
    }
    
    /**
     * 스케줄 교환 요청 관련 알림 메시지
     */
    public static final class SubstituteRequest {
        public static final String NEW_REQUEST_TITLE = "새로운 대타 요청이 있습니다";
        public static final String NEW_REQUEST_BODY = "%s님이 %s %s 근무 대타를 요청했습니다";
        
        public static final String ACCEPTED_TITLE = "대타 요청이 수락되었습니다";
        public static final String ACCEPTED_BODY = "%s님이 %s %s 근무 대타 요청을 수락했습니다. 사장님 승인 대기 중입니다.";
        
        public static final String REJECTED_TITLE = "대타 요청이 거절되었습니다";
        public static final String REJECTED_BODY = "%s님이 %s %s 근무 대타 요청을 거절했습니다";
        
        public static final String MANAGER_APPROVED_TITLE = "대타 요청이 승인되었습니다";
        public static final String MANAGER_APPROVED_BODY = "%s에서 %s %s 근무 대타 요청이 승인되었습니다. 근무 교환이 완료되었습니다.";
        
        public static final String MANAGER_REJECTED_TITLE = "대타 요청이 거절되었습니다";
        public static final String MANAGER_REJECTED_BODY = "%s에서 %s %s 근무 대타 요청이 거절되었습니다";
        
        public static final String MANAGER_ACCEPTED_TITLE = "대타 요청이 수락되었습니다";
        public static final String MANAGER_ACCEPTED_BODY = "%s님이 %s %s 근무 대타를 수락했습니다. 승인을 기다리고 있습니다.";
    }
    
    /**
     * 채팅 관련 알림 메시지
     */
    public static final class Chat {
        public static final String NEW_MESSAGE_TITLE = "새로운 메시지";
        public static final String NEW_MESSAGE_BODY = "%s: %s";
    }
    
}
