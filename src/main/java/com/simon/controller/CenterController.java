package com.simon.controller;

import com.simon.config.GlobalVariable;
import com.simon.dto.*;
import com.simon.model.User;
import com.simon.service.CenterService;
import com.simon.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "中心控制器（所有接口都在这里面）")
@RestController
public class CenterController {

    @Resource
    private CenterService centerServiceImpl;

    @PostMapping("/login")
    @ApiOperation(value = "我的->登录开通->登录")
    public Result login(@RequestBody LoginDto loginDto) {

        return centerServiceImpl.login(loginDto);
    }

    @PostMapping("/loginout")
    @ApiOperation(value = "我的->登录开通->退出")
    public Result loginout(@RequestHeader("token") String token) {

        GlobalVariable.tokenMap.remove(token);
        return Result.ok();
    }

    @PostMapping("/register")
    @ApiOperation(value = "我的->登录开通->开通")
    public Result register(@RequestBody RegisterDto registerDto) {

        return centerServiceImpl.register(registerDto);
    }

    @PostMapping("/getTransactionList")
    @ApiOperation(value = "首页->转账汇款->转账记录")
    public Result getTransactionList(@RequestBody TransactionDto transactionDto,
                                     @RequestHeader("token") String token) {

        User user= getUserFromCache(token);
        if(user==null){
            return  Result.failure("请登录！");
        }
        return centerServiceImpl.getTransactionList(transactionDto,user);
    }

    @PostMapping("/getTransactionDetail")
    @ApiOperation(value = "首页->转账汇款->转账记录->查看详情(使用转账记录替代)")
    public Result getTransactionDetail(@RequestParam(name = "userBankId") @ApiParam(value = "转账记录ID") String userBankId,
                                     @RequestHeader("token") String token) {

        User user= getUserFromCache(token);
        if(user==null){
            return  Result.failure("请登录！");
        }
        return centerServiceImpl.getTransactionDetail(userBankId);
    }



    @PostMapping("/getUserforTransaction")
    @ApiOperation(value = "查询需要转账的用户")
    public Result getUserforTransaction(@RequestBody GetUserforTransactionDto getUserforTransactionDto,
                                 @RequestHeader("token") String token) {

        User user = getUserFromCache(token);
        if(user==null){
            return  Result.failure("请登录！");
        }
        return centerServiceImpl.getUserforTransaction(getUserforTransactionDto,user);
    }

    @PostMapping("/addTransaction")
    @ApiOperation(value = "管理界面（增加转账记录）")
    public Result addTransaction(@RequestBody AddTransactionDto addTransactionDto,
                                 @RequestHeader("token") String token) {

        User user = getUserFromCache(token);
        if(user==null){
            return  Result.failure("请登录！");
        }
        return centerServiceImpl.addTransaction(addTransactionDto,user);
    }

    @PostMapping("/addOtherBankUser")
    @ApiOperation(value = "管理界面（增加其他银行账户）")
    public Result addOtherBankUser(@RequestBody RegisterDto registerDto,
                                 @RequestHeader("token") String token) {


        return centerServiceImpl.addOtherBankUser(registerDto);
    }

    @PostMapping("/getBalance")
    @ApiOperation(value = "首页查询账户余额（登录接口也会返回账户余额）")
    public Result getBalance(@RequestHeader("token") String token) {

        User user = getUserFromCache(token);
        if(user==null){
            return  Result.failure("请登录！");
        }
        return centerServiceImpl.getBalance(user);
    }

    private User getUserFromCache(String token) {
        return GlobalVariable.tokenMap.get(token);
    }
}
