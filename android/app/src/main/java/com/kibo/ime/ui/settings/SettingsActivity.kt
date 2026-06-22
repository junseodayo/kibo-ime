package com.kibo.ime.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kibo.ime.ui.theme.KiboTheme

enum class Route { HOME, LANGUAGE_ORDER, ASSIGN_KEY, APP_LANGUAGE, CLIPBOARD, USER_DICT, THEME }

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: SettingsViewModel = viewModel()
            val settings by vm.settings.collectAsState()
            KiboTheme(accentOverride = settings.accentColor?.let { Color(it) }) {
                SettingsNav(vm)
            }
        }
    }
}

@Composable
private fun SettingsNav(vm: SettingsViewModel) {
    val backStack = remember { mutableStateListOf(Route.HOME) }
    val current = backStack.last()
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    val navTo: (Route) -> Unit = { backStack.add(it) }

    Column(Modifier.fillMaxSize().background(KiboTheme.colors.bg)) {
        when (current) {
            Route.HOME -> HomeScreen(vm, navTo)
            Route.LANGUAGE_ORDER -> LanguageOrderScreen(vm, goBack)
            Route.ASSIGN_KEY -> AssignKeyScreen(vm, goBack)
            Route.APP_LANGUAGE -> AppLanguageScreen(vm, goBack)
            Route.CLIPBOARD -> ClipboardManageScreen(vm, goBack)
            Route.USER_DICT -> UserDictScreen(vm, goBack)
            Route.THEME -> ThemeScreen(vm, goBack)
        }
    }
}

@Composable
private fun HomeScreen(vm: SettingsViewModel, navTo: (Route) -> Unit) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Kibo 키보 설정", onBack = null)

        // Enable-IME shortcut: most useful first action after install.
        SectionTitle("입력기")
        KiboCard {
            NavRow("시스템 키보드 설정 열기", subtitle = "Kibo를 활성화하고 기본 입력기로 지정") {
                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
            RowDivider()
            NavRow("입력기 선택", subtitle = "현재 입력기를 Kibo로 전환") {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showInputMethodPicker()
            }
        }

        SectionTitle("언어 (§2)")
        KiboCard {
            NavRow("언어 순서", subtitle = settings.languageOrder.joinToString(" → ") { it.label }) {
                navTo(Route.LANGUAGE_ORDER)
            }
            RowDivider()
            NavRow("언어 전환키", subtitle = settings.switchKey.describe()) { navTo(Route.ASSIGN_KEY) }
            RowDivider()
            NavRow(
                "앱별 기본 언어",
                subtitle = if (settings.appLangMasterOn) "켜짐 · 매핑 ${settings.appLangMappings.size}개" else "꺼짐",
            ) { navTo(Route.APP_LANGUAGE) }
        }

        SectionTitle("편의 (§7, §8)")
        KiboCard {
            NavRow("클립보드", subtitle = "복사 히스토리 · 텍스트 프리셋") { navTo(Route.CLIPBOARD) }
            RowDivider()
            NavRow("사용자 사전", subtitle = "한국어 · English · 日本語", trailing = "${settings.userDict.size}") {
                navTo(Route.USER_DICT)
            }
        }

        SectionTitle("그 외 (§13)")
        KiboCard {
            NavRow("테마 · 색상", subtitle = "툴바 · 온스크린 키보드 강조색") { navTo(Route.THEME) }
        }
        Spacer(Modifier.height(24.dp))
    }
}
