package com.example.a207354_wanmouwei_cikgulzwan_lab05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.a207354_wanmouwei_cikgulzwan_lab05.ui.theme.A207354_WANMOUWEI_Cikgulzwan_Lab05Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            A207354_WANMOUWEI_Cikgulzwan_Lab05Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoodFlowApp()
                }
            }
        }
    }
}

class MoodViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    var moodText by mutableStateOf("")
    var noteText by mutableStateOf("")
    var resultMessage by mutableStateOf("")

    var selectedMood by mutableStateOf<MoodEntity?>(null)

    var editMoodText by mutableStateOf("")
    var editNoteText by mutableStateOf("")

    val savedMoods = repository.allMoods.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addMood() {
        if (moodText.isBlank()) {
            resultMessage = "Please enter your mood first."
            return
        }

        viewModelScope.launch {
            repository.insert(
                MoodEntity(
                    mood = moodText,
                    note = if (noteText.isBlank()) "No extra note" else noteText
                )
            )

            resultMessage = "Mood saved successfully to Room Database."
            moodText = ""
            noteText = ""
        }
    }

    fun selectMood(mood: MoodEntity) {
        selectedMood = mood
    }

    fun prepareEdit(mood: MoodEntity) {
        selectedMood = mood
        editMoodText = mood.mood
        editNoteText = mood.note
    }

    fun updateMood() {
        val current = selectedMood ?: return

        viewModelScope.launch {
            repository.update(
                current.copy(
                    mood = editMoodText,
                    note = editNoteText
                )
            )

            resultMessage = "Mood updated successfully."
        }
    }

    fun deleteMood(moodId: Int) {
        viewModelScope.launch {
            repository.delete(moodId)
        }
    }
}

class MoodViewModelFactory(
    private val repository: MoodRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MoodViewModel(repository) as T
    }
}

@Composable
fun MoodFlowApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = MoodDatabase.getDatabase(context)
    val repository = MoodRepository(database.moodDao())

    val moodViewModel: MoodViewModel = viewModel(
        factory = MoodViewModelFactory(repository)
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen()
                }

                composable("add") {
                    AddMoodScreen(
                        moodViewModel = moodViewModel,
                        onSaveClick = {
                            moodViewModel.addMood()
                            navController.navigate("result")
                        }
                    )
                }

                composable("result") {
                    ResultScreen(
                        message = moodViewModel.resultMessage,
                        onHistoryClick = {
                            navController.navigate("history")
                        }
                    )
                }

                composable("list") {
                    MoodListScreen(
                        moodViewModel = moodViewModel,
                        onDetailsClick = {
                            navController.navigate("details")
                        },
                        onEditClick = {
                            navController.navigate("edit")
                        }
                    )
                }

                composable("details") {
                    MoodDetailsScreen(moodViewModel)
                }

                composable("history") {
                    HistoryScreen(moodViewModel)
                }

                composable("edit") {
                    EditMoodScreen(
                        moodViewModel = moodViewModel,
                        onUpdateClick = {
                            moodViewModel.updateMood()
                            navController.navigate("history")
                        }
                    )
                }

                composable("community") {
                    CommunityScreen()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        BottomNavigationBar(
            selectedRoute = currentRoute,
            navController = navController
        )
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        TopHeader()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SDG 3: Good Health & Well-being",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        InfoCard(
            title = "Problem",
            description = "Many students feel stressed, but they do not record or track their emotions."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Solution",
            description = "MoodFlow helps students add moods, save them permanently, and review mood history."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Impact",
            description = "This app supports mental health awareness through personal mood tracking."
        )
    }
}

@Composable
fun AddMoodScreen(
    moodViewModel: MoodViewModel,
    onSaveClick: () -> Unit
) {
    Column {
        Text(
            text = "Add Mood",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MoodChip("Happy") { moodViewModel.moodText = "Happy" }
            MoodChip("Stressed") { moodViewModel.moodText = "Stressed" }
            MoodChip("Tired") { moodViewModel.moodText = "Tired" }
            MoodChip("Calm") { moodViewModel.moodText = "Calm" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = moodViewModel.moodText,
            onValueChange = { moodViewModel.moodText = it },
            placeholder = "Enter your mood"
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = moodViewModel.noteText,
            onValueChange = { moodViewModel.noteText = it },
            placeholder = "Write a short note"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Save to Room",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ResultScreen(
    message: String,
    onHistoryClick: () -> Unit
) {
    Column {
        Text(
            text = "Result",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "Status",
            description = message
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onHistoryClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "View History",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun MoodListScreen(
    moodViewModel: MoodViewModel,
    onDetailsClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val moods by moodViewModel.savedMoods.collectAsState()

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Mood List",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (moods.isEmpty()) {
            InfoCard(
                title = "No Mood",
                description = "Please add your first mood record."
            )
        } else {
            moods.forEach { mood ->
                MoodItemCard(
                    mood = mood,
                    onClick = {
                        moodViewModel.selectMood(mood)
                        onDetailsClick()
                    },
                    onEdit = {
                        moodViewModel.prepareEdit(mood)
                        onEditClick()
                    },
                    onDelete = {
                        moodViewModel.deleteMood(mood.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun HistoryScreen(
    moodViewModel: MoodViewModel
) {
    val moods by moodViewModel.savedMoods.collectAsState()

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "History",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "Local Room History",
            description = "These records are saved permanently using Room Database."
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (moods.isEmpty()) {
            InfoCard(
                title = "No Local Data",
                description = "Add a mood first. Then close and reopen the app to test persistence."
            )
        } else {
            moods.forEach { mood ->
                MoodSimpleCard(mood)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun MoodDetailsScreen(
    moodViewModel: MoodViewModel
) {
    val mood = moodViewModel.selectedMood

    Column {
        Text(
            text = "Mood Details",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mood == null) {
            InfoCard(
                title = "No Mood Selected",
                description = "Please select a mood from the list."
            )
        } else {
            InfoCard(
                title = mood.mood,
                description = mood.note
            )
        }
    }
}

@Composable
fun EditMoodScreen(
    moodViewModel: MoodViewModel,
    onUpdateClick: () -> Unit
) {
    Column {
        Text(
            text = "Edit Mood",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = moodViewModel.editMoodText,
            onValueChange = { moodViewModel.editMoodText = it },
            placeholder = "Edit mood"
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = moodViewModel.editNoteText,
            onValueChange = { moodViewModel.editNoteText = it },
            placeholder = "Edit note"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onUpdateClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Update Mood",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun CommunityScreen() {
    Column {
        Text(
            text = "Community",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "Cloud / Firestore",
            description = "This screen is prepared for Firestore cloud backup and community mood sharing."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Community Example",
            description = "Happy - I feel better after recording my emotion today."
        )
    }
}

@Composable
fun TopHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "M",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "MoodFlow",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MoodChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MoodItemCard(
    mood: MoodEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = mood.mood,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onClick() }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = mood.note,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Details",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onClick() }
                )

                Text(
                    text = "Edit",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onEdit() }
                )

                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
fun MoodSimpleCard(mood: MoodEntity) {
    InfoCard(
        title = mood.mood,
        description = mood.note
    )
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomText("Home", selectedRoute == "home") {
            navController.navigate("home") { launchSingleTop = true }
        }

        BottomText("Add", selectedRoute == "add") {
            navController.navigate("add") { launchSingleTop = true }
        }

        BottomText("List", selectedRoute == "list") {
            navController.navigate("list") { launchSingleTop = true }
        }

        BottomText("History", selectedRoute == "history") {
            navController.navigate("history") { launchSingleTop = true }
        }

        BottomText("Cloud", selectedRoute == "community") {
            navController.navigate("community") { launchSingleTop = true }
        }
    }
}

@Composable
fun BottomText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() },
        style = MaterialTheme.typography.bodySmall
    )
}