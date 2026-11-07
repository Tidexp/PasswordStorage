package com.example.passwordstorage.data.repository

import com.example.passwordstorage.data.local.AuditLogDao
import com.example.passwordstorage.data.local.PasswordPolicyDao
import com.example.passwordstorage.data.local.UserDao
import com.example.passwordstorage.data.local.VaultDao
import com.example.passwordstorage.data.model.AuditLogEntity
import com.example.passwordstorage.data.model.PasswordPolicyEntity
import com.example.passwordstorage.data.model.UserEntity
import com.example.passwordstorage.data.model.UserRole
import com.example.passwordstorage.data.model.UserStatus
import kotlinx.coroutines.flow.Flow

class AdminRepository(
    private val userDao: UserDao,
    private val auditLogDao: AuditLogDao,
    private val passwordPolicyDao: PasswordPolicyDao,
    private val vaultDao: VaultDao
) {

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    fun getPasswordPolicy(): Flow<PasswordPolicyEntity?> = passwordPolicyDao.getPolicy()

    suspend fun toggleUserLock(adminEmail: String, targetEmail: String, currentStatus: UserStatus) {
        val newStatus = if (currentStatus == UserStatus.ACTIVE) UserStatus.LOCKED else UserStatus.ACTIVE
        userDao.updateUserStatus(targetEmail, newStatus)

        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = adminEmail,
                action = if (newStatus == UserStatus.LOCKED) "REMOTE_LOCK_USER" else "UNLOCK_USER",
                status = "SUCCESS",
                details = "Admin ($adminEmail) đã ${if (newStatus == UserStatus.LOCKED) "khóa từ xa (Remote Lock)" else "mở khóa"} tài khoản: $targetEmail."
            )
        )
    }

    suspend fun revokeUserAccess(adminEmail: String, targetEmail: String) {
        // Clear session & lock user
        userDao.updateUserStatus(targetEmail, UserStatus.LOCKED)
        vaultDao.deleteAllUserItems(targetEmail)

        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = adminEmail,
                action = "REMOTE_REVOKE_ACCESS",
                status = "SUCCESS",
                details = "Admin ($adminEmail) đã thu hồi quyền truy cập (Revoke Access) và xóa sạch dữ liệu cục bộ của $targetEmail."
            )
        )
    }

    suspend fun toggleUserRole(adminEmail: String, targetEmail: String, currentRole: UserRole) {
        val newRole = if (currentRole == UserRole.USER) UserRole.ADMIN else UserRole.USER
        userDao.updateUserRole(targetEmail, newRole)

        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = adminEmail,
                action = "UPDATE_USER_ROLE",
                status = "SUCCESS",
                details = "Admin ($adminEmail) đã đổi vai trò của $targetEmail thành $newRole."
            )
        )
    }

    suspend fun updatePasswordPolicy(adminEmail: String, policy: PasswordPolicyEntity) {
        passwordPolicyDao.savePolicy(policy)
        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = adminEmail,
                action = "UPDATE_PASSWORD_POLICY",
                status = "SUCCESS",
                details = "Admin ($adminEmail) đã cập nhật chính sách mật khẩu: độ dài tối thiểu=${policy.minLength}, chữ hoa=${policy.requireUppercase}, ký tự đặc biệt=${policy.requireSymbols}."
            )
        )
    }

    suspend fun clearAuditLogs(adminEmail: String) {
        auditLogDao.clearLogs()
        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = adminEmail,
                action = "CLEAR_AUDIT_LOGS",
                status = "SUCCESS",
                details = "Admin ($adminEmail) đã dọn dẹp tất cả nhật ký kiểm tra cũ."
            )
        )
    }
}
