import { defineStore } from 'pinia';
import api from '@/services/api';

export const useNotificacaoStore = defineStore('notificacao', {
  state: () => ({
    notifications: [],
    unreadCount: 0,
  }),
  actions: {
    async fetchNotifications() {
      try {
        const response = await api.get('/notificacoes', {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        this.notifications = response.data;
        this.unreadCount = this.notifications.filter(n => !n.lida).length;
      } catch (error) {
        console.error('Erro ao buscar notificações:', error);
      }
    },
    async markAsRead(id) {
      try {
        await api.post(`/notificacoes/${id}/marcar-lida`, {}, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        const notification = this.notifications.find(n => n.id === id);
        if (notification) {
          notification.lida = true;
          this.unreadCount = this.notifications.filter(n => !n.lida).length;
        }
      } catch (error) {
        console.error('Erro ao marcar notificação como lida:', error);
      }
    },
    async markAllAsRead() {
      try {
        await api.post('/notificacoes/marcar-todas-lidas', {}, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        this.notifications.forEach(n => (n.lida = true));
        this.unreadCount = 0;
      } catch (error) {
        console.error('Erro ao marcar todas como lidas:', error);
      }
    },
  },
});