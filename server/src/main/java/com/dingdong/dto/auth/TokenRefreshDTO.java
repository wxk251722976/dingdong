package com.dingdong.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class TokenRefreshDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Refresh Token不能为空")
    private String refreshToken;
}
