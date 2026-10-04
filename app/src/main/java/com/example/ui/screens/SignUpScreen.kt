package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import com.example.data.UserStats
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    userStats: UserStats,
    onSignUpComplete: (type: String, username: String, value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTermsDialog by remember { mutableStateOf(false) }

    // Signup sequence steps:
    // 0: Welcome Onboarding
    // 1: Phone number registration
    // 2: OTP verification
    // 3: Profile Setup (Avatar & Gamertag name)
    var currentStep by remember { mutableStateOf(0) }

    // Onboarding values
    var selectedCountryName by remember { mutableStateOf("United States") }
    var selectedCountryCode by remember { mutableStateOf("+1") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var countrySearchQuery by remember { mutableStateOf("") }
    var phoneNoText by remember { mutableStateOf("") }
    
    // OTP verification values
    var otpDigits by remember { mutableStateOf("") }
    var resendTimer by remember { mutableStateOf(45) }
    var canResendOtp by remember { mutableStateOf(false) }
    var expectedOtpCode by remember { mutableStateOf("") }

    // Profile Setup values
    var chosenGamertag by remember { mutableStateOf(userStats.username) }
    val avatarColors = listOf(NeonMint, NeonPurple, CyberPink, AccentOrange)
    var selectedAvatarIndex by remember { mutableStateOf(0) }

    // Async states
    var isVerifying by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Firebase Auth Phone verification state variables
    var verificationId by remember { mutableStateOf("") }
    var forceResendingToken by remember { mutableStateOf<com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) break
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // No hard block, fallback shown in app anyway
    }

    fun triggerVerificationCodeNotification(context: android.content.Context, phoneNumber: String, code: String) {
        val channelId = "sms_verification"
        val channelName = "SMS Verification Codes"
        val notificationId = 98231
        
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for sending local simulated SMS verification codes"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("SMS from $phoneNumber")
            .setContentText("Your WeGist verification code is: $code")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted or SecurityException
        }
    }

    // Countdown Timer for OTP
    LaunchedEffect(currentStep, resendTimer) {
        if (currentStep == 2 && resendTimer > 0) {
            delay(1000)
            resendTimer--
            if (resendTimer == 0) {
                canResendOtp = true
            }
        }
    }

    val startPhoneVerification = {
        if (phoneNoText.isBlank() || phoneNoText.length < 7) {
            feedbackMessage = "Please enter a valid phone number"
        } else {
            isVerifying = true
            feedbackMessage = "Sending verification SMS..."
            val fullPhoneNumber = "$selectedCountryCode$phoneNoText"
            
            // Proactively request POST_NOTIFICATIONS permission for API 33+
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            
            try {
                val authInstance = com.google.firebase.auth.FirebaseAuth.getInstance()
                
                val callbacks = object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                        val code = credential.smsCode
                        if (code != null) {
                            otpDigits = code
                        }
                        authInstance.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    feedbackMessage = "OTP Code successfully verified! ✓"
                                    currentStep = 3
                                } else {
                                    feedbackMessage = "Verification failed: ${task.exception?.message}"
                                }
                            }
                    }

                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                        feedbackMessage = "Verification failed: ${e.message}"
                        isVerifying = false
                    }

                    override fun onCodeSent(
                        verId: String,
                        token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
                    ) {
                        verificationId = verId
                        forceResendingToken = token
                        isVerifying = false
                        feedbackMessage = "Verification code sent to $fullPhoneNumber!"
                        currentStep = 2
                        resendTimer = 45
                        canResendOtp = false
                    }
                }

                val optionsBuilder = com.google.firebase.auth.PhoneAuthOptions.newBuilder(authInstance)
                    .setPhoneNumber(fullPhoneNumber)
                    .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                    .setCallbacks(callbacks)
                    
                if (activity != null) {
                    optionsBuilder.setActivity(activity)
                }
                
                com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
                
            } catch (e: Exception) {
                coroutineScope.launch {
                    val code = (100000..999999).random().toString()
                    expectedOtpCode = code
                    
                    delay(1200)
                    isVerifying = false
                    feedbackMessage = "Verification SMS sent to $fullPhoneNumber!"
                    
                    // Trigger a real local Android system notification
                    triggerVerificationCodeNotification(context, fullPhoneNumber, code)
                    
                    currentStep = 2
                    resendTimer = 45
                    canResendOtp = false
                }
            }
        }
    }

    val verifyOtpCode = {
        if (otpDigits.length < 6) {
            feedbackMessage = "Please enter the complete 6-digit verification code"
        } else {
            isVerifying = true
            feedbackMessage = "Verifying OTP..."
            
            if (verificationId.isNotEmpty()) {
                try {
                    val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, otpDigits)
                    com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            isVerifying = false
                            if (task.isSuccessful) {
                                feedbackMessage = "OTP Code successfully verified! ✓"
                                coroutineScope.launch {
                                    delay(600)
                                    feedbackMessage = ""
                                    currentStep = 3
                                }
                            } else {
                                feedbackMessage = "Incorrect OTP code. Please try again."
                            }
                        }
                } catch (e: Exception) {
                    isVerifying = false
                    feedbackMessage = "Error: ${e.message}"
                }
            } else {
                coroutineScope.launch {
                    delay(1200)
                    isVerifying = false
                    if (otpDigits == expectedOtpCode) {
                        feedbackMessage = "OTP Code successfully verified! ✓"
                        delay(600)
                        feedbackMessage = ""
                        currentStep = 3
                    } else {
                        feedbackMessage = "Incorrect code. Please enter the simulated code sent: $expectedOtpCode"
                    }
                }
            }
        }
    }

    val countries = listOf(
        Pair("Afghanistan", "+93"),
        Pair("Albania", "+355"),
        Pair("Algeria", "+213"),
        Pair("Andorra", "+376"),
        Pair("Angola", "+244"),
        Pair("Argentina", "+54"),
        Pair("Armenia", "+374"),
        Pair("Australia", "+61"),
        Pair("Austria", "+43"),
        Pair("Azerbaijan", "+994"),
        Pair("Bahamas", "+1-242"),
        Pair("Bahrain", "+973"),
        Pair("Bangladesh", "+880"),
        Pair("Barbados", "+1-246"),
        Pair("Belarus", "+375"),
        Pair("Belgium", "+32"),
        Pair("Belize", "+501"),
        Pair("Benin", "+229"),
        Pair("Bhutan", "+975"),
        Pair("Bolivia", "+591"),
        Pair("Bosnia and Herzegovina", "+387"),
        Pair("Botswana", "+267"),
        Pair("Brazil", "+55"),
        Pair("Brunei", "+673"),
        Pair("Bulgaria", "+359"),
        Pair("Burkina Faso", "+226"),
        Pair("Burundi", "+257"),
        Pair("Cambodia", "+855"),
        Pair("Cameroon", "+237"),
        Pair("Canada", "+1"),
        Pair("Cape Verde", "+238"),
        Pair("Central African Republic", "+236"),
        Pair("Chad", "+235"),
        Pair("Chile", "+56"),
        Pair("China", "+86"),
        Pair("Colombia", "+57"),
        Pair("Comoros", "+269"),
        Pair("Congo", "+242"),
        Pair("Costa Rica", "+506"),
        Pair("Croatia", "+385"),
        Pair("Cuba", "+53"),
        Pair("Cyprus", "+357"),
        Pair("Czech Republic", "+420"),
        Pair("Denmark", "+45"),
        Pair("Djibouti", "+253"),
        Pair("Dominica", "+1-767"),
        Pair("Dominican Republic", "+1-809"),
        Pair("Ecuador", "+593"),
        Pair("Egypt", "+20"),
        Pair("El Salvador", "+503"),
        Pair("Equatorial Guinea", "+240"),
        Pair("Eritrea", "+291"),
        Pair("Estonia", "+372"),
        Pair("Eswatini", "+268"),
        Pair("Ethiopia", "+251"),
        Pair("Fiji", "+679"),
        Pair("Finland", "+358"),
        Pair("France", "+33"),
        Pair("Gabon", "+241"),
        Pair("Gambia", "+220"),
        Pair("Georgia", "+995"),
        Pair("Germany", "+49"),
        Pair("Ghana", "+233"),
        Pair("Greece", "+30"),
        Pair("Grenada", "+1-473"),
        Pair("Guatemala", "+502"),
        Pair("Guinea", "+224"),
        Pair("Guyana", "+592"),
        Pair("Haiti", "+509"),
        Pair("Honduras", "+504"),
        Pair("Hungary", "+36"),
        Pair("Iceland", "+354"),
        Pair("India", "+91"),
        Pair("Indonesia", "+62"),
        Pair("Iran", "+98"),
        Pair("Iraq", "+964"),
        Pair("Ireland", "+353"),
        Pair("Israel", "+972"),
        Pair("Italy", "+39"),
        Pair("Jamaica", "+1-876"),
        Pair("Japan", "+81"),
        Pair("Jordan", "+962"),
        Pair("Kazakhstan", "+7"),
        Pair("Kenya", "+254"),
        Pair("Kuwait", "+965"),
        Pair("Kyrgyzstan", "+996"),
        Pair("Laos", "+856"),
        Pair("Latvia", "+371"),
        Pair("Lebanon", "+961"),
        Pair("Lesotho", "+266"),
        Pair("Liberia", "+231"),
        Pair("Libya", "+218"),
        Pair("Liechtenstein", "+423"),
        Pair("Lithuania", "+370"),
        Pair("Luxembourg", "+352"),
        Pair("Madagascar", "+261"),
        Pair("Malawi", "+265"),
        Pair("Malaysia", "+60"),
        Pair("Maldives", "+960"),
        Pair("Mali", "+223"),
        Pair("Malta", "+356"),
        Pair("Mauritania", "+222"),
        Pair("Mauritius", "+230"),
        Pair("Mexico", "+52"),
        Pair("Moldova", "+373"),
        Pair("Monaco", "+377"),
        Pair("Mongolia", "+976"),
        Pair("Montenegro", "+382"),
        Pair("Morocco", "+212"),
        Pair("Mozambique", "+258"),
        Pair("Myanmar", "+95"),
        Pair("Namibia", "+264"),
        Pair("Nepal", "+977"),
        Pair("Netherlands", "+31"),
        Pair("New Zealand", "+64"),
        Pair("Nicaragua", "+505"),
        Pair("Niger", "+227"),
        Pair("Nigeria", "+234"),
        Pair("North Korea", "+850"),
        Pair("North Macedonia", "+389"),
        Pair("Norway", "+47"),
        Pair("Oman", "+968"),
        Pair("Pakistan", "+92"),
        Pair("Panama", "+507"),
        Pair("Papua New Guinea", "+675"),
        Pair("Paraguay", "+595"),
        Pair("Peru", "+51"),
        Pair("Philippines", "+63"),
        Pair("Poland", "+48"),
        Pair("Portugal", "+351"),
        Pair("Qatar", "+974"),
        Pair("Romania", "+40"),
        Pair("Russia", "+7"),
        Pair("Rwanda", "+250"),
        Pair("Saudi Arabia", "+966"),
        Pair("Senegal", "+221"),
        Pair("Serbia", "+381"),
        Pair("Seychelles", "+248"),
        Pair("Sierra Leone", "+232"),
        Pair("Singapore", "+65"),
        Pair("Slovakia", "+421"),
        Pair("Slovenia", "+386"),
        Pair("Somalia", "+252"),
        Pair("South Africa", "+27"),
        Pair("South Korea", "+82"),
        Pair("South Sudan", "+211"),
        Pair("Spain", "+34"),
        Pair("Sri Lanka", "+94"),
        Pair("Sudan", "+249"),
        Pair("Suriname", "+597"),
        Pair("Sweden", "+46"),
        Pair("Switzerland", "+41"),
        Pair("Syria", "+963"),
        Pair("Taiwan", "+886"),
        Pair("Tajikistan", "+992"),
        Pair("Tanzania", "+255"),
        Pair("Thailand", "+66"),
        Pair("Togo", "+228"),
        Pair("Trinidad and Tobago", "+1-868"),
        Pair("Tunisia", "+216"),
        Pair("Turkey", "+90"),
        Pair("Turkmenistan", "+993"),
        Pair("Uganda", "+256"),
        Pair("Ukraine", "+380"),
        Pair("United Arab Emirates", "+971"),
        Pair("United Kingdom", "+44"),
        Pair("United States", "+1"),
        Pair("Uruguay", "+598"),
        Pair("Uzbekistan", "+998"),
        Pair("Venezuela", "+58"),
        Pair("Vietnam", "+84"),
        Pair("Yemen", "+967"),
        Pair("Zambia", "+260"),
        Pair("Zimbabwe", "+263")
    )

    val filteredCountries = countries.filter {
        it.first.contains(countrySearchQuery, ignoreCase = true) ||
                it.second.contains(countrySearchQuery)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberSlateDark)
    ) {
        // Aesthetic ambient glow background circles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.08f), Color.Transparent),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // --- STEP HEADER PANEL ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep in 1..2) {
                    IconButton(
                        onClick = {
                            feedbackMessage = ""
                            currentStep--
                        },
                        modifier = Modifier.testTag("signup_back_step_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Step",
                            tint = TextWhiteDark
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Text(
                    text = when (currentStep) {
                        0 -> "Welcome to WeGist"
                        1 -> "Enter Phone Number"
                        2 -> "Verify OTP Code"
                        else -> "Setup Profile"
                    },
                    color = TextWhiteDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )

                // Dots step indicators
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..3).forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (index == currentStep) NeonMint else CyberBorderDark)
                        )
                    }
                }
            }

            // --- MAIN CARD CONTENT (SMOOTH TRANSITIONS) ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Invisible focus controller for OTP
                val otpFocusRequester = remember { FocusRequester() }

                // Dialog-based searchable country picker for high accessibility & WhatsApp feel
                if (isCountryDropdownExpanded) {
                    AlertDialog(
                        onDismissRequest = { 
                            isCountryDropdownExpanded = false
                            countrySearchQuery = ""
                        },
                        containerColor = CyberCardDark,
                        shape = RoundedCornerShape(16.dp),
                        title = {
                            Text(
                                "Select Country",
                                color = TextWhiteDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                                OutlinedTextField(
                                    value = countrySearchQuery,
                                    onValueChange = { countrySearchQuery = it },
                                    placeholder = { Text("Search country...", color = TextGrayDark.copy(alpha = 0.5f), fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGrayDark) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonMint,
                                        unfocusedBorderColor = CyberBorderDark,
                                        focusedTextColor = TextWhiteDark,
                                        unfocusedTextColor = TextWhiteDark
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("country_search_input")
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (filteredCountries.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No countries found", color = TextGrayDark, fontSize = 14.sp)
                                            }
                                        }
                                    } else {
                                        items(filteredCountries) { (name, code) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable(
                                                        onClickLabel = "Select $name",
                                                        onClick = {
                                                            selectedCountryName = name
                                                            selectedCountryCode = code
                                                            isCountryDropdownExpanded = false
                                                            countrySearchQuery = ""
                                                        }
                                                    )
                                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = name,
                                                    color = TextWhiteDark,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = code,
                                                    color = NeonMint,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            HorizontalDivider(color = CyberBorderDark.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    isCountryDropdownExpanded = false
                                    countrySearchQuery = ""
                                }
                            ) {
                                Text("Cancel", color = NeonMint)
                            }
                        }
                    )
                }

                // Autocomplete autofocus on OTP step
                LaunchedEffect(currentStep) {
                    if (currentStep == 2) {
                        try {
                            otpFocusRequester.requestFocus()
                        } catch (e: Exception) {
                            // Safe fallback
                        }
                    }
                }

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith 
                            (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith 
                            (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "signup_steps_transition"
                ) { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        when (step) {
                            0 -> {
                                // STEP 0: Welcome Landing screen
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .background(NeonMint.copy(alpha = 0.12f), CircleShape)
                                            .border(2.5.dp, NeonMint, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Forum,
                                            contentDescription = "WeGist Logo",
                                            tint = NeonMint,
                                            modifier = Modifier.size(50.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Text(
                                        text = "Welcome to WeGist",
                                        color = TextWhiteDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 26.sp,
                                        letterSpacing = (-0.5).sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Simple, reliable messaging combined with live stream views and interactive mini-games — local-first, with your data stored on your device.",
                                        color = TextGrayDark,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.clickable { showTermsDialog = true }.testTag("view_terms_link")
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = "Security", tint = NeonMint, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tap \"Agree and continue\" to accept Terms of Service & Privacy Policy",
                                            color = TextGrayDark,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // STEP 1: Phone number registration
                                Text(
                                    text = "WeGist will send an SMS verification code (simulated locally) to verify your phone number.",
                                    color = TextGrayDark,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Country selector dropdown (WhatsApp Style)
                                OutlinedButton(
                                    onClick = { isCountryDropdownExpanded = true },
                                    border = BorderStroke(1.dp, CyberBorderDark),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhiteDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("country_select_dropdown_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedCountryName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Open Countries List", tint = TextGrayDark)
                                    }
                                }

                                // Phone number text inputs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Static Country code box
                                    Box(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(56.dp)
                                            .border(1.dp, CyberBorderDark, RoundedCornerShape(12.dp))
                                            .background(CyberCardDark, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = selectedCountryCode,
                                            color = NeonMint,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    // Phone entry text field
                                    OutlinedTextField(
                                        value = phoneNoText,
                                        onValueChange = { phoneNoText = it.filter { char -> char.isDigit() || char == ' ' || char == '-' } },
                                        placeholder = { Text("Phone number", color = TextGrayDark.copy(alpha = 0.5f)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhiteDark,
                                            unfocusedTextColor = TextWhiteDark,
                                            focusedBorderColor = NeonMint,
                                            unfocusedBorderColor = CyberBorderDark
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .testTag("phone_registration_input")
                                    )
                                }

                                Text(
                                    text = "Carrier SMS charges may apply",
                                    color = TextGrayDark.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                if (feedbackMessage.isNotEmpty()) {
                                    Text(
                                        text = feedbackMessage,
                                        color = CyberPink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            2 -> {
                                // STEP 2: OTP SMS Code verification
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Verifying your phone number",
                                        color = NeonMint,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    
                                    Text(
                                        text = "Waiting to automatically detect an SMS sent to $selectedCountryCode $phoneNoText. Wrong number?",
                                        color = TextGrayDark,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    
                                    Text(
                                        text = "Wrong number? Tap to change",
                                        color = NeonPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .clickable {
                                                feedbackMessage = ""
                                                otpDigits = ""
                                                currentStep = 1
                                            }
                                            .testTag("wrong_number_btn")
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Hidden field that captures real Android soft keyboard inputs
                                BasicTextField(
                                    value = otpDigits,
                                    onValueChange = { newValue ->
                                        if (newValue.length <= 6) {
                                            otpDigits = newValue.filter { it.isDigit() }
                                            if (otpDigits.length == 6) {
                                                verifyOtpCode()
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .size(1.dp)
                                        .focusRequester(otpFocusRequester),
                                    cursorBrush = SolidColor(Color.Transparent),
                                    textStyle = TextStyle(color = Color.Transparent)
                                )

                                // Custom OTP digit boxes (tapping any focuses the hidden textfield)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { otpFocusRequester.requestFocus() }
                                ) {
                                    (0..5).forEach { index ->
                                        val digitChar = otpDigits.getOrNull(index)?.toString() ?: ""
                                        val isCurrent = index == otpDigits.length

                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .border(
                                                    BorderStroke(
                                                        1.5.dp,
                                                        if (isCurrent) NeonMint else CyberBorderDark
                                                    ),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .background(CyberCardDark, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = digitChar,
                                                color = NeonMint,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Numeric Keyboard Simulator (Optional fallback in view)
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val keys = listOf(
                                        listOf("1", "2", "3"),
                                        listOf("4", "5", "6"),
                                        listOf("7", "8", "9"),
                                        listOf("⌫", "0", "✓")
                                    )
                                    keys.forEach { rowKeys ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            rowKeys.forEach { key ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp, 44.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(CyberBorderDark)
                                                        .clickable {
                                                            when (key) {
                                                                "⌫" -> {
                                                                    if (otpDigits.isNotEmpty()) {
                                                                        otpDigits = otpDigits.dropLast(1)
                                                                    }
                                                                }
                                                                "✓" -> {
                                                                    if (otpDigits.length == 6) {
                                                                        verifyOtpCode()
                                                                    }
                                                                }
                                                                else -> {
                                                                    if (otpDigits.length < 6) {
                                                                        otpDigits += key
                                                                        if (otpDigits.length == 6) {
                                                                            verifyOtpCode()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        .testTag("otp_key_$key"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = key,
                                                        color = TextWhiteDark,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Timer countdown text
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = "OTP Timer", tint = TextGrayDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (!canResendOtp) {
                                        Text("Resend SMS in ${resendTimer}s", color = TextGrayDark, fontSize = 12.sp)
                                    } else {
                                        Text(
                                            text = "Resend SMS verification code",
                                            color = NeonPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .clickable {
                                                    otpDigits = ""
                                                    resendTimer = 45
                                                    canResendOtp = false
                                                    coroutineScope.launch {
                                                        val newCode = (100000..999999).random().toString()
                                                        expectedOtpCode = newCode
                                                        val fullPhoneNumber = "$selectedCountryCode$phoneNoText"
                                                        triggerVerificationCodeNotification(context, fullPhoneNumber, newCode)
                                                        feedbackMessage = "Sent a new SMS OTP code!"
                                                        delay(3000)
                                                        if (feedbackMessage == "Sent a new SMS OTP code!") {
                                                            feedbackMessage = ""
                                                        }
                                                    }
                                                }
                                                .testTag("resend_otp_btn")
                                        )
                                    }
                                }

                                if (feedbackMessage.isNotEmpty()) {
                                    Text(
                                        text = feedbackMessage,
                                        color = if (feedbackMessage.contains("success", ignoreCase = true)) NeonMint else CyberPink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            3 -> {
                                // STEP 3: Setup profile photo and gamer username (WhatsApp profile info style)
                                Text(
                                    text = "Please provide your name and an optional profile photo.",
                                    color = TextGrayDark,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // WhatsApp profile circle photo selector
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clickable {
                                            selectedAvatarIndex = (selectedAvatarIndex + 1) % avatarColors.size
                                        }
                                        .testTag("avatar_setup_container"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Custom avatar background circle
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(avatarColors[selectedAvatarIndex], CircleShape)
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (chosenGamertag.isNotBlank()) chosenGamertag.take(2).uppercase() else "WG",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 32.sp
                                        )
                                    }
                                    
                                    // WhatsApp green camera icon badge
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(NeonMint, CircleShape)
                                            .border(1.5.dp, CyberCardDark, CircleShape)
                                            .align(Alignment.BottomEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Edit Profile Picture",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Username Input field with Character Counter (WhatsApp style)
                                OutlinedTextField(
                                    value = chosenGamertag,
                                    onValueChange = { 
                                        if (it.length <= 25) {
                                            chosenGamertag = it
                                        }
                                    },
                                    placeholder = { Text("Type your name here...", color = TextGrayDark.copy(alpha = 0.5f)) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = NeonMint) },
                                    trailingIcon = {
                                        Text(
                                            text = (25 - chosenGamertag.length).toString(),
                                            color = if (chosenGamertag.length > 20) CyberPink else TextGrayDark,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhiteDark,
                                        unfocusedTextColor = TextWhiteDark,
                                        focusedBorderColor = NeonMint,
                                        unfocusedBorderColor = CyberBorderDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chosen_gamertag_input")
                                )

                                if (feedbackMessage.isNotEmpty()) {
                                    Text(
                                        text = feedbackMessage,
                                        color = CyberPink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- PRIMARY NAVIGATION BUTTON ---
            Button(
                onClick = {
                    feedbackMessage = ""
                    when (currentStep) {
                        0 -> {
                            currentStep = 1
                        }
                        1 -> {
                            startPhoneVerification()
                        }
                        2 -> {
                            verifyOtpCode()
                        }
                        3 -> {
                            if (chosenGamertag.isBlank()) {
                                feedbackMessage = "Please choose a gamertag nickname"
                            } else {
                                isVerifying = true
                                coroutineScope.launch {
                                    delay(1200)
                                    isVerifying = false
                                    // Trigger complete callback with phone and username
                                    onSignUpComplete("PHONE", chosenGamertag, "$selectedCountryCode $phoneNoText")
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonMint,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                enabled = !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_primary_btn")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    val btnText = when (currentStep) {
                        0 -> "AGREE AND CONTINUE"
                        1, 2 -> "NEXT"
                        else -> "INITIALIZE PROFILE 🎮"
                    }
                    Text(btnText, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        }
    }

    // --- PLAY STORE COMPLIANCE: TERMS OF SERVICE & PRIVACY POLICY DIALOG ---
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = CyberCardDark,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NeonMint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terms of Service & Privacy Policy", color = TextWhiteDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text("Safety & Data Policy", color = NeonMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    item {
                        Text(
                            "1. Data Storage & Security:\nWeGist is a local-first demo: your messages, profile and game data are stored in a local database on your device and are never uploaded. Phone sign-in uses Firebase Authentication when configured; otherwise a local demo verification is used. Messages are not yet end-to-end encrypted — E2EE is planned for the online release.",
                            color = TextGrayDark,
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Text(
                            "2. Community Safety & Moderation:\nWeGist strictly prohibits hate speech, illegal content, harassment, and unauthorized commercial spam. Violators will face immediate account termination.",
                            color = TextGrayDark,
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Text(
                            "3. User Data Deletion Rights (GDPR/Play Policy):\nUsers have full control over their data. You can request complete account and data deletion at any time in Profile Settings.",
                            color = TextGrayDark,
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Text(
                            "4. Age Requirement:\nYou must be at least 13 years old (or the legal minimum age in your jurisdiction) to use WeGist services.",
                            color = TextGrayDark,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("accept_terms_btn")
                ) {
                    Text("I AGREE & ACCEPT", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
