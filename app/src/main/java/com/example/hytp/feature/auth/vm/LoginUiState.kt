package com.example.hytp.feature.auth.vm

/**
 * 登录页 UI 状态（UDF）。
 *
 * @property phone 手机号输入
 * @property code 验证码输入
 * @property countdown 发码倒计时秒数（>0 时禁用重发）
 * @property sending 发码请求中
 * @property loggingIn 登录请求中
 * @property error 错误提示（一次性展示用，展示后可清）
 * @property devCode 开发 Mock 模式后端回带的验证码（仅 debug 便于联调）
 * @property loginSuccess 登录成功标记（触发导航）
 */
data class LoginUiState(
    val phone: String = "",
    val code: String = "",
    val countdown: Int = 0,
    val sending: Boolean = false,
    val loggingIn: Boolean = false,
    val error: String? = null,
    val devCode: String? = null,
    val loginSuccess: Boolean = false,
) {
    /** 手机号格式合法（简单校验）。 */
    val isPhoneValid: Boolean get() = phone.matches(Regex("^1[3-9]\\d{9}$"))

    /** 可发送验证码：号合法、无倒计时、非发码中。 */
    val canSendCode: Boolean get() = isPhoneValid && countdown == 0 && !sending

    /** 可登录：号合法、验证码非空、非登录中。 */
    val canLogin: Boolean get() = isPhoneValid && code.length >= 4 && !loggingIn
}
