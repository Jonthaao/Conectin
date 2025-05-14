import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../views/HomePage.vue';
import LoginPage from '../views/Login/LoginPage.vue';
import CadastroPage from '../views/Login/CadastroPage.vue';
import ComoFuncionaPage from '../views/ComoFunciona.vue';
import RankingPrestadores from '../views/Usuario/Prestador/RankingPrestadores.vue';
import PerfilPrestador from '../views/Usuario/Prestador/PerfilPrestador.vue';
import { useUserStore } from '../stores/user'; // Add this import
import EditarPerfilUsuario from '../views/Usuario/EditarPerfilUsuario.vue'; // Import the EditarUsuario component
import AvaliarServico from '@/views/Usuario/AvaliarServico.vue';

const routes = [
  { path: '/', name: 'Home', component: HomePage },
  { path: '/login', name: 'Login', component: LoginPage },
  { path: '/cadastro', name: 'Cadastro', component: CadastroPage },
  { path: '/comofunciona', name: 'ComoFunciona', component: ComoFuncionaPage },
  {
    path: '/ranking-prestadores',
    name: 'RankingPrestadores',
    component: RankingPrestadores,
  },
  {
    path: '/prestador/:id',
    name: 'PerfilPrestador',
    component: PerfilPrestador,
    props: true, // Pass route params as props to the component
  },
  {
    path: '/EditarPerfilUsuario/:id',
    name: 'EditarPerfilUsuario',
    component: EditarPerfilUsuario,
    meta: { requiresAuth: true }, // Optional: Mark as protected
  },
  {
    path: '/AvaliarServico/:id',
    name: 'AvaliarServico',
    component: AvaliarServico,
    meta: { requiresAuth: true }, // Optional: Mark as protected
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard for protected routes
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth);

  if (requiresAuth && !userStore.user) {
    next({ name: 'Login' }); // Redirect to login if not authenticated
  } else {
    next();
  }
});

export default router;