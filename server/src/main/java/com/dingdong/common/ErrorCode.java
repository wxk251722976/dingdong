package com.dingdong.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标准错误码枚举
 * 
 * 错误码规则：
 * 200: 成功
 * 400-499: 客户端错误 (400参数错误, 401未认证, 403未授权, 404未找到)
 * 500-599: 服务端错误
 * 1000+: 业务自定义错误
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),

    // 客户端错误
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证或Token已失效"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 服务端错误
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // 业务错误
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    VERIFY_CODE_ERROR(1004, "验证码错误"),

    // 关系业务
    RELATION_NOT_EXIST(2001, "关系不存在"),
    RELATION_ALREADY_EXIST(2002, "关系已存在"),
    RELATION_STATUS_ERROR(2003, "关系状态异常"),
    CANNOT_BIND_SELF(2004, "不能绑定自己"),
    COOLDOWN_PERIOD(2005, "处于解绑冷却期中"),

    // 任务业务
    TASK_NOT_EXIST(3001, "任务不存在"),
    CHECKIN_ALREADY_DONE(3002, "今日已打卡"),

    // 微信业务
    WX_LOGIN_ERROR(4001, "微信登录失败"),

    // 文件业务
    FILE_UPLOAD_ERROR(5001, "文件上传失败");

    private final Integer code;
    private final String msg;
}
