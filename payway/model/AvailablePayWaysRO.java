package com.bytz.modules.cms.payway.model;

import com.bytz.modules.cms.payway.credit.application.model.BasePaymentItemRo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailablePayWaysRO {

//    @NotNull
//    private List<String> paymentIds;

    @NotBlank(groups = BasePaymentItemRo.AdminGroup.class)
    private String resellerId;
}
