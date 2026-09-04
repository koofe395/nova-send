package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.ui.NovaViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun MessagesScreen(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  val contacts by viewModel.allContacts.collectAsStateWithLifecycle()
  val activeContact by viewModel.activeChatContact.collectAsStateWithLifecycle()
  val messages by viewModel.chatMessages.collectAsStateWithLifecycle()

  var messageInput by remember { mutableStateOf("") }

  if (activeContact != null) {
    // Single Conversation View
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(com.example.ui.theme.NovaBackground)
        .testTag("conversation_view")
    ) {
      // Header
      Surface(
        color = Color.White,
        border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { viewModel.closeChat() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NovaTextPrimary)
          }

          UserAvatar(
            name = activeContact!!.name,
            initials = activeContact!!.avatarInitials,
            colorHex = activeContact!!.avatarColorHex,
            size = 36.dp,
            isOnline = activeContact!!.isOnline,
            isVerified = activeContact!!.isVerified
          )

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = activeContact!!.name,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = NovaTextPrimary
            )
            Text(
              text = "${activeContact!!.deviceName} • End-to-End Encrypted",
              fontSize = 11.sp,
              color = NovaSuccess
            )
          }
        }
      }

      // Messages List
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        reverseLayout = false,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          ChatBubble(message = msg)
        }
      }

      // Input Row
      Surface(
        color = Color.White,
        border = BorderStroke(1.dp, NovaBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              viewModel.sendMessage(
                text = "Attached transfer package:",
                isTransferCard = true,
                fileName = "Project_Update_Assets.zip",
                fileSize = "64 MB"
              )
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = NovaPrimary)
          }

          OutlinedTextField(
            value = messageInput,
            onValueChange = { messageInput = it },
            placeholder = { Text("Write encrypted message...", fontSize = 13.sp) },
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input_field"),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color(0xFFF8FAFC),
              unfocusedContainerColor = Color(0xFFF8FAFC),
              focusedIndicatorColor = NovaPrimary,
              unfocusedIndicatorColor = NovaBorder
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (messageInput.isNotBlank()) {
                viewModel.sendMessage(messageInput)
                messageInput = ""
              }
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(NovaPrimary)
              .testTag("send_msg_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  } else {
    // Contacts / Conversations List
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(com.example.ui.theme.NovaBackground)
        .testTag("messages_contacts_list")
    ) {
      Surface(
        color = Color.White,
        border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Transfer Conversations",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = "Secure direct messaging with your transfer contacts",
            fontSize = 12.sp,
            color = NovaTextSecondary
          )
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(contacts, key = { it.id }) { contact ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.openChatWith(contact) }
              .testTag("chat_contact_${contact.id}"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                  name = contact.name,
                  initials = contact.avatarInitials,
                  colorHex = contact.avatarColorHex,
                  size = 46.dp,
                  isOnline = contact.isOnline,
                  isVerified = contact.isVerified
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = contact.name,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = NovaTextPrimary
                    )
                    if (contact.isVerified) {
                      Spacer(modifier = Modifier.width(4.dp))
                      Icon(Icons.Default.Verified, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(13.dp))
                    }
                  }
                  Text(
                    text = "@${contact.username} • ${contact.deviceName}",
                    fontSize = 11.sp,
                    color = NovaTextSecondary
                  )
                }
              }

              Icon(Icons.Default.Chat, contentDescription = null, tint = NovaPrimaryLight, modifier = Modifier.size(20.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChatBubble(message: MessageEntity) {
  val isMe = message.isMe
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
  ) {
    Column(
      horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
      modifier = Modifier.fillMaxWidth(0.82f)
    ) {
      if (message.isTransferCard) {
        // Rich Transfer Card in Chat
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isMe) NovaPrimaryLight else Color.White
          ),
          border = BorderStroke(1.dp, NovaPrimary.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "${message.senderName} shared a file:",
              fontSize = 11.sp,
              color = NovaTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(NovaPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = message.transferFileName ?: "File.zip",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = NovaTextPrimary
                )
                Text(
                  text = message.transferFileSize ?: "48 MB",
                  fontSize = 11.sp,
                  color = NovaTextSecondary
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = { /* downloaded */ },
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().height(34.dp)
            ) {
              Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Download Directly", fontSize = 11.sp)
            }
          }
        }
      } else {
        // Regular Text Message
        Box(
          modifier = Modifier
            .clip(
              RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
              )
            )
            .background(if (isMe) NovaPrimary else Color.White)
            .border(
              1.dp,
              if (isMe) NovaPrimary else NovaBorder,
              RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Text(
            text = message.content,
            color = if (isMe) Color.White else NovaTextPrimary,
            fontSize = 13.sp
          )
        }
      }

      Text(
        text = formatTimestamp(message.timestamp),
        fontSize = 10.sp,
        color = NovaTextMuted,
        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
      )
    }
  }
}
