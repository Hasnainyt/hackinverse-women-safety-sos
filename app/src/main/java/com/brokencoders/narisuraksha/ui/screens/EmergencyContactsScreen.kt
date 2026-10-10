package com.brokencoders.narisuraksha.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.R
import com.brokencoders.narisuraksha.data.EmergencyContactEntity
import com.brokencoders.narisuraksha.ui.components.AppBottomBar
import com.brokencoders.narisuraksha.ui.components.ConfirmDialog
import com.brokencoders.narisuraksha.ui.components.EmptyState
import com.brokencoders.narisuraksha.ui.components.InfoCard
import com.brokencoders.narisuraksha.ui.components.PrimaryButton
import com.brokencoders.narisuraksha.ui.components.SecondaryButton
import com.brokencoders.narisuraksha.ui.theme.BorderDivider
import com.brokencoders.narisuraksha.ui.theme.CardBackground
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.PrimaryPink
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.SoftPinkSurface
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val contacts by viewModel.emergencyContacts.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<EmergencyContactEntity?>(null) }
    var contactToDelete by remember { mutableStateOf<EmergencyContactEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.emergency_contacts_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    contactToEdit = null
                    showAddEditDialog = true
                },
                containerColor = PrimaryPink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_contact),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = "emergency_contacts",
                onNavigate = { route ->
                    if (route != "emergency_contacts") onBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Informational Header Card
            InfoCard(
                backgroundColor = SoftPinkSurface,
                borderColor = BorderDivider
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.trusted_circle_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.trusted_circle_subtitle),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Person,
                    headline = stringResource(R.string.no_contacts_title),
                    body = stringResource(R.string.no_contacts_subtitle),
                    action = {
                        PrimaryButton(
                            text = stringResource(R.string.add_first_contact),
                            onClick = {
                                contactToEdit = null
                                showAddEditDialog = true
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onCall = { viewModel.callContact(contact.phoneNumber) },
                            onMessage = { viewModel.sendEmergencySms(contact.phoneNumber) },
                            onTogglePrimary = {
                                viewModel.updateEmergencyContact(contact.copy(isPrimary = !contact.isPrimary))
                            },
                            onEdit = {
                                contactToEdit = contact
                                showAddEditDialog = true
                            },
                            onDelete = {
                                contactToDelete = contact
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showAddEditDialog) {
            AddEditContactDialog(
                initialContact = contactToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { name, phone, rel, isPrimary ->
                    if (contactToEdit != null) {
                        viewModel.updateEmergencyContact(
                            contactToEdit!!.copy(
                                name = name,
                                phoneNumber = phone,
                                relationship = rel,
                                isPrimary = isPrimary
                            )
                        )
                    } else {
                        viewModel.addEmergencyContact(name, phone, rel, isPrimary)
                    }
                    showAddEditDialog = false
                }
            )
        }

        // Delete Confirmation Dialog
        contactToDelete?.let { contact ->
            ConfirmDialog(
                title = stringResource(R.string.delete_contact_confirm_title),
                message = stringResource(R.string.delete_contact_confirm_message, contact.name),
                confirmText = "Delete",
                dismissText = "Cancel",
                isDanger = true,
                onConfirm = {
                    viewModel.deleteEmergencyContact(contact)
                    contactToDelete = null
                },
                onDismiss = { contactToDelete = null }
            )
        }
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContactEntity,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onTogglePrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    InfoCard(
        backgroundColor = CardBackground,
        borderColor = if (contact.isPrimary) PrimaryPink else BorderDivider
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (contact.isPrimary) SoftPinkSurface else BorderDivider.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (contact.isPrimary) PrimaryPink else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = TextPrimary
                            )

                            // Star icon for primary contact
                            if (contact.isPrimary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.primary_contact_star),
                                    tint = VigilanceAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (contact.relationship.isNotBlank()) {
                                "${contact.relationship} • ${contact.phoneNumber}"
                            } else {
                                contact.phoneNumber
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            color = TextSecondary
                        )
                    }
                }

                // Header Action Buttons (Star toggle, Edit, Delete)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (contact.isPrimary) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (contact.isPrimary) "Remove Primary" else "Make Primary",
                            tint = if (contact.isPrimary) VigilanceAmber else TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_contact),
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete contact",
                            tint = EmergencyRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stress-proof 56dp action buttons: Call & Message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.call_now),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                SecondaryButton(
                    text = stringResource(R.string.send_sms_btn),
                    icon = Icons.AutoMirrored.Filled.Message,
                    onClick = onMessage,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
            }
        }
    }
}

@Composable
private fun AddEditContactDialog(
    initialContact: EmergencyContactEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phoneNumber ?: "") }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "") }
    var isPrimary by remember { mutableStateOf(initialContact?.isPrimary ?: false) }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialContact != null) stringResource(R.string.edit_contact) else stringResource(R.string.add_contact),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Full Name", fontSize = 16.sp) },
                    isError = nameError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        focusedLabelColor = PrimaryPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = false },
                    label = { Text("Phone Number", fontSize = 16.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        focusedLabelColor = PrimaryPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g. Mother, Sister)", fontSize = 16.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        focusedLabelColor = PrimaryPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPink)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set as Primary Contact",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Save Contact",
                onClick = {
                    if (name.isBlank()) { nameError = true; return@PrimaryButton }
                    if (phone.isBlank()) { phoneError = true; return@PrimaryButton }
                    onSave(name.trim(), phone.trim(), relationship.trim(), isPrimary)
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
