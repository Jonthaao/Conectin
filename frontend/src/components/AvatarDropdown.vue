<template>
  <div class="avatar-dropdown" @click="toggleDropdown">
    <img :src="user ? user.foto || defaultAvatar : defaultAvatar" class="avatar" alt="Avatar" />
    <span v-if="user">{{ user.nome }}</span>
    <div v-if="dropdownVisible" class="dropdown-content">
      <template v-if="user">
        <router-link :to="{ name: 'EditarPerfilUsuario', params: { id: user.id } }">Meu Perfil</router-link>
        <a href="#" @click.prevent="logout">Logout</a>
      </template>
      <template v-else>
        <router-link to="/login">Login</router-link>
        <router-link to="/cadastro">Cadastre-se</router-link>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

// Define props
defineProps({
  defaultAvatar: {
    type: String,
    default: 'https://www.gravatar.com/avatar/?d=mp',
  },
});

// Pinia store e router
const userStore = useUserStore();
const router = useRouter();

// Reactive state
const dropdownVisible = ref(false);

// Computed user data from store
const user = computed(() => userStore.user);

// Methods
const toggleDropdown = (event) => {
  event.stopPropagation();
  dropdownVisible.value = !dropdownVisible.value;
};

const closeDropdown = (event) => {
  if (!event.target.closest('.avatar-dropdown')) {
    dropdownVisible.value = false;
  }
};

const logout = () => {
  userStore.logout();
  dropdownVisible.value = false;
  router.push({ name: 'Home' });
};

// Lifecycle hooks
onMounted(() => {
  userStore.loadUser();
  console.log('Usuário carregado:', userStore.user); // Para depuração
  document.addEventListener('click', closeDropdown);
});

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown);
});
</script>

<style scoped>
.avatar-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.dropdown-content {
  position: absolute;
  top: 50px;
  right: 0;
  background-color: #ffffff;
  border: 1px solid #ccc;
  border-radius: 5px;
  min-width: 150px;
  z-index: 999;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.dropdown-content a {
  display: block;
  padding: 10px;
  color: #257bb8;
  text-decoration: none;
  font-weight: 500;
}

.dropdown-content a:hover {
  background-color: #f4b400;
  color: #fff;
}
</style>