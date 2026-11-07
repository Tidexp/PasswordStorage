package com.example.passwordstorage.data.repository

import com.example.passwordstorage.data.local.AuditLogDao
import com.example.passwordstorage.data.local.PasswordPolicyDao
import com.example.passwordstorage.data.local.UserDao
import com.example.passwordstorage.data.model.AuditLogEntity
import com.example.passwordstorage.data.model.PasswordPolicyEntity
import com.example.passwordstorage.data.model.UserEntity
import com.example.passwordstorage.data.model.UserRole
import com.example.passwordstorage.data.model.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(
    private val userDao: UserDao,
    private val auditLogDao: AuditLogDao,
    private val passwordPolicyDao: PasswordPolicyDao
) {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun login(email: String, password: String): AuthResult {
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = email,
                    action = "USER_LOGIN",
                    status = "FAILED",
                    details = "Đăng nhập thất bại: Tài khoản không tồn tại."
                )
            )
            return AuthResult.Error("Tài khoản không tồn tại.")
        }

        if (user.status == UserStatus.LOCKED) {
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = email,
                    action = "USER_LOGIN",
                    status = "FAILED",
                    details = "Đăng nhập bị từ chối: Tài khoản bị khóa bởi Admin (Remote Lock)."
                )
            )
            return AuthResult.Error("Tài khoản đã bị Admin khóa hoặc hủy quyền truy cập từ xa.")
        }

        if (user.passwordHash != password) {
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = email,
                    action = "USER_LOGIN",
                    status = "FAILED",
                    details = "Mật khẩu không chính xác."
                )
            )
            return AuthResult.Error("Mật khẩu không đúng.")
        }

        userDao.updateLastLogin(email, System.currentTimeMillis())
        val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
        _currentUser.value = updatedUser

        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = email,
                action = "USER_LOGIN",
                status = "SUCCESS",
                details = "Đăng nhập thành công với vai trò ${user.role}."
            )
        )

        return AuthResult.Success(updatedUser)
    }

    suspend fun loginWithBiometric(email: String): AuthResult {
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            return AuthResult.Error("Tài khoản không tồn tại.")
        }
        if (user.status == UserStatus.LOCKED) {
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = email,
                    action = "BIOMETRIC_LOGIN",
                    status = "FAILED",
                    details = "Sinh trắc học bị từ chối: Tài khoản đang bị Admin khóa."
                )
            )
            return AuthResult.Error("Tài khoản đã bị khóa từ xa.")
        }

        userDao.updateLastLogin(email, System.currentTimeMillis())
        val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
        _currentUser.value = updatedUser

        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = email,
                action = "BIOMETRIC_LOGIN",
                status = "SUCCESS",
                details = "Đăng nhập sinh trắc học thành công (Fingerprint/Face ID)."
            )
        )

        return AuthResult.Success(updatedUser)
    }

    suspend fun register(email: String, password: String, role: UserRole = UserRole.USER): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Vui lòng điền đầy đủ thông tin.")
        }

        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return AuthResult.Error("Email này đã được đăng ký.")
        }

        // Validate Password Policy
        val policy = passwordPolicyDao.getPolicyDirect() ?: PasswordPolicyEntity()
        if (password.length < policy.minLength) {
            return AuthResult.Error("Mật khẩu phải có ít nhất ${policy.minLength} ký tự theo quy định Admin.")
        }
        if (policy.requireUppercase && !password.any { it.isUpperCase() }) {
            return AuthResult.Error("Mật khẩu phải chứa ít nhất 1 chữ hoa.")
        }
        if (policy.requireLowercase && !password.any { it.isLowerCase() }) {
            return AuthResult.Error("Mật khẩu phải chứa ít nhất 1 chữ thường.")
        }
        if (policy.requireNumbers && !password.any { it.isDigit() }) {
            return AuthResult.Error("Mật khẩu phải chứa ít nhất 1 số.")
        }
        if (policy.requireSymbols && !password.any { !it.isLetterOrDigit() }) {
            return AuthResult.Error("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt.")
        }

        val newUser = UserEntity(
            email = email,
            passwordHash = password,
            role = role,
            status = UserStatus.ACTIVE
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser

        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = email,
                action = "USER_REGISTER",
                status = "SUCCESS",
                details = "Đăng ký tài khoản mới thành công (Role: $role)."
            )
        )

        return AuthResult.Success(newUser)
    }

    suspend fun logout() {
        val user = _currentUser.value
        if (user != null) {
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = user.email,
                    action = "USER_LOGOUT",
                    status = "SUCCESS",
                    details = "Đăng xuất khỏi hệ thống."
                )
            )
        }
        _currentUser.value = null
    }
}
