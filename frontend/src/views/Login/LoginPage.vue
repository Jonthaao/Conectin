<template>
  <div class="login-container">
    <div class="login-box">
      <h2>Login</h2>
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <input v-model="email" type="email" placeholder="Email" required class="input-field" />
        </div>
        <div class="input-group">
          <input v-model="senha" type="password" placeholder="Senha" required class="input-field" />
        </div>
        <button type="submit" class="login-btn">Entrar</button>
      </form>
      <div v-if="showForgotPassword" class="forgot-password">
        <p>Você esqueceu sua senha? <a href="/recuperar-senha">Clique aqui!</a></p>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';
import { useToast } from 'vue-toastification';

export default {
  data() {
    return {
      email: '',
      senha: '',
      showForgotPassword: false, // Controla a exibição da mensagem de recuperação
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  methods: {
    async handleLogin() {
      const authStore = useAuthStore();
      const userStore = useUserStore();
      this.showForgotPassword = false; // Resetar a mensagem antes de tentar o login
      try {
        const response = await authStore.login(this.email, this.senha);
        this.toast.success(response.message);
        await userStore.loadUser(); // Garantir que o userStore está atualizado
        this.$router.push('/');
      } catch (error) {
        console.log('Erro completo:', error);
        if (error.response && error.response.data && error.response.data.message) {
          const errorMessage = error.response.data.message;
          this.toast.error(errorMessage);
          // Exibir a mensagem de recuperação para erros específicos
          if (
            errorMessage.includes('Usuário não encontrado') ||
            errorMessage.includes('Senha incorreta')
          ) {
            this.showForgotPassword = true;
          }
        } else {
          this.toast.error('Erro ao fazer login: ' + (error.message || 'Servidor não retornou detalhes.'));
        }
      }
    },
  },
};
</script>
  
  <style scoped>
  .login-container {
    max-width: 400px;
    margin: 50px auto;
  }
  
  .login-box {
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 5px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  }
  
  .input-group {
    margin-bottom: 15px;
  }
  
  .input-field {
    width: 95%;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  .login-btn {
    width: 100%;
    padding: 10px;
    background-color: #ffc107; /* amarelo */
    color: white;
    font-size: large;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .login-btn:hover {
    background-color: #257bb8; /* azul ao hover */
  }
</style>
