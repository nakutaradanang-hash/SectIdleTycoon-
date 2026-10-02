package com.sect.idle.hub.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sect.idle.hub.*
import com.sect.idle.security.EncryptionManager
import com.sect.idle.security.InputSecurityValidator
import com.sect.idle.security.MfaManager
import com.sect.idle.security.SecurityAuditLogger

@Composable
fun SecuritySettingsDialog(
    onDismissRequest: () -> Unit,
    sectName: String = "Immortal Sect"
) {
    val context = LocalContext.current
    val mfaManager = remember { MfaManager.getInstance(context) }
    val encryptionManager = remember { EncryptionManager.getInstance(context) }

    var isMfaEnabled by remember { mutableStateOf(mfaManager.isMfaEnabled()) }
    var masterPinInput by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("") }
    var pinMessageColor by remember { mutableStateOf(SuccessGreen) }

    var totpSecret by remember {
        val existing = mfaManager.totpSecret
        mutableStateOf(if (existing.isNotEmpty()) existing else mfaManager.generateTotpSecret())
    }
    var otpVerifyInput by remember { mutableStateOf("") }
    var otpMessage by remember { mutableStateOf("") }
    var otpMessageColor by remember { mutableStateOf(SuccessGreen) }

    var backupCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var showBackupCodes by remember { mutableStateOf(false) }

    // Live Security Tester States
    var testInputText by remember { mutableStateOf("' OR '1'='1' --") }
    var validationResult by remember { mutableStateOf<InputSecurityValidator.ValidationResult?>(null) }
    var auditLogs by remember { mutableStateOf(SecurityAuditLogger.getRecentLogs()) }

    var selectedSubTab by remember { mutableStateOf(0) } // 0: MFA Setup, 1: Data Encryption & Status, 2: Input Validator Tester

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("security_settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.verticalGradient(listOf(JadeCyan, CelestialGold)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(JadeDark.copy(alpha = 0.4f))
                                .border(1.5.dp, CelestialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Heavenly Security Vault",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CloudMistWhite
                            )
                            Text(
                                text = "Multi-Factor Authentication & Multi-Layer Defense",
                                fontSize = 11.sp,
                                color = JadeCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_security_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = TwilightElevated,
                    contentColor = JadeCyan,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = { Text("MFA & 2FA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = { Text("Vault & Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedSubTab == 2,
                        onClick = { selectedSubTab = 2 },
                        text = { Text("WAF / SQLi Guard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedSubTab) {
                    0 -> {
                        // MFA Management Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Master Toggle
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isMfaEnabled) JadeCyan else TwilightBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Multi-Factor Authentication (MFA)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = CloudMistWhite
                                            )
                                            Text(
                                                text = if (isMfaEnabled) "Active: Protection against unauthorized sect access" else "Inactive: Enable 2FA to protect your dao progression",
                                                fontSize = 11.sp,
                                                color = if (isMfaEnabled) SuccessGreen else TextMuted
                                            )
                                        }
                                        Switch(
                                            checked = isMfaEnabled,
                                            onCheckedChange = { checked ->
                                                isMfaEnabled = checked
                                                mfaManager.setMfaEnabled(checked)
                                                Toast.makeText(
                                                    context,
                                                    if (checked) "MFA Activated!" else "MFA Deactivated",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.testTag("mfa_toggle_switch")
                                        )
                                    }
                                }
                            }

                            // Factor 1: Master PIN
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🔑", fontSize = 16.sp)
                                            Text("Factor 1: Master Cultivation PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CelestialGold)
                                        }

                                        OutlinedTextField(
                                            value = masterPinInput,
                                            onValueChange = { if (it.length <= 12) masterPinInput = it },
                                            label = { Text("Enter 4-12 Digit PIN") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("master_pin_input"),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            visualTransformation = PasswordVisualTransformation(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = JadeCyan,
                                                unfocusedBorderColor = TwilightBorder,
                                                focusedTextColor = CloudMistWhite,
                                                unfocusedTextColor = CloudMistWhite
                                            )
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (masterPinInput.length >= 4) {
                                                        mfaManager.setMasterPin(masterPinInput)
                                                        pinMessage = "Master PIN successfully updated & salted!"
                                                        pinMessageColor = SuccessGreen
                                                        masterPinInput = ""
                                                    } else {
                                                        pinMessage = "PIN must be at least 4 digits."
                                                        pinMessageColor = DangerRed
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("save_pin_button"),
                                                colors = ButtonDefaults.buttonColors(containerColor = JadeDark)
                                            ) {
                                                Text("Set PIN", fontSize = 12.sp, color = CloudMistWhite)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val ok = mfaManager.verifyMasterPin(masterPinInput)
                                                    if (ok) {
                                                        pinMessage = "PIN verified successfully!"
                                                        pinMessageColor = SuccessGreen
                                                    } else {
                                                        val locked = mfaManager.isLockedOut
                                                        pinMessage = if (locked) "Account locked out for ${mfaManager.remainingLockoutSeconds}s!" else "Invalid PIN!"
                                                        pinMessageColor = DangerRed
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("verify_pin_button"),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, CelestialGold)
                                            ) {
                                                Text("Verify PIN", fontSize = 12.sp, color = CelestialGold)
                                            }
                                        }

                                        if (pinMessage.isNotEmpty()) {
                                            Text(pinMessage, color = pinMessageColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            // Factor 2: TOTP RFC 6238 Authenticator
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("📲", fontSize = 16.sp)
                                            Text("Factor 2: Time-based OTP (RFC 6238)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JadeCyan)
                                        }

                                        Text(
                                            "Compatible with Google Authenticator, Microsoft Authenticator, and Authy.",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )

                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            color = VoidBlack,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Base32 Secret Key:", fontSize = 10.sp, color = TextMuted)
                                                    Text(
                                                        text = totpSecret,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CelestialGold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("TOTP Secret", totpSecret))
                                                        mfaManager.setTotpSecret(totpSecret)
                                                        Toast.makeText(context, "Secret Key copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "Copy", tint = JadeCyan)
                                                }
                                            }
                                        }

                                        // OTP Verifier Field
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = otpVerifyInput,
                                                onValueChange = { if (it.length <= 6) otpVerifyInput = it },
                                                label = { Text("6-Digit OTP Code") },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("otp_code_input"),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = JadeCyan,
                                                    unfocusedBorderColor = TwilightBorder,
                                                    focusedTextColor = CloudMistWhite,
                                                    unfocusedTextColor = CloudMistWhite
                                                )
                                            )

                                            Button(
                                                onClick = {
                                                    mfaManager.setTotpSecret(totpSecret)
                                                    val ok = mfaManager.verifyTotp(otpVerifyInput)
                                                    if (ok) {
                                                        otpMessage = "6-Digit OTP Code Verified! Token Valid."
                                                        otpMessageColor = SuccessGreen
                                                    } else {
                                                        otpMessage = "Invalid OTP Code. Please sync device time."
                                                        otpMessageColor = DangerRed
                                                    }
                                                },
                                                modifier = Modifier.testTag("verify_otp_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = JadeDark)
                                            ) {
                                                Text("Verify OTP", fontSize = 11.sp, color = CloudMistWhite)
                                            }
                                        }

                                        if (otpMessage.isNotEmpty()) {
                                            Text(otpMessage, color = otpMessageColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            // Factor 3: Emergency Backup Recovery Codes
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🎟️", fontSize = 16.sp)
                                            Text("Emergency Backup Recovery Codes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SpiritPurple)
                                        }

                                        Text(
                                            "Single-use tokens to regain account access if you lose your authenticator device.",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )

                                        Button(
                                            onClick = {
                                                backupCodes = mfaManager.generateBackupCodes()
                                                showBackupCodes = true
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("generate_backup_codes_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = TwilightSurface),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SpiritPurple)
                                        ) {
                                            Text("Generate 8 New Single-Use Codes", fontSize = 12.sp, color = SpiritPurple)
                                        }

                                        if (showBackupCodes && backupCodes.isNotEmpty()) {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                color = VoidBlack,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    backupCodes.chunked(2).forEach { pair ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text("• ${pair[0]}", fontSize = 12.sp, color = CloudMistWhite, fontFamily = FontFamily.Monospace)
                                                            if (pair.size > 1) {
                                                                Text("• ${pair[1]}", fontSize = 12.sp, color = CloudMistWhite, fontFamily = FontFamily.Monospace)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Data Encryption & System Security Posture
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, JadeCyan)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🔐 Cryptographic Defense Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CelestialGold)

                                        SecurityStatusRow("Data Encryption", "AES-256-GCM (128-bit Auth Tag)", SuccessGreen)
                                        SecurityStatusRow("Key Derivation", "PBKDF2WithHmacSHA256 (10,000 Iterations)", SuccessGreen)
                                        SecurityStatusRow("Integrity Protection", "HMAC-SHA256 Anti-Tamper Sealed", SuccessGreen)
                                        SecurityStatusRow("Side-Channel Defense", "Constant-Time Verification Active", SuccessGreen)
                                        SecurityStatusRow("Memory Security", "Zeroization RAM Scrubbing Active", SuccessGreen)
                                        SecurityStatusRow("Brute-Force Guard", "Adaptive Rate Limiter (Max 5 Fails)", SuccessGreen)
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📜 Recent Security Audit Trail", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CloudMistWhite)

                                        val logs = remember { SecurityAuditLogger.getRecentLogs() }
                                        if (logs.isEmpty()) {
                                            Text("No security incidents recorded. System pristine.", fontSize = 11.sp, color = TextMuted)
                                        } else {
                                            logs.takeLast(6).reversed().forEach { log ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("[${log.formattedTime}] ${log.eventType}", fontSize = 11.sp, color = JadeCyan, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        text = log.severityTag,
                                                        fontSize = 10.sp,
                                                        color = if (log.severity == SecurityAuditLogger.SEVERITY_CRITICAL) DangerRed else if (log.severity == SecurityAuditLogger.SEVERITY_WARN) CelestialAmber else SuccessGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(log.details, fontSize = 10.sp, color = TextMuted)
                                                HorizontalDivider(color = TwilightBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Live WAF / SQLi & XSS Input Validator Tester
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TwilightElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CelestialGold)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🧪 Interactive SQLi & XSS Threat Analyzer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CelestialGold)
                                        Text(
                                            "Test user inputs against the real-time Web Application Firewall (WAF) & Input Sanitization Engine.",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )

                                        // Quick Attack Presets
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            PresetButton("SQLi Attack", Modifier.weight(1f)) {
                                                testInputText = "admin' OR 1=1; DROP TABLE disciples; --"
                                            }
                                            PresetButton("XSS Attack", Modifier.weight(1f)) {
                                                testInputText = "<script>alert('Heavenly XSS');</script><img src=x onerror=alert(1)>"
                                            }
                                            PresetButton("Safe Input", Modifier.weight(1f)) {
                                                testInputText = "Celestial Cloud Sword Sect"
                                            }
                                        }

                                        OutlinedTextField(
                                            value = testInputText,
                                            onValueChange = { testInputText = it },
                                            label = { Text("Input String to Validate") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("security_test_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = JadeCyan,
                                                unfocusedBorderColor = TwilightBorder,
                                                focusedTextColor = CloudMistWhite,
                                                unfocusedTextColor = CloudMistWhite
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                validationResult = InputSecurityValidator.validateAndSanitize(
                                                    context,
                                                    testInputText,
                                                    64,
                                                    "InteractiveTester"
                                                )
                                                auditLogs = SecurityAuditLogger.getRecentLogs()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("run_validation_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = JadeDark)
                                        ) {
                                            Text("Inspect & Sanitize Input", fontSize = 12.sp, color = CloudMistWhite)
                                        }

                                        validationResult?.let { res ->
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = VoidBlack,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (res.isValid) SuccessGreen else DangerRed)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("Inspection Status:", fontSize = 11.sp, color = TextMuted)
                                                        Text(
                                                            text = if (res.isValid) "✅ PASSED (Safe Input)" else "🚫 THREAT DETECTED & BLOCKED",
                                                            fontSize = 11.sp,
                                                            color = if (res.isValid) SuccessGreen else DangerRed,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        ThreatBadge("SQLi: " + if (res.sqliDetected) "DETECTED" else "CLEAR", res.sqliDetected)
                                                        ThreatBadge("XSS: " + if (res.xssDetected) "DETECTED" else "CLEAR", res.xssDetected)
                                                    }

                                                    Text("Sanitized Output:", fontSize = 10.sp, color = TextMuted)
                                                    Text(
                                                        text = if (res.sanitizedText.isEmpty()) "[Empty String]" else res.sanitizedText,
                                                        fontSize = 12.sp,
                                                        color = JadeCyan,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityStatusRow(title: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = CloudMistWhite)
        Text(status, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PresetButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
    ) {
        Text(text, fontSize = 10.sp, color = CloudMistWhite)
    }
}

@Composable
private fun ThreatBadge(text: String, isThreat: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isThreat) DangerRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isThreat) DangerRed else SuccessGreen)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = if (isThreat) DangerRed else SuccessGreen,
            fontWeight = FontWeight.Bold
        )
    }
}
