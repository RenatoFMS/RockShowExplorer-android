package com.example.rockshowexplorer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<RockShowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RockShowAppTheme {
                MainNavigator(viewModel)
            }
        }
    }
}

val HeavyMetalDark = Color(0xFF121212)
val BloodRed = Color(0xFFB71C1C)
val SteelGrey = Color(0xFF2D2D2D)
val LightGrey = Color(0xFFB0B0B0)

@Composable
fun RockShowAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(background = HeavyMetalDark, surface = SteelGrey, primary = BloodRed, onPrimary = Color.White),
        content = content
    )
}

@Composable
fun MainNavigator(viewModel: RockShowViewModel) {
    val auth = FirebaseAuth.getInstance()
    var isUserLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    if (isUserLoggedIn) {
        RockShowMainScreen(viewModel, onSignOut = {
            auth.signOut()
            isUserLoggedIn = false
        })
    } else {
        LoginScreen(onLoginSuccess = { isUserLoggedIn = true })
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(HeavyMetalDark).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ROCK ARCHIVE 🤘", color = BloodRed, fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail", color = LightGrey) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = LightGrey, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Palavra-passe", color = LightGrey) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = LightGrey, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task -> if (task.isSuccessful) onLoginSuccess() else Toast.makeText(context, "Erro no login.", Toast.LENGTH_SHORT).show() }
            } else Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
        }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("ENTRAR", fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task -> if (task.isSuccessful) { Toast.makeText(context, "Conta criada!", Toast.LENGTH_SHORT).show(); onLoginSuccess() } else Toast.makeText(context, "Erro ao criar conta.", Toast.LENGTH_LONG).show() }
            } else Toast.makeText(context, "Preencha os campos para registar!", Toast.LENGTH_SHORT).show()
        }) { Text("Não tem conta? Registe-se aqui", color = LightGrey) }
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "/"
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RockShowMainScreen(viewModel: RockShowViewModel, onSignOut: () -> Unit) {
    val shows by viewModel.shows.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showToEdit by remember { mutableStateOf<RockShow?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Arquivo de Concertos", fontWeight = FontWeight.Bold, color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = BloodRed), actions = { TextButton(onClick = onSignOut) { Text("Sair", color = Color.White) } }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = BloodRed, contentColor = Color.White) { Icon(Icons.Default.Add, "Adicionar Concerto") } },
        containerColor = HeavyMetalDark
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 8.dp)) {
            items(shows) { show ->
                ShowItemCard(
                    show = show,
                    onDelete = { viewModel.deleteShow(show.id) },
                    onClick = { showToEdit = show }
                )
            }
        }

        if (showAddDialog) {
            ShowFormDialog(
                initialShow = null,
                onDismiss = { showAddDialog = false },
                onSave = { newShow -> viewModel.addShow(newShow); showAddDialog = false }
            )
        }

        showToEdit?.let { show ->
            ShowFormDialog(
                initialShow = show,
                onDismiss = { showToEdit = null },
                onSave = { updatedShow -> viewModel.updateShow(updatedShow); showToEdit = null }
            )
        }
    }
}

@Composable
fun ShowItemCard(show: RockShow, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SteelGrey),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (show.ticketImageUrl.isNotEmpty() || show.setlistImageUrl.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (show.ticketImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = show.ticketImageUrl, contentDescription = "Bilhete",
                            modifier = Modifier.weight(1f).height(120.dp), contentScale = ContentScale.Crop
                        )
                    }
                    if (show.setlistImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = show.setlistImageUrl, contentDescription = "Setlist",
                            modifier = Modifier.weight(1f).height(120.dp), contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = show.bandName, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val formattedDate = if (show.tourDate.length == 8 && !show.tourDate.contains("/")) {
                        "${show.tourDate.substring(0,2)}/${show.tourDate.substring(2,4)}/${show.tourDate.substring(4,8)}"
                    } else show.tourDate

                    Text(text = "📅 $formattedDate", color = LightGrey, fontSize = 14.sp)
                    Text(text = "🏟️ ${show.arena}", color = LightGrey, fontSize = 14.sp)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Apagar", tint = BloodRed) }
            }
        }
    }
}

@Composable
fun ShowFormDialog(initialShow: RockShow?, onDismiss: () -> Unit, onSave: (RockShow) -> Unit) {
    var band by remember { mutableStateOf(initialShow?.bandName ?: "") }
    var date by remember { mutableStateOf(initialShow?.tourDate?.replace("/", "") ?: "") }
    var arena by remember { mutableStateOf(initialShow?.arena ?: "") }

    var ticketUrl by remember { mutableStateOf(initialShow?.ticketImageUrl ?: "") }
    var setlistUrl by remember { mutableStateOf(initialShow?.setlistImageUrl ?: "") }
    val context = LocalContext.current

    val ticketLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            ticketUrl = it.toString()
        }
    }

    val setlistLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setlistUrl = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteelGrey,
        title = { Text(if (initialShow == null) "Adicionar Concerto" else "Editar Concerto", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = band, onValueChange = { band = it }, label = { Text("Nome da Banda", color = LightGrey) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = date, onValueChange = { if (it.length <= 8) date = it.filter { char -> char.isDigit() } },
                    label = { Text("Data", color = LightGrey) }, visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = arena, onValueChange = { arena = it }, label = { Text("Arena/Estádio", color = LightGrey) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Digitalização (Cole o Link ou use a Galeria)", color = LightGrey, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = ticketUrl, onValueChange = { ticketUrl = it }, label = { Text("URL do Bilhete", color = LightGrey) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { ticketLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Image, contentDescription = "Galeria Bilhete", tint = BloodRed)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = setlistUrl, onValueChange = { setlistUrl = it }, label = { Text("URL do Setlist", color = LightGrey) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { setlistLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Image, contentDescription = "Galeria Setlist", tint = BloodRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (band.isNotBlank() && date.length == 8 && arena.isNotBlank()) {
                        val formattedDate = "${date.substring(0,2)}/${date.substring(2,4)}/${date.substring(4,8)}"
                        val idToSave = initialShow?.id ?: ""
                        onSave(RockShow(id = idToSave, bandName = band, tourDate = formattedDate, arena = arena, ticketImageUrl = ticketUrl, setlistImageUrl = setlistUrl))
                    } else if (date.length < 8) {
                        Toast.makeText(context, "Data incompleta.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Por favor, preencha os textos principais.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
            ) { Text("Guardar", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = LightGrey) } }
    )
}
