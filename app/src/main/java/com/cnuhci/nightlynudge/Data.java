package com.cnuhci.nightlynudge;

public class Data {
    public static String FILE_NAME = "NightlyNudge_log_DATA";
    public static String TIMESTAMP = "";            // 시간 데이터
    public static String SLEEP_TIME = "";           // 설정된 취침 시각
    public static String WAKE_TIME = "";            // 설정된 기상 시각
    public static String BATTERY_STATUS = "";       // 배터리 상태
    public static String BATTERY_PERCENTAGE = "";   // 배터리 잔량
    public static String BEDTIME_USAGE_TIME = "";   // 취침 시각 ~ 기상 시각 휴대폰 사용 시간 (초)
    public static String TOTAL_USAGE_TIME = "";     // 취침 준비 시각 ~ 기상 시각 휴대폰 사용 시간 (초)
    public static String WINDOW_ON = "none";            // 휴대폰 사용 여부 (스크린 켜져있는 경우: on)
    public static String ALARM = "none";            // 알람 준 여부 (1: 알람, 0: none)
    public static String ACTION = "none";           // 활동 시간대 여부
}
