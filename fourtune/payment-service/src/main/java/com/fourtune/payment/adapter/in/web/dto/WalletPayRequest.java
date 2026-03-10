package com.fourtune.payment.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record WalletPayRequest(
        @NotNull(message = "orderId는 필수입니다.")
        String orderId
) {}
