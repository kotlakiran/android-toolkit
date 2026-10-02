package com.kotlakiran.apptemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlakiran.toolkit.aicoach.ChatMessage
import com.kotlakiran.toolkit.aicoach.CoachChat
import com.kotlakiran.toolkit.coreui.components.AppListRow
import com.kotlakiran.toolkit.coreui.components.AppTab
import com.kotlakiran.toolkit.coreui.components.FloatingBottomBar
import com.kotlakiran.toolkit.coreui.components.MeterBar
import com.kotlakiran.toolkit.coreui.components.SectionCard
import com.kotlakiran.toolkit.coreui.components.SegmentedControl
import com.kotlakiran.toolkit.coreui.components.StatTile
import com.kotlakiran.toolkit.coreui.components.Tone
import com.kotlakiran.toolkit.coreui.theme.DarkTokens
import com.kotlakiran.toolkit.coreui.theme.LocalAppTokens
import com.kotlakiran.toolkit.coreui.theme.ProvideAppTokens
import com.kotlakiran.toolkit.money.Emi
import com.kotlakiran.toolkit.money.LoanInput
import com.kotlakiran.toolkit.money.Money

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideAppTokens(DarkTokens) {
                TemplateRoot()
            }
        }
    }
}

private val TABS = listOf(
    AppTab("home", "Home", "⌂"),
    AppTab("coach", "Coach", "✦"),
    AppTab("profile", "Profile", "☺"),
)

@Composable
fun TemplateRoot() {
    val tokens = LocalAppTokens.current
    var tab by rememberSaveable { mutableStateOf("home") }
    Box(Modifier.fillMaxSize().background(tokens.bg)) {
        when (tab) {
            "home" -> HomeScreen()
            "coach" -> CoachScreen()
            else -> ProfileScreen()
        }
        FloatingBottomBar(
            tabs = TABS,
            selectedId = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HomeScreen() {
    val tokens = LocalAppTokens.current
    // Demo loan: the money-core math an app would wire to real user input.
    val loan = LoanInput(principalPaise = 362_000_000, annualRatePct = 8.5, tenureMonths = 190)
    val emi = Emi.monthlyEmiPaise(loan.principalPaise, loan.annualRatePct, loan.tenureMonths)
    val prepay = Emi.prepayByExtraMonthly(loan, 500_000)
    var rateIdx by rememberSaveable { mutableIntStateOf(1) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 48.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Toolkit Template", color = tokens.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Swap this screen for your app's home", color = tokens.muted, fontSize = 12.sp)

        SectionCard(title = "money-core demo") {
            Text(
                Money.paiseToRupees(emi) + " /month",
                color = tokens.ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
            )
            Text(
                "₹5K extra/mo saves ${Money.paiseToRupeesCompact(prepay.interestSavedPaise)} " +
                    "and closes ${prepay.monthsSaved} months early",
                color = tokens.muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp),
            )
            MeterBar(fraction = 0.42f, markFraction = 0.40f, modifier = Modifier.padding(top = 12.dp))
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Total pay", "₹65.9L", modifier = Modifier.weight(1f))
                StatTile("Interest", "₹29.8L", tone = Tone.BAD, modifier = Modifier.weight(1f))
                StatTile("Savings", "₹7.6L", tone = Tone.GOOD, modifier = Modifier.weight(1f))
            }
        }

        SectionCard(title = "core-ui demo") {
            SegmentedControl(
                options = listOf("Fixed", "Floating", "Hybrid"),
                selectedIndex = rateIdx,
                onSelect = { rateIdx = it },
            )
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppListRow(
                    "A reusable list row",
                    "title + subtitle + optional trailing",
                    trailing = { Text("›", color = tokens.dim, fontSize = 16.sp) },
                )
            }
        }
    }
}

@Composable
private fun CoachScreen() {
    // Template runs without Firebase: answers come from a local fallback.
    // Add google-services.json + AiCoach(fallback = ...) for real answers.
    var messages by rememberSaveable { mutableStateOf(listOf<ChatMessage>()) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .padding(top = 48.dp, bottom = 110.dp),
    ) {
        val tokens = LocalAppTokens.current
        Text("Coach", color = tokens.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        CoachChat(
            messages = messages,
            onSend = { q ->
                messages = messages + ChatMessage(fromUser = true, text = q)
                messages = messages + ChatMessage(
                    fromUser = false,
                    text = "Demo reply — wire AiCoach + google-services.json for real AI answers. You asked: \"$q\"",
                    sources = "Source: on-device demo",
                )
            },
            quickChips = listOf("What can this toolkit do?", "Show money math"),
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun ProfileScreen() {
    val tokens = LocalAppTokens.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .padding(top = 48.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", color = tokens.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        SectionCard(title = "Sections") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppListRow("Settings row", "subtitle text")
                AppListRow("Data & privacy", "delete, export, consent")
            }
        }
    }
}
