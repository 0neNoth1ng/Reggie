package com.hcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcb.common.R;
import com.hcb.entity.User;
import com.hcb.service.UserService;
import com.hcb.utils.SMSUtils;
import com.hcb.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * */
    @PostMapping("/sendMsg")
    public R<String > sendMsg(HttpSession session, @RequestBody User user){
        //获取手机号
        String phone = user.getPhone();
        if(!StringUtils.isEmpty(phone)){
            //生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //调用阿里云提供的短信服务API 发送短信
            SMSUtils.sendMessage("何","",phone,code);
            //需要将生成的验证码保存到Session中
            session.setAttribute(phone,code);
            return R.success("手机验证码发送成功");
        }
        return R.error("手机验证码发送失败");

    }

    /**
     * 移动端用户登录
     * 前端也没有发短信的方法接口
     * 这里功能不完善，得过且过，没有校验直接登录了
     * 100%登录成功
     * */
    @PostMapping("/login")
    public R<User > login(HttpSession session, @RequestBody Map user){

        //获取手机号
        String phone = user.get("phone").toString();

        //查询是否为新用户
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getPhone,phone);
        User one = userService.getOne(lqw);
        //如果是新用户，注册
        if(one == null){
            one = new User();
            one.setPhone(phone);
            one.setStatus(1);
            userService.save(one);
        }
        //不是新用户，登录成功

        //存入Session
        session.setAttribute("user",one.getId());


        return R.success(one);
    }
}
