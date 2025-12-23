package com.bytz.modules.cms.payway.credit.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.modules.cms.payway.credit.application.assembler.CreditAssembler;
import com.bytz.modules.cms.payway.credit.domain.entity.ManagerLog;
import com.bytz.modules.cms.payway.credit.infrastructure.entity.ManagerLogPO;
import com.bytz.modules.cms.payway.credit.infrastructure.mapper.ManagerLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 授信变动查询服务
 * Credit Limit Change Query Service
 *
 * <p>处理信用钱包授信变动记录的查询操作（CQRS中的Query端）</p>
 * <p>职责：
 * <ul>
 *   <li>授信变动记录查询</li>
 *   <li>返回管理日志实体</li>
 *   <li>继承ServiceImpl以便利用MyBatis-Plus的查询能力</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditLimitChangeQueryService extends ServiceImpl<ManagerLogMapper, ManagerLogPO> {

    private final CreditAssembler creditAssembler;

    /**
     * 根据钱包ID分页查询授信变动记录
     *
     * @param page         分页参数
     * @param walletId     钱包ID
     * @param queryWrapper 查询条件
     * @return 分页授信变动记录
     */
    public IPage<ManagerLog> queryCreditLimitChangePage(Page<ManagerLogPO> page, String walletId, LambdaQueryWrapper<ManagerLogPO> queryWrapper) {

        // 添加钱包ID和操作类型的查询条件
        queryWrapper.eq(ManagerLogPO::getCreditWalletId, walletId)
                .orderByDesc(ManagerLogPO::getCreateTime);

        IPage<ManagerLogPO> poPage = baseMapper.selectPage(page, queryWrapper);

        return poPage.convert(creditAssembler::poToManagerLog);
    }

    /**
     * 根据经销商ID分页查询授信变动记录
     *
     * @param page         分页参数
     * @param resellerId   经销商ID
     * @param queryWrapper 查询条件
     * @return 分页授信变动记录
     */
    public IPage<ManagerLog> queryCreditLimitChangePageByResellerId(Page<ManagerLogPO> page, String resellerId, LambdaQueryWrapper<ManagerLogPO> queryWrapper) {

        // 添加经销商ID和操作类型的查询条件
        queryWrapper.eq(ManagerLogPO::getResellerId, resellerId)
                .orderByDesc(ManagerLogPO::getCreateTime);

        IPage<ManagerLogPO> poPage = baseMapper.selectPage(page, queryWrapper);

        return poPage.convert(creditAssembler::poToManagerLog);
    }
}
