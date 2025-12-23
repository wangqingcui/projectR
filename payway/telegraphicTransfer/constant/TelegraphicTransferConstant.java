package com.bytz.modules.cms.payway.telegraphicTransfer.constant;

/**
 * cms-backend
 *
 * @author bytz
 * @version 1.0
 * @date 2025/9/19 15:24
 */
public class TelegraphicTransferConstant {

    public static class USAGE_STATUS {

        // 未使用
        public static final String NOT_USED = "NotUsed";

        // 已使用
        public static final String USED = "Used";

        // 用尽
        public static final String USE_UP = "UseUp";
    }

    public static class ENABLED_STATUS {

        // 正常
        public static final String NORMAL = "Normal";

        // 作废
        public static final String CANCELED = "Canceled";
    }

    public static final String ENABLE_STATUS_PERMISSION = "admin:telegraphicTransfer:enableStatus";

    public static class OPERATE_TYPE {

        public static final String DEDUCTION = "Deduction";

        public static final String REFUND = "Refund";

        //  其他支出
        public static final String OTHER_DEDUCTION = "OTHER_DEDUCTION";

        // 其他退款
        public static final String OTHER_REFUND = "OTHER_REFUND";

    }
}
