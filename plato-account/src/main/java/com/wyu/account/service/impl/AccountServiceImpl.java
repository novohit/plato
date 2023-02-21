package com.wyu.account.service.impl;

import com.wyu.account.model.AccountDO;
import com.wyu.account.mapper.AccountMapper;
import com.wyu.account.service.AccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 *
 * @author novo
 * @since 2023-02-21
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDO> implements AccountService {

}
