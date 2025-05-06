<template>
  <div class="editar-usuario">
    <h1>Editar Perfil</h1>
    <form @submit.prevent="saveProfile">
      <label>
        Nome:
        <input v-model="user.nome" type="text" required />
      </label>
      <label>
        Foto (URL):
        <input v-model="user.foto" type="url" />
      </label>
      <button type="submit">Salvar</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '../../stores/user';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { useToast } from 'vue-toastification';

const userStore = useUserStore();
const router = useRouter();
const toast = useToast();

const user = ref(userStore.user || { nome: '', foto: '' });

const saveProfile = async () => {
  try {
    const response = await api.put('/usuarios/editar/' + userStore.user.id, {
      nome: user.value.nome,
      foto: user.value.foto,
    });
    userStore.user.nome = user.value.nome;
    userStore.user.foto = user.value.foto;
    localStorage.setItem('usuarioLogado', JSON.stringify(userStore.user));
    toast.success(response.data.message);
    router.push({ name: 'Home' });
  } catch (error) {
    if (error.response && error.response.data) {
      toast.error(error.response.data.message);
    } else {
      toast.error('Erro ao salvar perfil.');
    }
  }
};
</script>

<style scoped>
.editar-usuario {
  max-width: 500px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

label {
  display: block;
  margin-bottom: 10px;
}

input {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}
</style>