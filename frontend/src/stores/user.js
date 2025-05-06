import { defineStore } from 'pinia';
import api from '@/services/api';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
  }),
  actions: {
    setUser(userData) {
      this.user = userData;
    },
    async loadUser() {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const response = await api.get('/usuarios/perfil', {
            headers: { Authorization: `Bearer ${token}` },
          });
          this.user = response.data;
          localStorage.setItem('usuarioLogado', JSON.stringify(this.user));
        } catch (error) {
          console.error('Erro ao carregar usuário:', error);
          const userData = localStorage.getItem('usuarioLogado');
          if (userData) {
            this.user = JSON.parse(userData);
          } else {
            this.user = null;
            localStorage.removeItem('token');
            localStorage.removeItem('usuarioLogado');
          }
        }
      } else {
        const userData = localStorage.getItem('usuarioLogado');
        if (userData) {
          this.user = JSON.parse(userData);
        }
      }
    },
    logout() {
      this.user = null;
      localStorage.removeItem('usuarioLogado');
      localStorage.removeItem('token');
    },
  },
});