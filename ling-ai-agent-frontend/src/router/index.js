import { createRouter, createWebHistory } from 'vue-router';
import Home from '@/views/Home.vue';
import LoveApp from '@/views/LoveApp.vue';
import ManusApp from '@/views/ManusApp.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/love',
    name: 'LoveApp',
    component: LoveApp
  },
  {
    path: '/manus',
    name: 'ManusApp',
    component: ManusApp
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

