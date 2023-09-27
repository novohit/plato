package com.linklyze.common.model.bo;

import com.linklyze.common.enums.PayType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayParam {

    /**
     * 订单流水号
     */
    private String orderOutTradeNo;

    /**
     * 订单实际⽀付价格
     */
    private BigDecimal payAmount;

    /**
     * 支付类型，微信-银行卡-支付宝
     */
    private PayType payType;

    private String title;

    private String description;

    /**
     * 订单超时时间 s
     */
    private Long timeOut;

    private Long accountNo;
}
