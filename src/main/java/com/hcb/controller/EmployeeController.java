package com.hcb.controller;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcb.common.R;
import com.hcb.entity.Employee;
import com.hcb.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1.将页面提交的密码进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee one = employeeService.getOne(lqw);

        //3.如果没有查询到则返回登录失败结果
        if(one == null){
            return R.error("没有此账户！");
        }

        //4.密码比对
        if(!one.getPassword().equals(password)){
            return R.error("密码错误！");
        }

        //5.查看员工状态，0禁用，1启用
        if(one.getStatus() == 0){
            return R.error("账号已封禁！");
        }

        //6.登陆成功，将员工ID存入Session并返回登陆成功结果
        request.getSession().setAttribute("employee",one.getId());
        return R.success(one);
    }

    /**
     * 员工登出
     * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前员工的ID
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }

    /**
     * 新增员工
     * */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工,员工信息：{}",employee.toString());

        //设置初始密码123456,需要md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //以下操作交给MP自动填充字段实现
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获取当前用户的ID
        //Long EmpId = (Long) request.getSession().getAttribute("employee");

        //employee.setCreateUser(EmpId);
        //employee.setUpdateUser(EmpId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息的分页查询
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
            //添加过滤条件
        lqw.like(!StringUtils.isEmpty(name), Employee::getName, name);
            //添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,lqw);

        return R.success(pageInfo);
    }

    /**
     * (根据Id)
     * 员工信息的修改（包括状态的修改）
     * */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){

        //以下操作 由 MP 自动填充字段 解决

        //设置更新人 和 更新的时间
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateUser(empId);
        //employee.setUpdateTime(LocalDateTime.now());
        //执行状态更新操作
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
    * 根据Id查询员工信息
    * */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("查无此人");
    }
}
