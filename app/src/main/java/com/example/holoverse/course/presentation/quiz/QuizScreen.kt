package com.example.holoverse.course.presentation.quiz

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.course.domain.Quiz
import com.example.holoverse.course.domain.Question
import com.example.holoverse.course.domain.QuizResult
import com.example.holoverse.core.ui.spatial.Brush
import com.example.holoverse.core.ui.theme.HoloCyan
import com.example.holoverse.core.ui.theme.HoloPurple
import com.example.holoverse.core.ui.theme.IbarraNovaFont
import com.example.holoverse.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: CourseRepo,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _quizState = mutableStateOf<Response<Quiz?>>(Response.Loading)
    val quizState: State<Response<Quiz?>> = _quizState

    private val _timeLeft = mutableIntStateOf(0)
    val timeLeft: State<Int> = _timeLeft

    private val _userAnswers = mutableStateMapOf<Int, Set<Int>>() // Question Index -> Set of selected Option Indices
    val userAnswers: Map<Int, Set<Int>> = _userAnswers

    private val _submittedQuestions = mutableStateListOf<Int>()
    val submittedQuestions: List<Int> = _submittedQuestions

    private val _scoreState = mutableStateOf<Int?>(null)
    val scoreState: State<Int?> = _scoreState

    private var timerJob: Job? = null
    private var currentCourseId: String = ""

    fun loadQuiz(courseId: String, quizId: String) {
        currentCourseId = courseId
        viewModelScope.launch {
            repository.getCourseById(courseId).collectLatest { response ->
                if (response is Response.Success) {
                    val quiz = response.data?.quizzes?.find { it.id == quizId }
                    if (quiz != null) {
                        _quizState.value = Response.Success(quiz)
                        startTimer(quiz.timeLimitMinutes)
                    } else {
                        _quizState.value = Response.Error("Quiz not found")
                    }
                } else if (response is Response.Error) {
                    _quizState.value = Response.Error(response.message)
                }
            }
        }
    }

    private fun startTimer(minutes: Int) {
        _timeLeft.intValue = minutes * 60
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.intValue > 0) {
                delay(1000)
                _timeLeft.intValue -= 1
            }
            submitAll()
        }
    }

    fun onOptionToggle(questionIndex: Int, optionIndex: Int) {
        if (questionIndex in _submittedQuestions) return
        
        val current = _userAnswers[questionIndex] ?: emptySet()
        val next = if (current.contains(optionIndex)) current - optionIndex else current + optionIndex
        _userAnswers[questionIndex] = next
    }

    fun submitQuestion(questionIndex: Int) {
        if (questionIndex !in _submittedQuestions) {
            _submittedQuestions.add(questionIndex)
        }
    }

    fun submitAll() {
        val quiz = (quizState.value as? Response.Success)?.data ?: return
        if (_scoreState.value != null) return // Already submitted

        timerJob?.cancel()

        var totalCorrect = 0
        quiz.questions.forEachIndexed { index, question ->
            val userSelected = _userAnswers[index] ?: emptySet()
            val correctOnes = question.correctOptionIndices.toSet()
            if (userSelected == correctOnes) {
                totalCorrect++
            }
        }

        val score = if (quiz.questions.isNotEmpty()) (totalCorrect * 100) / quiz.questions.size else 0
        _scoreState.value = score

        saveResult(quiz.id, score)
    }

    private fun saveResult(quizId: String, score: Int) {
        val userId = authRepository.getCachedUser()?.userId ?: return
        val result = QuizResult(
            id = UUID.randomUUID().toString(),
            quizId = quizId,
            userId = userId,
            courseId = currentCourseId,
            score = score
        )
        viewModelScope.launch {
            repository.saveQuizResult(result).collectLatest { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    quizId: String,
    onBackClick: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
    darkTheme: Boolean = true
) {
    val context = LocalContext.current
    val quizState by viewModel.quizState
    val timeLeft by viewModel.timeLeft
    val userAnswers = viewModel.userAnswers
    val submittedQuestions = viewModel.submittedQuestions
    val scoreState by viewModel.scoreState

    LaunchedEffect(quizId) {
        viewModel.loadQuiz(courseId, quizId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        (quizState as? Response.Success)?.data?.title ?: "Quiz",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = IbarraNovaFont)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = HoloCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTime(timeLeft),
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 60) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (quizState) {
                is Response.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = HoloPurple)
                }
                is Response.Success -> {
                    val quiz = (quizState as Response.Success<Quiz?>).data
                    if (quiz != null) {
                        QuizContent(
                            quiz = quiz,
                            userAnswers = userAnswers,
                            submittedQuestions = submittedQuestions,
                            onOptionToggle = viewModel::onOptionToggle,
                            onSubmitQuestion = viewModel::submitQuestion,
                            onSubmitAll = {
                                if (submittedQuestions.size < quiz.questions.size) {
                                    val unsubmitted = quiz.questions.indices.filter { it !in submittedQuestions }
                                    Toast.makeText(context, "Please submit questions: ${unsubmitted.map { it + 1 }.joinToString()}", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.submitAll()
                                }
                            }
                        )
                    }
                }
                is Response.Error -> {
                    Text(
                        (quizState as Response.Error).message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (scoreState != null) {
                ScoreDialog(score = scoreState!!, onDismiss = onBackClick)
            }
        }
    }
}

@Composable
fun QuizContent(
    quiz: Quiz,
    userAnswers: Map<Int, Set<Int>>,
    submittedQuestions: List<Int>,
    onOptionToggle: (Int, Int) -> Unit,
    onSubmitQuestion: (Int) -> Unit,
    onSubmitAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (quiz.imageUrl.isNotEmpty()) {
            item {
                AsyncImage(
                    model = quiz.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        itemsIndexed(quiz.questions) { index, question ->
            QuestionItem(
                index = index,
                question = question,
                selectedOptions = userAnswers[index] ?: emptySet(),
                isSubmitted = index in submittedQuestions,
                onOptionToggle = { onOptionToggle(index, it) },
                onSubmit = { onSubmitQuestion(index) }
            )
        }

        item {
            Button(
                onClick = onSubmitAll,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)
            ) {
                Text("Submit All", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuestionItem(
    index: Int,
    question: Question,
    selectedOptions: Set<Int>,
    isSubmitted: Boolean,
    onOptionToggle: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSubmitted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (question.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = question.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).padding(bottom = 12.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = "Q${index + 1}) ${question.text}:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 grid for options as requested
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OptionItem(0, question.options.getOrNull(0) ?: "", selectedOptions.contains(0), isSubmitted, onOptionToggle, Modifier.weight(1f))
                    OptionItem(1, question.options.getOrNull(1) ?: "", selectedOptions.contains(1), isSubmitted, onOptionToggle, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OptionItem(2, question.options.getOrNull(2) ?: "", selectedOptions.contains(2), isSubmitted, onOptionToggle, Modifier.weight(1f))
                    OptionItem(3, question.options.getOrNull(3) ?: "", selectedOptions.contains(3), isSubmitted, onOptionToggle, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                enabled = !isSubmitted && selectedOptions.isNotEmpty(),
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubmitted) Color.Gray else HoloCyan
                )
            ) {
                Text(if (isSubmitted) "Submitted" else "Submit Q${index + 1}")
            }
        }
    }
}

@Composable
fun OptionItem(
    index: Int,
    text: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) HoloCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(enabled = !isSubmitted) { onToggle(index) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1})",
            fontWeight = FontWeight.Bold,
            color = HoloCyan,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            maxLines = 2
        )
    }
}

@Composable
fun ScoreDialog(score: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quiz Completed!", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Your Score:", fontSize = 18.sp)
                Text(
                    text = "$score/100",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (score >= 50) HoloCyan else Color.Red
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = HoloPurple)) {
                Text("Close")
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
