package com.simon.service.impl;

import com.simon.config.GlobalVariable;
import com.simon.dto.*;
import com.simon.enums.BankType;
import com.simon.enums.TransactionType;
import com.simon.mapper.UserBankDetailMapper;
import com.simon.mapper.UserBankMapper;
import com.simon.mapper.UserMapper;
import com.simon.model.*;
import com.simon.service.CenterService;
import com.simon.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CenterServiceImpl implements CenterService {

    public static final Logger LOG = LoggerFactory.getLogger(CenterServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBankMapper userBankMapper;

    @Resource
    private UserBankDetailMapper userBankDetailMapper;

    @Override
    public Result login(LoginDto loginDto) {

        String telephone = loginDto.getTelephone();
        String password = loginDto.getPassword();
        LOG.info("login params:telephone: {},password: {}",telephone,password);
        if (StringUtils.isEmpty(password)) {
            return Result.failure("密码不能为空！");
        }

        if (StringUtils.isEmpty(telephone)) {
            return Result.failure("手机号不能为空！");
        }

        if(!PhoneUtils.isChinaPhoneLegal(telephone)){
            return Result.failure("手机号格式不正确！");
        }

        UserExample example = new UserExample();
        example.createCriteria().andPasswordEqualTo(password)
                .andTelphoneEqualTo(telephone).andUserTypeEqualTo(BankType.ZGJSYH.getCode());
        List<User> users = userMapper.selectByExample(example);
        if (users.size() == 0) {
            return Result.failure("手机号或密码错误！");
        }
        User user = users.get(0);
        LoginResult loginResult = new LoginResult();
        BeanUtils.copyProperties(user, loginResult);
        if (user.getLastLoginTime() == null) {
            loginResult.setLastLoginTime(new Date());
        }
        user.setLastLoginTime(new Date());
        //加入全局缓存中去
        String token = UUID.randomUUID().toString();

        GlobalVariable.tokenMap.put(token, user);
        loginResult.setToken(token);
        userMapper.updateByPrimaryKeySelective(user);

        //查询账户余额
        UserBankExample example2 = new UserBankExample();
        example2.createCriteria().andUserIdEqualTo(user.getId()).andBankTypeEqualTo(BankType.ZGJSYH.getCode());
        List<UserBank> userBankList = userBankMapper.selectByExample(example2);
        if (userBankList.size() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");
            String balance = decimalFormat.format(userBankList.get(0).getBalance().doubleValue());//会返回格式化后的金额
            loginResult.setBalance(balance);
            loginResult.setBankNumber(userBankList.get(0).getBankNumber());
        }
        return Result.ok().setData(loginResult);
    }

    @Override
    public Result register(RegisterDto registerDto) {
        String username = registerDto.getUsername();
        String telephone = registerDto.getTelephone();
        String bankNumber = registerDto.getBankNumber();
        String password = registerDto.getPassword();
        String idCard = registerDto.getIdCard();
        //String depositBank = registerDto.getDepositBank();

        LOG.info("register params:username: {},telephone: {},bankNumber: {},password: {},idCard: {}",
                username,telephone,bankNumber,password,idCard);

        if (StringUtils.isEmpty(username)) {
            return Result.failure("客户姓名不能为空！");
        }
        if (StringUtils.isEmpty(telephone)) {
            return Result.failure("手机号不能为空！");
        }

        if(!PhoneUtils.isChinaPhoneLegal(telephone)){
            return Result.failure("手机号格式不正确！");
        }
        if (StringUtils.isEmpty(bankNumber)) {
            return Result.failure("银行账号不能为空！");
        }
        if(bankNumber.length()!=19){
            return Result.failure("银行账号格式不正确！");
        }
        if (StringUtils.isEmpty(password)) {
            return Result.failure("密码不能为空！");
        }
        if (StringUtils.isEmpty(idCard)) {
            return Result.failure("身份证号不能为空！");
        }

        if(!ValidateIdCardUtil.isIDCard(idCard)){
            return Result.failure("身份证号格式不正确！");
        }
//        if (StringUtils.isEmpty(depositBank)) {
//            return Result.failure("开户行不能为空！");
//        }

        UserExample example = new UserExample();
        example.createCriteria().andIdCartEqualTo(idCard).andUserTypeEqualTo(BankType.ZGJSYH.getCode());
        if (userMapper.selectByExample(example).size() > 0) {
            return Result.failure("账户已经被注册！");
        }
        User user = new User();
        user.setId(IdUtil.getStringId());
        user.setUsername(username);
        user.setPassword(password);
        user.setTelphone(telephone);
        user.setIdCart(idCard);
        user.setUserType(BankType.ZGJSYH.getCode());
        userMapper.insert(user);

        UserBankExample uexample= new UserBankExample();
        uexample.createCriteria().andUserIdEqualTo(user.getId()).andBankTypeEqualTo(BankType.ZGJSYH.getCode());
        if(userBankMapper.countByExample(uexample)>0){
            return Result.failure("该银行账号已被注册！");
        }

        UserBank userBank = new UserBank();
        userBank.setId(IdUtil.getStringId());
        userBank.setUserId(user.getId());
        userBank.setBankType(BankType.ZGJSYH.getCode());
        userBank.setBankNumber(bankNumber);
       // userBank.setDepositBank(depositBank);
        userBank.setBankName(BankType.ZGJSYH.getDescription());
        userBank.setBalance(BigDecimal.valueOf(100000));
        userBankMapper.insertSelective(userBank);

        LoginDto loginDto= new LoginDto();
        loginDto.setPassword(password);
        loginDto.setTelephone(telephone);
        return this.login(loginDto);
    }

    @Override
    public Result getTransactionList(TransactionDto transactionDto, User user) {

        String userId = user.getId();

        Date beginTime = transactionDto.getTransactionDate();

        Date endTime = new Date();

        //收款账户名 账号 手机号 留言
        String keyWords = transactionDto.getKeyWords();

        LOG.info("getTransactionList params:userId: {},beginTime: {},keyWords: {}",
                userId,beginTime,keyWords);
        UserBankDetailExample example = new UserBankDetailExample();
        example.setOrderByClause("transaction_date desc");
        UserBankDetailExample.Criteria criteria = example.createCriteria();

        //查询自己转出去的
        UserBankExample example2 = new UserBankExample();
        example2.createCriteria().andUserIdEqualTo(userId);
        List<UserBank> userBankList = userBankMapper.selectByExample(example2);
        if (userBankList.size() != 0) {
            UserBank userBank = userBankList.get(0);
            criteria.andExportFromUserBankIdEqualTo(userBank.getId());
        }

        if (beginTime != null) {
            criteria.andTransactionDateBetween(beginTime, endTime);
        } else {
            criteria.andTransactionDateLessThanOrEqualTo(endTime);
        }

        List<String> userBankIds = new ArrayList<>();
        if (StringUtils.isNotEmpty(keyWords)) {
            //收款账户名
            UserExample userExample = new UserExample();
            userExample.createCriteria().andUsernameEqualTo(keyWords);
            List<User> users = userMapper.selectByExample(userExample);
            if (users.size() != 0) {
                User queryUser = users.get(0);
                UserBankExample userbankexample = new UserBankExample();
                userbankexample.createCriteria().andUserIdEqualTo(queryUser.getId());
                List<UserBank> userBanks = userBankMapper.selectByExample(userbankexample);
                if (userBanks.size() != 0) {

                    List<String> userBankId = userBanks.stream().map(UserBank::getId).collect(Collectors.toList());
                    userBankIds.addAll(userBankId);
                }
            }

            //账号
            UserBankExample userbankexample = new UserBankExample();
            userbankexample.createCriteria().andBankNumberEqualTo(keyWords);
            List<UserBank> userBanks = userBankMapper.selectByExample(userbankexample);
            if (userBanks.size() != 0) {
                List<String> userBankId = userBanks.stream().map(UserBank::getId).collect(Collectors.toList());
                userBankIds.addAll(userBankId);
            }
            //手机号
            UserExample userExample2 = new UserExample();
            userExample2.createCriteria().andTelphoneEqualTo(keyWords);
            List<User> users2 = userMapper.selectByExample(userExample2);
            if (users2.size() != 0) {
                User queryUser = users.get(0);
                UserBankExample userbankexample2 = new UserBankExample();
                userbankexample2.createCriteria().andUserIdEqualTo(queryUser.getId());
                List<UserBank> userBanks2 = userBankMapper.selectByExample(userbankexample);
                if (userBanks2.size() != 0) {

                    List<String> userBankId = userBanks2.stream().map(UserBank::getId).collect(Collectors.toList());
                    userBankIds.addAll(userBankId);
                }
            }

            //备注
            UserBankDetailExample example3 = new UserBankDetailExample();
            example3.createCriteria().andRemarkLike("%" + keyWords + "%");
            List<UserBankDetail> userBankDetails3 = userBankDetailMapper.selectByExample(example3);
            if (userBankDetails3.size() != 0) {
                List<String> userBankId = userBankDetails3.stream().map(UserBankDetail::getImportToUserBankId).collect(Collectors.toList());
                userBankIds.addAll(userBankId);
            }
        }
        if (userBankIds.size() > 0) {
            criteria.andImportToUserBankIdIn(userBankIds);
        }
        List<UserBankDetail> userBankDetails = userBankDetailMapper.selectByExample(example);

        List<TransactionResultDto> resultList = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");
        if (userBankDetails.size() > 0) {
            for (UserBankDetail userBankDetail : userBankDetails) {
                TransactionResultDto transactionResultDto = new TransactionResultDto();
                String importToUserBankId = userBankDetail.getImportToUserBankId();
                UserBank userBank = userBankMapper.selectByPrimaryKey(importToUserBankId);
                BeanUtils.copyProperties(userBankDetail, transactionResultDto);
                String bankNumber = userBank.getBankNumber();
                transactionResultDto.setBankNumber(bankNumber.replaceAll("(\\d{4})\\d{11}(\\d{4})", "$1***$2"));
                User user1 = userMapper.selectByPrimaryKey(userBank.getUserId());
                transactionResultDto.setUsername(user1.getUsername());
                transactionResultDto.setWeekDay(DateUtils.dateToWeek(userBankDetail.getTransactionDate()));
                transactionResultDto.setBankType(userBank.getBankType());

                String transtionMoney = decimalFormat.format(userBankDetail.getTransactionMoney().doubleValue());//会返回格式化后的金额
                if("代发代扣".equals(user1.getUsername())){
                    transactionResultDto.setTransactionMoney("+"+transtionMoney);
                }else{
                    transactionResultDto.setTransactionMoney("-"+transtionMoney);
                }
                resultList.add(transactionResultDto);
            }
        }
        return Result.ok().setData(resultList);
    }

    @Override
    public Result getTransactionDetail(String userBankId) {

        LOG.info("getTransactionDetail params:userBankId: {}", userBankId);

        UserBankDetail userBankDetail = userBankDetailMapper.selectByPrimaryKey(userBankId);
        return Result.ok().setData(userBankDetail);
    }

    @Override
    public Result addTransaction(AddTransactionDto addTransactionDto, User user) {
       //String transactionType = addTransactionDto.getTransactionType();
        String transactionChannel = addTransactionDto.getTransactionChannel();
        String remark = addTransactionDto.getRemark();
        BigDecimal exportMoney = addTransactionDto.getExportMoney();
        String importUserBankId = addTransactionDto.getImportUserBankId();
        Date transactionDate = addTransactionDto.getTransactionDate();

        if (exportMoney == null || exportMoney.compareTo(BigDecimal.ZERO) == 0) {
            return Result.failure("请输入转账金额！");
        }
        if (StringUtils.isEmpty(importUserBankId)) {
            return Result.failure("请选择对方账户！");
        }

        if(transactionDate==null){
            transactionDate= new Date();
        }
        //转出账户
        UserBankExample example = new UserBankExample();
        example.createCriteria().andUserIdEqualTo(user.getId());
        List<UserBank> userBankList = userBankMapper.selectByExample(example);

        if (userBankList.size() == 0) {
            return Result.failure("系统错误，请联系管理员！");
        }

        //转入账户
        UserBankExample example2 = new UserBankExample();
        example2.createCriteria().andUserIdEqualTo(importUserBankId);
        List<UserBank> userBankList2 = userBankMapper.selectByExample(example2);
        if (userBankList2.size() == 0) {
            return Result.failure("对方账户不存在！");
        }

        UserBank userBank = userBankList.get(0);

        UserBank userBank2 = userBankList2.get(0);
        User intoUser = userMapper.selectByPrimaryKey(userBank2.getUserId());

        BigDecimal balance = userBank.getBalance();
        BigDecimal balance1 = userBank2.getBalance();
        String bankType = userBank2.getBankType();
        if (balance.compareTo(exportMoney) < 0) {
            return Result.failure("账户余额不足！");
        }

        String channel = IdUtil.getStringId() + IdUtil.getStringId();
        UserBankDetail userBankDetail = new UserBankDetail();
        userBankDetail.setId(IdUtil.getStringId());
        userBankDetail.setTransactionChannel(transactionChannel);

        if(BankType.ZGJSYH.getCode().equals(bankType)){
            userBankDetail.setTransactionType(TransactionType.XBHZHZZ.getCode());
        }else{
            userBankDetail.setTransactionType(TransactionType.XTHZHZZ.getCode());
        }
        userBankDetail.setRemark(remark);
        userBankDetail.setExportFromUserBankId(userBank.getId());
        userBankDetail.setImportToUserBankId(userBank2.getId());


        userBankDetail.setTransactionMoney(exportMoney);
        userBankDetail.setChannel(channel.substring(0,25));
        userBankDetail.setTransactionDate(transactionDate);
        userBankDetailMapper.insert(userBankDetail);
        BigDecimal subtract = balance.subtract(exportMoney);
        userBank.setBalance(subtract);
        userBankMapper.updateByPrimaryKeySelective(userBank);

        BigDecimal add = balance1.add(exportMoney);
        userBank2.setBalance(add);
        userBankMapper.updateByPrimaryKeySelective(userBank2);
        return Result.ok();
    }

    @Override
    public Result getUserforTransaction(GetUserforTransactionDto getUserforTransactionDto, User user) {

        String username = getUserforTransactionDto.getUsername();
        String bankNumber = getUserforTransactionDto.getBankNumber();

        if (StringUtils.isEmpty(bankNumber)) {
            return Result.failure("请输入需要转账的账户！");
        }
        UserBankExample example2 = new UserBankExample();
        UserBankExample.Criteria criteria = example2.createCriteria().andBankNumberEqualTo(bankNumber);
        List<User> users = new ArrayList<>();
        if (StringUtils.isNotEmpty(username)) {
            UserExample example = new UserExample();
            example.createCriteria().andUsernameEqualTo(username);
            users = userMapper.selectByExample(example);
        }
        if (users.size() != 0) {
            criteria.andUserIdIn(users.stream().map(User::getId).collect(Collectors.toList()));
        }
        List<UserBank> userBankList = userBankMapper.selectByExample(example2);
        return Result.ok().setData(userBankList);
    }

    @Override
    public Result getBalance(User user) {
        //查询账户余额
        UserBankExample example2 = new UserBankExample();
        example2.createCriteria().andUserIdEqualTo(user.getId()).andBankTypeEqualTo(BankType.ZGJSYH.getCode());
        List<UserBank> userBankList = userBankMapper.selectByExample(example2);
        GetBalanceResultDto getBalanceResultDto = new GetBalanceResultDto();
        if (userBankList.size() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");
            String balance = decimalFormat.format(userBankList.get(0).getBalance().doubleValue());//会返回格式化后的金额
            getBalanceResultDto.setBalance(balance);
            getBalanceResultDto.setBankNumber(userBankList.get(0).getBankNumber());
        }
        return Result.ok().setData(getBalanceResultDto);
    }

    @Override
    public Result addOtherBankUser(RegisterDto registerDto) {
        String username = registerDto.getUsername();
        String telephone = registerDto.getTelephone();
        String bankNumber = registerDto.getBankNumber();
        String password = registerDto.getPassword();
        String idCard = registerDto.getIdCard();
        String bankType = registerDto.getBankType();
        //String depositBank = registerDto.getDepositBank();

        LOG.info("register params:username: {},telephone: {},bankNumber: {},password: {},idCard: {},bankType: {}",
                username,telephone,bankNumber,password,idCard,bankType);

        if(StringUtils.isEmpty(bankType)){
            return Result.failure("请选择银行！");
        }
        if (StringUtils.isEmpty(username)) {
            return Result.failure("客户姓名不能为空！");
        }
        if (StringUtils.isEmpty(telephone)) {
            return Result.failure("手机号不能为空！");
        }

        if(!PhoneUtils.isChinaPhoneLegal(telephone)){
            return Result.failure("手机号格式不正确！");
        }
        if (StringUtils.isEmpty(bankNumber)) {
            return Result.failure("银行账号不能为空！");
        }
        if(bankNumber.length()!=19){
            return Result.failure("银行账号格式不正确！");
        }
        if (StringUtils.isEmpty(password)) {
            return Result.failure("密码不能为空！");
        }
        if (StringUtils.isEmpty(idCard)) {
            return Result.failure("身份证号不能为空！");
        }
        if(!ValidateIdCardUtil.isIDCard(idCard)){
            return Result.failure("身份证号格式不正确！");
        }
//        if (StringUtils.isEmpty(depositBank)) {
//            return Result.failure("开户行不能为空！");
//        }

        UserExample example = new UserExample();
        example.createCriteria().andIdCartEqualTo(idCard).andUserTypeEqualTo(bankType);
        if (userMapper.selectByExample(example).size() > 0) {
            return Result.failure("账户已经被注册！");
        }
        User user = new User();
        user.setId(IdUtil.getStringId());
        user.setUsername(username);
        user.setPassword(password);
        user.setTelphone(telephone);
        user.setIdCart(idCard);
        user.setUserType(bankType);//其他银行
        userMapper.insert(user);

        UserBankExample uexample= new UserBankExample();
        uexample.createCriteria().andUserIdEqualTo(user.getId()).andBankTypeEqualTo(bankType);
        if(userBankMapper.countByExample(uexample)>0){
            return Result.failure("该银行账号已被注册！");
        }

        UserBank userBank = new UserBank();
        userBank.setId(IdUtil.getStringId());
        userBank.setUserId(user.getId());
        userBank.setBankType(bankType);
        userBank.setBankNumber(bankNumber);
        userBank.setBankName(BankType.get(bankType).getDescription());
        userBank.setBalance(BigDecimal.valueOf(100000));
        userBankMapper.insertSelective(userBank);

        return Result.ok();
    }

}
