<template>
  <div class="register-container">
    <div class="register-box">
      <h1>Faça seu Cadastro</h1>
      <form @submit.prevent="cadastrarUsuario">
        <div class="input-group">
          <label for="nome">Nome:</label>
          <input v-model="usuario.nome" type="text" id="nome" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="endereco">Endereço:</label>
          <input v-model="usuario.endereco" type="text" id="endereco" required class="input-field" />
        </div>
        <div class="input-group">
          <label>Tipo de Usuário:</label>
          <div class="checkbox-group">
            <div>
              <br>
              <input type="checkbox" id="prestador" v-model="usuario.prestador" />
              <label for="prestador">Prestador</label>
            </div>
            <div>
              <br>
              <input type="checkbox" id="cliente" v-model="usuario.cliente" />
              <label for="cliente">Cliente</label>
            </div>
          </div>
        </div>
        <div class="input-group">
          <label for="email">Email:</label>
          <input v-model="usuario.email" type="email" id="email" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="senha">Senha:</label>
          <input v-model="usuario.senha" type="password" id="senha" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="confirmarSenha">Confirmar Senha:</label>
          <input v-model="usuario.confirmarSenha" type="password" id="confirmarSenha" required class="input-field" />
        </div>
        <button type="submit" class="register-btn">Cadastrar</button>
      </form>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

export default {
  name: 'CadastroPage',
  data() {
    return {
      usuario: {
        nome: '',
        endereco: '',
        prestador: false,
        cliente: false,
        email: '',
        senha: '',
        confirmarSenha: '',
      },
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  methods: {
    async cadastrarUsuario() {
      // Validar força da senha
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;
      if (!passwordRegex.test(this.usuario.senha)) {
        this.toast.error('A senha deve ter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas e números.');
        return;
      }

      // Verificar se as senhas coincidem
      if (this.usuario.senha !== this.usuario.confirmarSenha) {
        this.toast.error('As senhas não coincidem.');
        return;
      }

      // Verificar se pelo menos um tipo foi selecionado
      if (!this.usuario.prestador && !this.usuario.cliente) {
        this.toast.error('Selecione pelo menos um tipo de usuário.');
        return;
      }

      try {
        const response = await api.post('/usuarios/cadastrar', {
          nome: this.usuario.nome,
          endereco: this.usuario.endereco,
          prestador: this.usuario.prestador,
          cliente: this.usuario.cliente,
          email: this.usuario.email,
          senha: this.usuario.senha,
          confirmarSenha: this.usuario.confirmarSenha,
        });

        this.toast.success(response.data.message);

        // Limpar formulário
        this.usuario = {
          nome: '',
          endereco: '',
          prestador: false,
          cliente: false,
          email: '',
          senha: '',
          confirmarSenha: '',
        };

        setTimeout(() => {
          this.$router.push('/login');
        }, 2000);
      } catch (error) {
        if (error.response && error.response.data) {
          this.toast.error(error.response.data.message);
        } else {
          this.toast.error('Erro ao cadastrar usuário. Tente novamente.');
        }
      }
    },
  },
};
</script>

<style scoped>
.register-container {
  max-width: 500px;
  margin: 50px auto;
}

.register-box {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.input-group {
  margin-bottom: 15px;
}

.input-field {
  width: 95.5%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.checkbox-group {
  display: flex;
  gap: 20px;
}

.register-btn {
  width: 100%;
  padding: 10px;
  background-color: #ffc107;
  color: white;
  font-size: large;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.register-btn:hover {
  background-color: #257bb8;
}
</style>