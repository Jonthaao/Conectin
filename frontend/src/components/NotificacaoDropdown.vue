<template>
  <div class="notification-dropdown" @click="toggleDropdown">
    <i class="fas fa-bell"></i>
    <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
    <div v-if="dropdownVisible" class="dropdown-content">
      <div v-if="notifications.length === 0" class="no-notifications">
        Nenhuma notificação
      </div>
      <div v-else>
        <div v-for="notification in notifications" :key="notification.id" class="notification-item" :class="{ unread: !notification.lida }" @click.stop="handleNotificationClick(notification)">
          <p>{{ notification.mensagem }}</p>
          <small>{{ formatDate(notification.dataCriacao) }}</small>
        </div>
        <button @click.stop="markAllAsRead" class="mark-all-btn">Marcar todas como lidas</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useNotificacaoStore } from '@/stores/notificacao';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { computed } from 'vue';

const notificacaoStore = useNotificacaoStore();
const router = useRouter();
const dropdownVisible = ref(false);

onMounted(() => {
  if (localStorage.getItem('token')) {
    notificacaoStore.fetchNotifications();
  }
});

const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value;
  if (dropdownVisible.value) {
    notificacaoStore.fetchNotifications();
  }
};

const handleNotificationClick = (notification) => {
  notificacaoStore.markAsRead(notification.id);
  if (notification.link) {
    router.push(notification.link);
  }
  dropdownVisible.value = false;
};

const markAllAsRead = () => {
  notificacaoStore.markAllAsRead();
};

const formatDate = (date) => {
  return new Date(date).toLocaleString('pt-BR');
};

const notifications = computed(() => notificacaoStore.notifications);
const unreadCount = computed(() => notificacaoStore.unreadCount);
</script>

<style scoped>
.notification-dropdown {
  position: relative;
  cursor: pointer;
  color: #fff;
  font-size: 1.5em;
  display: flex;
  align-items: center;
}

.fa-bell {
  margin-right: 5px;
}

.badge {
  background-color: #f4b400;
  color: #fff;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 0.8em;
  position: absolute;
  top: -5px;
  right: -5px;
}

.dropdown-content {
  position: absolute;
  top: 40px;
  right: 0;
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 5px;
  width: 300px;
  max-height: 400px;
  overflow-y: auto;
  z-index: 999;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.notification-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.notification-item.unread {
  background-color: #f0f8ff;
  font-weight: bold;
}

.notification-item p {
  margin: 0;
  color: #333;
}

.notification-item small {
  color: #777;
}

.no-notifications {
  padding: 10px;
  text-align: center;
  color: #666;
}

.mark-all-btn {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 0 0 5px 5px;
  cursor: pointer;
}

.mark-all-btn:hover {
  background-color: #0056b3;
}
</style>