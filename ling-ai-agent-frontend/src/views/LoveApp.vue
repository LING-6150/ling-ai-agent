<template>
  <section class="love-page">
    <div class="love-chat-container">
      <header class="love-chat-header">
        <div>
          <h2>AI Love Coach</h2>
          <p class="chat-subtitle">Chatroom ID: {{ chatId }}</p>
        </div>
        <div class="header-controls">
          <!-- RAG 模式切换 -->
          <label class="rag-toggle">
            <input type="checkbox" v-model="ragMode" :disabled="isStreaming" />
            <span>RAG Mode</span>
          </label>
          <!-- 状态选择（RAG模式下显示） -->
          <select v-if="ragMode" v-model="status" class="status-select" :disabled="isStreaming">
            <option value="single">Single</option>
            <option value="dating">Dating</option>
            <option value="married">Married</option>
          </select>
          <button class="chat-reset-btn" @click="resetChat" :disabled="isStreaming">
            Reset session
          </button>
        </div>
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
            <!-- 主要回答内容 -->
            <p>
              <span v-if="msg.role === 'ai' && msg.isStreaming" class="typing-text">
                {{ msg.answer }}<span class="typing-cursor"></span>
              </span>
              <span v-else>{{ msg.answer || msg.content }}</span>
            </p>

            <!-- Sources 展示（只在 RAG 模式下显示） -->
            <div v-if="msg.sources && msg.sources.length > 0" class="sources-container">
              <div class="sources-title">📎 Sources</div>
              <div v-for="(source, idx) in msg.sources" :key="idx" class="source-item">
                {{ source }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <form class="love-chat-input-bar" @submit.prevent="handleSend">
        <input
            v-model="input"
            class="love-input"
            type="text"
            autocomplete="off"
            placeholder="Share your current relationship concern..."
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
const ragMode = ref(false);
const status = ref('dating');

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

// 解析 Sources
function parseSourcesFromContent(content) {
  if (!content || !content.includes('**Sources:**')) {
    return { answer: content, sources: [] };
  }
  const parts = content.split('**Sources:**');
  const answer = parts[0].trim();
  const sourcesRaw = parts[1] ? parts[1].trim() : '';
  const sources = sourcesRaw
      .split('\n')
      .map(s => s.replace(/^-\s*/, '').trim())
      .filter(s => s.length > 0);
  return { answer, sources };
}

function appendMessage(partial) {
  messages.value.push({
    id: ++messageCounter,
    role: partial.role,
    content: partial.content || '',
    answer: partial.answer || partial.content || '',
    sources: partial.sources || [],
    isStreaming: !!partial.isStreaming
  });
  scrollToBottom();
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

async function handleSend() {
  const text = input.value.trim();
  if (!text || isStreaming.value) return;

  appendMessage({ role: 'user', content: text });
  input.value = '';
  isStreaming.value = true;

  // RAG 模式：同步调用 rerank 接口
  if (ragMode.value) {
    appendMessage({ role: 'ai', content: '', answer: '', sources: [], isStreaming: true });
    try {
      const url = `${API_BASE}/ai/love_app/chat/rag/rerank` +
          `?message=${encodeURIComponent(text)}` +
          `&chatId=${encodeURIComponent(chatId.value)}` +
          `&status=${encodeURIComponent(status.value)}`;

      const response = await fetch(url);
      const rawContent = await response.text();

      const { answer, sources } = parseSourcesFromContent(rawContent);

      const last = messages.value[messages.value.length - 1];
      if (last && last.role === 'ai') {
        last.answer = answer;
        last.sources = sources;
        last.isStreaming = false;
      }
    } catch (e) {
      const last = messages.value[messages.value.length - 1];
      if (last) {
        last.answer = 'Error occurred. Please try again.';
        last.isStreaming = false;
      }
    }
    isStreaming.value = false;
    scrollToBottom();
    return;
  }

  // 普通 SSE 模式
  const url = `${API_BASE}/ai/love_app/chat/sse` +
      `?message=${encodeURIComponent(text)}` +
      `&chatId=${encodeURIComponent(chatId.value)}`;

  appendMessage({ role: 'ai', content: '', answer: '', isStreaming: true });
  let buffer = '';
  currentSource = new EventSource(url);

  currentSource.onmessage = (event) => {
    if (!event.data) return;
    buffer = mergeChunk(buffer, event.data);
    const last = messages.value[messages.value.length - 1];
    if (last && last.role === 'ai') {
      last.answer = buffer;
    }
    scrollToBottom();
  };

  currentSource.onerror = () => {
    stopStreaming();
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

<style scoped>
.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rag-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.rag-toggle input {
  cursor: pointer;
}

.status-select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #ddd;
  font-size: 13px;
  cursor: pointer;
}

.sources-container {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.sources-title {
  font-size: 12px;
  font-weight: 600;
  color: #888;
  margin-bottom: 6px;
}

.source-item {
  font-size: 12px;
  color: #999;
  padding: 3px 0;
  padding-left: 8px;
  border-left: 2px solid #e0e0e0;
  margin-bottom: 4px;
}
</style>