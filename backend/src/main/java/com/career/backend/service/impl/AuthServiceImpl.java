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

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private AdminService adminService;
    @Autowired private StudentService studentService;
    @Autowired private CounselorService counselorService;
    @Autowired private CompanyService companyService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String rawPassword = request.getPassword();

        // 依次查 admin → student → counselor → company
        Admin admin = adminService.getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUserName, username));
        if (admin != null && passwordEncoder.matches(rawPassword, admin.getPassword())) {
            return new LoginResponse(jwtUtil.generateToken(username, "admin"), "admin",
                    admin.getRealName(), admin.getUserName());
        }

        Student student = studentService.getOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserName, username));
        if (student != null && passwordEncoder.matches(rawPassword, student.getPassword())) {
            return new LoginResponse(jwtUtil.generateToken(username, "student"), "student",
                    student.getRealName(), student.getUserName());
        }

        Counselor counselor = counselorService.getOne(new LambdaQueryWrapper<Counselor>()
                .eq(Counselor::getUserName, username));
        if (counselor != null && passwordEncoder.matches(rawPassword, counselor.getPassword())) {
            return new LoginResponse(jwtUtil.generateToken(username, "counselor"), "counselor",
                    counselor.getRealName(), counselor.getUserName());
        }

        Company company = companyService.getOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserName, username));
        if (company != null && passwordEncoder.matches(rawPassword, company.getPassword())) {
            return new LoginResponse(jwtUtil.generateToken(username, "company"), "company",
                    company.getCompanyName(), company.getUserName());
        }

        throw new RuntimeException("账号或密码错误");
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
}
