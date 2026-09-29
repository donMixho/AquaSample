package com.aldemar.aquasample.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.components.StatusBadge
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.theme.StatusAlert
import com.aldemar.aquasample.ui.theme.StatusAlertBg
import com.aldemar.aquasample.ui.viewmodel.AuthUiState
import com.aldemar.aquasample.ui.viewmodel.SampleListViewModel

/**
 * Pantalla 2: Historial de Muestras y Panel Principal (HomeScreen).
 *
 * Basada en el prototipo: AquaSample_MVP_Stitch/MVP/aquamuestra_historial_de_muestras/historial.html
 *
 * Funcionalidades clave:
 * - Buscador en tiempo real por texto (código, centro, tren, línea o responsable).
 * - Filtros rápidos por estado de trazabilidad (Todos, Pendientes, Validados, Observados).
 * - Renderizado en LazyColumn de tarjetas de muestra con badges y conteo de individuos.
 * - Botón Flotante (FAB) para que el operador inicie rápidamente una nueva muestra.
 * - Acceso directo al módulo de supervisión al tocar cualquier tarjeta.
 */
@Composable
fun HomeScreen(
    authUiState: AuthUiState,
    viewModel: SampleListViewModel,
    onNavigateToNewSample: () -> Unit,
    onNavigateToReview: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val samples by viewModel.filteredSamples.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "AquaSample · ALDEMAR",
                canNavigateBack = false,
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (authUiState.currentRole == UserRole.SUPERVISOR) "Supervisor" else "Operador",
                            color = AquaSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = AquaSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo los operadores ven el botón flotante para crear muestras en terreno
            if (authUiState.currentRole == UserRole.OPERADOR) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToNewSample,
                    containerColor = AquaPrimary,
                    contentColor = AquaSurface,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva Muestra", fontWeight = FontWeight.Bold) }
                )
            }
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Buscador en tiempo real
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Buscar por código, centro o línea...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = AquaTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquaSecondary,
                    unfocusedBorderColor = AquaOutline,
                    focusedContainerColor = AquaSurface,
                    unfocusedContainerColor = AquaSurface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chips de filtro por estado
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = AquaSurface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == SampleStatus.PENDIENTE,
                        onClick = { viewModel.setStatusFilter(SampleStatus.PENDIENTE) },
                        label = { Text("Pendientes") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = AquaSurface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == SampleStatus.VALIDADO,
                        onClick = { viewModel.setStatusFilter(SampleStatus.VALIDADO) },
                        label = { Text("Validados") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = AquaSurface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == SampleStatus.OBSERVADO,
                        onClick = { viewModel.setStatusFilter(SampleStatus.OBSERVADO) },
                        label = { Text("Observados") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = AquaSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de cantidad de resultados
            Text(
                text = "${samples.size} muestras registradas en el historial local",
                fontSize = 12.sp,
                color = AquaTextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Listado de Tarjetas de Muestra
            if (samples.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron muestras registradas.",
                        color = AquaTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(samples, key = { it.id }) { sample ->
                        SampleCardItem(
                            sample = sample,
                            isSupervisor = authUiState.currentRole == UserRole.SUPERVISOR,
                            onClick = {
                                onNavigateToReview(sample.id)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

/**
 * Componente individual de tarjeta para cada registro de muestreo.
 */
@Composable
fun SampleCardItem(
    sample: SampleEntity,
    isSupervisor: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AquaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Encabezado: Código de muestra y Badge de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sample.code,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AquaPrimary
                )
                StatusBadge(status = SampleStatus.fromString(sample.status))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Centro de Cultivo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AquaSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sample.centerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaTextPrimary
                )
            }

            // Tren y Línea
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = null,
                    tint = AquaTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${sample.trainNumber} · ${sample.lineNumber} (Tramo: ${sample.sectionLengthMeters}m)",
                    fontSize = 13.sp,
                    color = AquaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bloque de Métricas: Conteo de individuos y foto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AquaBackground, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conteo: ${sample.musselCount} individuos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaTextPrimary
                )
                Text(
                    text = if (sample.photoPath != null) "📷 Foto adjunta" else "Sin fotografía",
                    fontSize = 12.sp,
                    color = if (sample.photoPath != null) AquaPrimary else AquaTextSecondary
                )
            }

            // Advertencia visual si el supervisor dejó una observación
            if (!sample.supervisorComment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusAlertBg, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        tint = StatusAlert,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sample.supervisorComment,
                        fontSize = 11.sp,
                        color = StatusAlert,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pie de tarjeta: Fecha y Operador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AquaTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sample.sampleDate,
                        fontSize = 11.sp,
                        color = AquaTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AquaTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sample.operatorName,
                        fontSize = 11.sp,
                        color = AquaTextSecondary
                    )
                }
            }
        }
    }
}
