import { defineStore } from 'pinia';
import api from '@/services/api';
import { useUserStore } from './user';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
  }),
  actions: {
    async login(email, senha) {
      console.log('Enviando payload:', { email, senha });
      const response = await api.post('usuarios/login', { email, senha });
      const token = response.data.token; // Acessar diretamente o token

      // Buscar dados completos do usuário
      let userData;
      if (response.data.user) {
        userData = response.data.user;
      } else {
        const profileResponse = await api.get('/usuarios/perfil', {
          headers: { Authorization: `Bearer ${token}` },
        });
        userData = profileResponse.data;
      }

      this.token = token;
      this.user = userData;

      // Atualizar o userStore
      const userStore = useUserStore();
      userStore.setUser(userData);

      // Armazenar no localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('usuarioLogado', JSON.stringify(userData));

      return response.data;
    },
    logout() {
      this.user = null;
      this.token = null;

      // Limpar o userStore
      const userStore = useUserStore();
      userStore.logout();

      localStorage.removeItem('token');
      localStorage.removeItem('usuarioLogado');
    },
  },
});