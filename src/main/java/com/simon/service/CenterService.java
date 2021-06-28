package com.simon.service;

import com.simon.dto.*;
import com.simon.model.User;
import com.simon.utils.Result;

public interface CenterService {
    Result login(LoginDto loginDto);

    Result register(RegisterDto registerDto);

    Result getTransactionList(TransactionDto transactionDto, User user);

    Result getTransactionDetail(String userBankId);

    Result addTransaction(AddTransactionDto addTransactionDto, User user);

    Result getUserforTransaction(GetUserforTransactionDto getUserforTransactionDto, User user);

    Result getBalance(User user);

    Result addOtherBankUser(RegisterDto registerDto);
}
