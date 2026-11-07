package com.example.passwordstorage.data.security

import java.security.SecureRandom

enum class PasswordStrength(val label: String, val colorHex: Long) {
    WEAK("Weak", 0xFFE53935),
    MEDIUM("Medium", 0xFFFB8C00),
    STRONG("Strong", 0xFF43A047),
    VERY_STRONG("Very Strong", 0xFF1E88E5)
}

object PasswordGenerator {

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    fun generate(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val charPool = StringBuilder()
        val guaranteedChars = mutableListOf<Char>()
        val random = SecureRandom()

        if (includeUppercase) {
            charPool.append(UPPERCASE)
            guaranteedChars.add(UPPERCASE[random.nextInt(UPPERCASE.length)])
        }
        if (includeLowercase) {
            charPool.append(LOWERCASE)
            guaranteedChars.add(LOWERCASE[random.nextInt(LOWERCASE.length)])
        }
        if (includeNumbers) {
            charPool.append(NUMBERS)
            guaranteedChars.add(NUMBERS[random.nextInt(NUMBERS.length)])
        }
        if (includeSymbols) {
            charPool.append(SYMBOLS)
            guaranteedChars.add(SYMBOLS[random.nextInt(SYMBOLS.length)])
        }

        if (charPool.isEmpty()) {
            return ""
        }

        val passwordChars = ArrayList<Char>()
        passwordChars.addAll(guaranteedChars)

        val remainingLength = length - passwordChars.size
        for (i in 0 until remainingLength.coerceAtLeast(0)) {
            val randomIndex = random.nextInt(charPool.length)
            passwordChars.add(charPool[randomIndex])
        }

        passwordChars.shuffle(random)
        return passwordChars.joinToString("")
    }

    fun calculateStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 12) score += 2
        else if (password.length >= 8) score += 1

        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (password.any { !it.isLetterOrDigit() }) score += 1

        return when {
            score <= 2 -> PasswordStrength.WEAK
            score == 3 -> PasswordStrength.MEDIUM
            score == 4 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }
}
