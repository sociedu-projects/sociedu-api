package com.unishare.api.modules.payment.dto;

import lombok.Data;


@Data
public class VNPayCallbackParams {
    private String vnp_TxnRef;
    private String vnp_TransactionStatus;
    private String vnp_SecureHash;
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_PayDate;
    private String vnp_ResponseCode;
    private String vnp_OrderInfo;
}
