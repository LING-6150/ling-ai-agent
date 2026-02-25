<template>
  <div class="app-root">
    <header class="app-header">
      <div class="app-logo">Ling AI Agent</div>
      <nav class="app-nav">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/love">AI Love Coach</RouterLink>
        <RouterLink to="/manus">AI Super Agent</RouterLink>
      </nav>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router';
</script>

