package com.simon.config;

import com.simon.model.User;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;
@Data
public class GlobalVariable {

    public static Map<String, User> tokenMap = new HashMap<>();
}