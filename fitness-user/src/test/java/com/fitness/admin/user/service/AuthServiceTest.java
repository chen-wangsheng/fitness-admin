package com.fitness.admin.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.event.LoginEvent;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.user.dto.LoginRequest;
import com.fitness.admin.user.dto.LoginResponse;
import com.fitness.admin.user.entity.AdminUser;
import com.fitness.admin.user.mapper.AdminUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证服务单测。
 *
 * <p>使用 Mockito 隔离 Sa-Token、Mapper、EventPublisher 等依赖。
 * 重点覆盖登录成功/失败、改密等核心分支。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        stpUtilMock = Mockito.mockStatic(StpUtil.class);
        // Sa-Token 内部需要 SaTokenContext,直接 stub 掉以避免真实调用
        stpUtilMock.when(() -> StpUtil.login(anyLong())).thenAnswer(inv -> "mock-token");
    }

    @AfterEach
    void tearDown() {
        stpUtilMock.close();
    }

    @Test
    void login_withCorrectPassword_shouldReturnTokenAndUserInfo() {
        AdminUser user = newAdminUser(1L, "admin", MD5_PASSWORD, 1);
        when(adminUserMapper.selectByUsername("admin")).thenReturn(user);
        stpUtilMock.when(() -> StpUtil.getTokenValue()).thenReturn("token-abc");
        stpUtilMock.when(() -> StpUtil.getTokenTimeout()).thenReturn(7200L);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");
        MockHttpServletRequest http = new MockHttpServletRequest();
        http.setRemoteAddr("127.0.0.1");

        LoginResponse response = authService.login(request, http);

        assertEquals("token-abc", response.getToken());
        assertEquals(7200L, response.getExpire());
        assertNotNull(response.getUserInfo());
        assertEquals("admin", response.getUserInfo().getUsername());
        assertEquals("nick", response.getUserInfo().getNickname());

        ArgumentCaptor<LoginEvent> eventCaptor = ArgumentCaptor.forClass(LoginEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        // 成功事件 loginStatus=1
        assertEquals(1, eventCaptor.getValue().getLoginStatus());

        verify(adminUserMapper).updateById(any(AdminUser.class));
        stpUtilMock.verify(() -> StpUtil.login(anyLong()));
    }

    @Test
    void login_userNotFound_shouldThrowAndPublishFailureEvent() {
        when(adminUserMapper.selectByUsername("ghost")).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setUsername("ghost");
        request.setPassword("any");
        MockHttpServletRequest http = new MockHttpServletRequest();

        BizException ex = assertThrows(BizException.class, () -> authService.login(request, http));
        assertEquals(ResultCodeEnum.USER_NOT_FOUND.getCode(), ex.getCode());

        ArgumentCaptor<LoginEvent> eventCaptor = ArgumentCaptor.forClass(LoginEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(2, eventCaptor.getValue().getLoginStatus());
        assertEquals("用户不存在", eventCaptor.getValue().getFailReason());
        stpUtilMock.verify(() -> StpUtil.login(anyLong()), never());
    }

    @Test
    void login_disabledUser_shouldThrowUserDisabled() {
        AdminUser user = newAdminUser(2L, "locked", "x", 0);
        when(adminUserMapper.selectByUsername("locked")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("locked");
        request.setPassword("any");
        MockHttpServletRequest http = new MockHttpServletRequest();

        BizException ex = assertThrows(BizException.class, () -> authService.login(request, http));
        assertEquals(ResultCodeEnum.USER_DISABLED.getCode(), ex.getCode());
    }

    @Test
    void login_wrongPassword_shouldThrowAndNotLogin() {
        AdminUser user = newAdminUser(1L, "admin", MD5_PASSWORD, 1);
        when(adminUserMapper.selectByUsername("admin")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        MockHttpServletRequest http = new MockHttpServletRequest();

        BizException ex = assertThrows(BizException.class, () -> authService.login(request, http));
        assertEquals(ResultCodeEnum.USER_PASSWORD_ERROR.getCode(), ex.getCode());
        stpUtilMock.verify(() -> StpUtil.login(anyLong()), never());
        verify(adminUserMapper, never()).updateById(any(AdminUser.class));
    }

    @Test
    void login_shouldUseXForwardedForIpWhenPresent() {
        AdminUser user = newAdminUser(1L, "admin", MD5_PASSWORD, 1);
        when(adminUserMapper.selectByUsername("admin")).thenReturn(user);
        stpUtilMock.when(() -> StpUtil.getTokenValue()).thenReturn("t");
        stpUtilMock.when(() -> StpUtil.getTokenTimeout()).thenReturn(1L);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");
        MockHttpServletRequest http = new MockHttpServletRequest();
        http.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        http.addHeader("User-Agent", "JUnit");

        authService.login(request, http);

        ArgumentCaptor<AdminUser> userCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).updateById(userCaptor.capture());
        assertEquals("10.0.0.1", userCaptor.getValue().getLastLoginIp());
    }

    @Test
    void changePassword_withWrongOldPassword_shouldThrow() {
        AdminUser user = newAdminUser(10L, "u", "old-md5", 1);
        stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(10L);
        when(adminUserMapper.selectById(10L)).thenReturn(user);

        BizException ex = assertThrows(BizException.class, () -> authService.changePassword("wrong", "new"));
        assertEquals(ResultCodeEnum.USER_PASSWORD_ERROR.getCode(), ex.getCode());
        verify(adminUserMapper, never()).updateById(any(AdminUser.class));
    }

    @Test
    void changePassword_withCorrectOldPassword_shouldUpdate() {
        AdminUser user = newAdminUser(10L, "u", MD5_PASSWORD, 1);
        stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(10L);
        when(adminUserMapper.selectById(10L)).thenReturn(user);

        authService.changePassword("password", "newpwd");

        ArgumentCaptor<AdminUser> captor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).updateById(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals(cn.hutool.crypto.digest.DigestUtil.md5Hex("newpwd"), captor.getValue().getPassword());
    }

    @Test
    void getCurrentUserInfo_shouldReturnUserInfoAndSuperAdminPerms() {
        AdminUser user = newAdminUser(99L, "root", "x", 1);
        stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(99L);
        when(adminUserMapper.selectById(99L)).thenReturn(user);

        LoginResponse.UserInfo info = authService.getCurrentUserInfo();

        assertEquals(99L, info.getId());
        assertEquals("root", info.getUsername());
        assertTrue(info.getPermissions().contains("*"));
    }

    @Test
    void getCurrentUserInfo_userMissing_shouldThrow() {
        stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(123L);
        when(adminUserMapper.selectById(123L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> authService.getCurrentUserInfo());
        assertEquals(ResultCodeEnum.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void logout_shouldCallStpUtil() {
        authService.logout();
        stpUtilMock.verify(StpUtil::logout, times(1));
    }

    @Test
    void refreshToken_shouldReturnCurrentToken() {
        stpUtilMock.when(() -> StpUtil.getTokenValue()).thenReturn("t-2");
        stpUtilMock.when(() -> StpUtil.getTokenTimeout()).thenReturn(7200L);

        LoginResponse response = authService.refreshToken();

        assertEquals("t-2", response.getToken());
        assertEquals(7200L, response.getExpire());
        stpUtilMock.verify(() -> StpUtil.renewTimeout(7200), times(1));
    }

    private static final String MD5_PASSWORD = cn.hutool.crypto.digest.DigestUtil.md5Hex("password");

    private AdminUser newAdminUser(Long id, String username, String password, int status) {
        AdminUser u = new AdminUser();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(password);
        u.setNickname("nick");
        u.setAvatar("https://example.com/avatar.png");
        u.setStatus(status);
        u.setDeleted(0);
        u.setLastLoginTime(LocalDateTime.now());
        return u;
    }
}
