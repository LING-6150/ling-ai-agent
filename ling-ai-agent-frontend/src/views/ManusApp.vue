<template>
  <section class="manus-page">
    <div class="manus-container">
      <header class="manus-header">
        <div>
          <h2>AI Super Agent</h2>
          <p class="manus-subtitle">
            Enter a complex task and watch each execution step in real time.
          </p>
        </div>
        <button class="manus-reset-btn" @click="resetSteps" :disabled="isStreaming">
          Clear results
        </button>
      </header>

      <form class="manus-input-bar" @submit.prevent="handleRun">
        <input
          v-model="input"
          class="manus-input"
          type="text"
          autocomplete="off"
          placeholder="For example: Research and compare 3 Boston restaurants for a weekend date."
          :disabled="isStreaming"
        />
        <button
          type="submit"
          class="manus-run-btn"
          :disabled="!input.trim() || isStreaming"
        >
          <span v-if="!isStreaming">Run agent</span>
          <span v-else>Running…</span>
        </button>
      </form>

      <div class="manus-steps">
        <transition-group name="step-fade" tag="div">
          <article
            v-for="step in steps"
            :key="step.id"
            class="step-card"
            :class="{ 'step-card-tool': hasToolKeyword(step) }"
          >
            <div class="step-index">Step {{ step.id }}</div>
            <div class="step-content">
              <pre>{{ getDisplayText(step) }}</pre>
              <button
                v-if="step.content && step.content.length > 200"
                type="button"
                class="step-toggle-btn"
                @click="toggleStep(step)"
              >
                {{ step.isExpanded ? 'Collapse' : 'Expand' }}
              </button>
            </div>
          </article>
        </transition-group>

        <p v-if="!steps.length" class="manus-empty">
          No steps yet. Enter a task above and click “Run agent”.
        </p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue';

const API_BASE = 'http://localhost:8123/api';

const input = ref('');
const steps = ref([]);
const isStreaming = ref(false);

let stepCounter = 0;
let manusSource = null;

function appendStep(text) {
  steps.value.push({
    id: ++stepCounter,
    content: text,
    isExpanded: false
  });
}

function getDisplayText(step) {
  if (!step.content) return '';
  if (step.isExpanded || step.content.length <= 200) return step.content;
  return `${step.content.slice(0, 200)}...`;
}

function toggleStep(step) {
  step.isExpanded = !step.isExpanded;
}

function hasToolKeyword(step) {
  return typeof step.content === 'string' && step.content.includes('工具');
}

function stopStreaming() {
  if (manusSource) {
    manusSource.close();
    manusSource = null;
  }
  isStreaming.value = false;
}

function handleRun() {
  const text = input.value.trim();
  if (!text || isStreaming.value) return;

  isStreaming.value = true;
  steps.value = [];
  stepCounter = 0;

  const url =
    `${API_BASE}/ai/manus/chat` + `?message=${encodeURIComponent(text)}`;

  manusSource = new EventSource(url);

  manusSource.onmessage = (event) => {
    if (!event.data) return;
    appendStep(event.data);
  };

  manusSource.onerror = () => {
    stopStreaming();
  };
}

function resetSteps() {
  if (isStreaming.value) return;
  steps.value = [];
  stepCounter = 0;
}

onBeforeUnmount(() => {
  stopStreaming();
});
</script>

