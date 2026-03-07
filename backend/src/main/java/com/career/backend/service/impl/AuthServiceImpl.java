package com.career.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.backend.dto.LoginRequest;
import com.career.backend.dto.LoginResponse;
import com.career.backend.dto.RegisterRequest;
import com.career.backend.entity.Admin;
import com.career.backend.entity.Company;
import com.career.backend.entity.Counselor;
import com.career.backend.entity.Student;
import com.career.backend.service.AdminService;
import com.career.backend.service.AuthService;
import com.career.backend.service.CompanyService;
import com.career.backend.service.CounselorService;
import com.career.backend.service.StudentService;
import com.career.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private AdminService adminService;
    @Autowired private StudentService studentService;
    @Autowired private CounselorService counselorService;
    @Autowired private CompanyService companyService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    // 记录每个用户名的连续登录失败次数
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String rawPassword = request.getPassword();

        // 检查是否已被锁定
        int attempts = loginAttempts.getOrDefault(username, 0);
        if (attempts >= MAX_ATTEMPTS) {
            throw new RuntimeException("LOCKED:您已连续输错5次密码，账号已锁定。请点击\"忘记密码\"重置密码后再登录。");
        }

        // 依次查 admin → student → counselor → company
        Admin admin = adminService.getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUserName, username));
        if (admin != null && passwordEncoder.matches(rawPassword, admin.getPassword())) {
            loginAttempts.remove(username);
            return new LoginResponse(jwtUtil.generateToken(username, "admin"), "admin",
                    admin.getRealName(), admin.getUserName(), admin.getId());
        }

        Student student = studentService.getOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserName, username));
        if (student != null && passwordEncoder.matches(rawPassword, student.getPassword())) {
            loginAttempts.remove(username);
            return new LoginResponse(jwtUtil.generateToken(username, "student"), "student",
                    student.getRealName(), student.getUserName(), student.getId());
        }

        Counselor counselor = counselorService.getOne(new LambdaQueryWrapper<Counselor>()
                .eq(Counselor::getUserName, username));
        if (counselor != null && passwordEncoder.matches(rawPassword, counselor.getPassword())) {
            loginAttempts.remove(username);
            return new LoginResponse(jwtUtil.generateToken(username, "counselor"), "counselor",
                    counselor.getRealName(), counselor.getUserName(), counselor.getId());
        }

        Company company = companyService.getOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserName, username));
        if (company != null && passwordEncoder.matches(rawPassword, company.getPassword())) {
            loginAttempts.remove(username);
            return new LoginResponse(jwtUtil.generateToken(username, "company"), "company",
                    company.getCompanyName(), company.getUserName(), company.getId(), company.getStatus());
        }

        // 登录失败，增加失败次数
        int newAttempts = loginAttempts.merge(username, 1, Integer::sum);
        if (newAttempts >= MAX_ATTEMPTS) {
            throw new RuntimeException("LOCKED:您已连续输错5次密码，账号已锁定。请点击\"忘记密码\"重置密码后再登录。");
        }
        throw new RuntimeException("账号或密码错误，还可尝试" + (MAX_ATTEMPTS - newAttempts) + "次");
    }

    @Override
    public void register(RegisterRequest request) {
        String role = request.getRole();
        if (role == null || role.isBlank()) {
            throw new RuntimeException("请选择注册角色");
        }

        String encodedPwd = passwordEncoder.encode(request.getPassword());
        LocalDateTime now = LocalDateTime.now();

        switch (role) {
            case "student" -> {
                boolean exists = studentService.count(new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserName, request.getUsername())) > 0;
                if (exists) throw new RuntimeException("账号已存在");
                Student s = new Student();
                s.setUserName(request.getUsername());
                s.setPassword(encodedPwd);
                s.setRealName(request.getRealName());
                s.setEmail(request.getEmail());
                s.setPhone(request.getPhone());
                s.setSchool(request.getSchool());
                s.setMajor(request.getMajor());
                s.setDepartment(request.getDepartment());
                s.setCounselorId(request.getCounselorId());
                s.setCreateAt(now);
                s.setUpdatedAt(now);
                studentService.save(s);
            }
            case "counselor" -> {
                boolean exists = counselorService.count(new LambdaQueryWrapper<Counselor>()
                        .eq(Counselor::getUserName, request.getUsername())) > 0;
                if (exists) throw new RuntimeException("账号已存在");
                Counselor c = new Counselor();
                c.setUserName(request.getUsername());
                c.setPassword(encodedPwd);
                c.setRealName(request.getRealName());
                c.setEmail(request.getEmail());
                c.setPhone(request.getPhone());
                c.setDepartment(request.getDepartment());
                c.setCreateAt(now);
                c.setUpdatedAt(now);
                counselorService.save(c);
            }
            case "company" -> {
                boolean exists = companyService.count(new LambdaQueryWrapper<Company>()
                        .eq(Company::getUserName, request.getUsername())) > 0;
                if (exists) throw new RuntimeException("账号已存在");
                Company co = new Company();
                co.setUserName(request.getUsername());
                co.setPassword(encodedPwd);
                co.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : request.getUsername());
                co.setEmail(request.getEmail());
                co.setPhone(request.getPhone());
                co.setIndustry(request.getIndustry());
                co.setStatus(0);
                co.setCreateAt(now);
                co.setUpdatedAt(now);
                companyService.save(co);
            }
            default -> throw new RuntimeException("不支持的角色类型：" + role);
        }
    }

    @Override
    public void resetPassword(RegisterRequest request) {
        String role = request.getRole();
        String username = request.getUsername();
        String newPassword = request.getPassword();

        if (role == null || role.isBlank()) throw new RuntimeException("请选择角色");
        if (username == null || username.isBlank()) throw new RuntimeException("请输入账号");
        if (newPassword == null || newPassword.isBlank()) throw new RuntimeException("请输入新密码");

        String encodedPwd = passwordEncoder.encode(newPassword);
        LocalDateTime now = LocalDateTime.now();

        switch (role) {
            case "student" -> {
                Student s = studentService.getOne(new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserName, username));
                if (s == null) throw new RuntimeException("账号不存在");
                // 验证注册时填写的字段
                if (!matchField(request.getRealName(), s.getRealName()))
                    throw new RuntimeException("真实姓名不匹配");
                if (!matchField(request.getEmail(), s.getEmail()))
                    throw new RuntimeException("邮箱不匹配");
                if (!matchField(request.getPhone(), s.getPhone()))
                    throw new RuntimeException("手机号不匹配");
                s.setPassword(encodedPwd);
                s.setUpdatedAt(now);
                studentService.updateById(s);
                loginAttempts.remove(username);
            }
            case "counselor" -> {
                Counselor c = counselorService.getOne(new LambdaQueryWrapper<Counselor>()
                        .eq(Counselor::getUserName, username));
                if (c == null) throw new RuntimeException("账号不存在");
                if (!matchField(request.getRealName(), c.getRealName()))
                    throw new RuntimeException("真实姓名不匹配");
                if (!matchField(request.getEmail(), c.getEmail()))
                    throw new RuntimeException("邮箱不匹配");
                if (!matchField(request.getPhone(), c.getPhone()))
                    throw new RuntimeException("手机号不匹配");
                c.setPassword(encodedPwd);
                c.setUpdatedAt(now);
                counselorService.updateById(c);
                loginAttempts.remove(username);
            }
            case "company" -> {
                Company co = companyService.getOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getUserName, username));
                if (co == null) throw new RuntimeException("账号不存在");
                if (!matchField(request.getCompanyName(), co.getCompanyName()))
                    throw new RuntimeException("企业名称不匹配");
                if (!matchField(request.getEmail(), co.getEmail()))
                    throw new RuntimeException("邮箱不匹配");
                if (!matchField(request.getPhone(), co.getPhone()))
                    throw new RuntimeException("手机号不匹配");
                co.setPassword(encodedPwd);
                co.setUpdatedAt(now);
                companyService.updateById(co);
                loginAttempts.remove(username);
            }
            default -> throw new RuntimeException("不支持的角色类型");
        }
    }

    /** 验证字段是否匹配（两边都为空视为匹配） */
    private boolean matchField(String input, String dbValue) {
        if ((input == null || input.isBlank()) && (dbValue == null || dbValue.isBlank())) return true;
        if (input == null || dbValue == null) return false;
        return input.trim().equals(dbValue.trim());
    }
}
