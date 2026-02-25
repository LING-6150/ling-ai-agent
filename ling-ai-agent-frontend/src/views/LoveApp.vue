<template>
  <section class="love-page">
    <div class="love-chat-container">
      <header class="love-chat-header">
        <div>
          <h2>AI Love Coach</h2>
          <p class="chat-subtitle">Chatroom ID: {{ chatId }}</p>
        </div>
        <button class="chat-reset-btn" @click="resetChat" :disabled="isStreaming">
          Reset session
        </button>
      </header>

      <div ref="messagesRef" class="love-chat-messages">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="chat-row"
          :class="msg.role === 'user' ? 'chat-row-user' : 'chat-row-ai'"
        >
          <div
            class="chat-bubble"
            :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'"
          >
            <p>
              <span v-if="msg.role === 'ai' && msg.isStreaming" class="typing-text">
                {{ msg.content }}<span class="typing-cursor"></span>
              </span>
              <span v-else>{{ msg.content }}</span>
            </p>
          </div>
        </div>
      </div>

      <form class="love-chat-input-bar" @submit.prevent="handleSend">
        <input
          v-model="input"
          class="love-input"
          type="text"
          autocomplete="off"
          placeholder="Share your current relationship concern, for example: We keep arguing about small things..."
          :disabled="isStreaming"
        />
        <button
          type="submit"
          class="love-send-btn"
          :disabled="!input.trim() || isStreaming"
        >
          <span v-if="!isStreaming">Send</span>
          <span v-else>Thinking…</span>
        </button>
      </form>
    </div>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';

const API_BASE = 'http://localhost:8123/api';

const input = ref('');
const messages = ref([]);
const isStreaming = ref(false);
const chatId = ref('');
const messagesRef = ref(null);

let currentSource = null;
let messageCounter = 0;

function genChatId() {
  const random = Math.random().toString(36).slice(2, 8);
  const timestamp = Date.now().toString(36);
  return `love-${timestamp}-${random}`;
}

function scrollToBottom() {
  requestAnimationFrame(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
    }
  });
}

function appendMessage(partial) {
  messages.value.push({
    id: ++messageCounter,
    role: partial.role,
    content: partial.content || '',
    isStreaming: !!partial.isStreaming
  });
  scrollToBottom();
}

function updateStreamingMessage(content) {
  const last = messages.value[messages.value.length - 1];
  if (last && last.role === 'ai' && last.isStreaming) {
    last.content = content;
    scrollToBottom();
  }
}

function stopStreaming() {
  if (currentSource) {
    currentSource.close();
    currentSource = null;
  }
  isStreaming.value = false;

  const last = messages.value[messages.value.length - 1];
  if (last && last.role === 'ai' && last.isStreaming) {
    last.isStreaming = false;
  }
}

function mergeChunk(previous, chunk) {
  if (!previous) return chunk || '';
  if (!chunk) return previous;

  const lastChar = previous[previous.length - 1];
  const firstChar = chunk[0];

  const isWordChar = (ch) => /[A-Za-z0-9]/.test(ch);

  if (isWordChar(lastChar) && isWordChar(firstChar)) {
    return `${previous} ${chunk}`;
  }

  return previous + chunk;
}

function handleSend() {
  const text = input.value.trim();
  if (!text || isStreaming.value) return;

  appendMessage({ role: 'user', content: text, isStreaming: false });
  input.value = '';

  isStreaming.value = true;

  const url =
    `${API_BASE}/ai/love_app/chat/sse` +
    `?message=${encodeURIComponent(text)}` +
    `&chatId=${encodeURIComponent(chatId.value)}`;

  appendMessage({ role: 'ai', content: '', isStreaming: true });

  let buffer = '';
  currentSource = new EventSource(url);

  currentSource.onmessage = (event) => {
    if (!event.data) return;
    buffer = mergeChunk(buffer, event.data);
    updateStreamingMessage(buffer);
  };

  currentSource.onerror = () => {
    stopStreaming();
  };

  currentSource.onopen = () => {
    // connection established
  };
}

function resetChat() {
  if (isStreaming.value) return;
  messages.value = [];
  messageCounter = 0;
  chatId.value = genChatId();
}

onMounted(() => {
  chatId.value = genChatId();
});

onBeforeUnmount(() => {
  stopStreaming();
});
</script>

