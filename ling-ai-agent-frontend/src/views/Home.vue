<template>
  <section class="home-page">
    <div class="home-hero">
      <h1>Choose your AI experience</h1>
      <p>Enjoy a romantic chat coach and a multi-step super agent in one place.</p>
    </div>
    <div class="home-cards">
      <RouterLink to="/love" class="app-card love-card">
        <div class="card-badge">Love · Mind</div>
        <h2>AI Love Coach</h2>
        <p>A realistic chat space for warm and thoughtful relationship advice.</p>
        <span class="card-link">Enter chatroom →</span>
      </RouterLink>

      <RouterLink to="/manus" class="app-card manus-card">
        <div class="card-badge">Agent · Workflow</div>
        <h2>AI Super Agent</h2>
        <p>Execute complex tasks step by step and inspect each result in real time.</p>
        <span class="card-link">View agent steps →</span>
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>

