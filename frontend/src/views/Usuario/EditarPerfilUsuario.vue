<template>
  <div class="editar-perfil-container">
    <div class="editar-perfil-box">
      <h1>Editar Perfil</h1>
      <form @submit.prevent="salvarPerfil">
        <!-- Dados Gerais -->
        <div class="input-group">
          <label for="nome">Nome:</label>
          <input v-model="usuario.nome" type="text" id="nome" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="endereco">Endereço:</label>
          <input v-model="usuario.endereco" type="text" id="endereco" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="email">Email:</label>
          <input v-model="usuario.email" type="email" id="email" required class="input-field" />
        </div>
        <div class="input-group">
          <label for="fotoPerfil">Foto de Perfil:</label>
          <input type="file" id="fotoPerfil" accept="image/*" @change="handleFotoUpload" class="input-field" />
          <img v-if="usuario.fotoPerfil" :src="usuario.fotoPerfil" alt="Foto de Perfil" class="foto-preview" />
        </div>

        <!-- Dados do Prestador -->
        <div v-if="usuario.prestador" class="prestador-section">
          <h3>Dados do Prestador</h3>
          <div class="input-group">
            <label for="descricao">Descrição:</label>
            <textarea v-model="usuario.descricao" id="descricao" class="input-field"></textarea>
          </div>
          <div class="input-group">
            <label for="disponibilidade">Disponibilidade:</label>
            <input v-model="usuario.disponibilidade" type="text" id="disponibilidade" class="input-field" />
          </div>

          <!-- Categorias (Serviços) -->
          <div class="input-group">
            <label>Categorias (Serviços):</label>
            <select multiple v-model="usuario.categoriasSelecionadas" class="input-field">
              <option v-for="categoria in categoriasDisponiveis" :key="categoria.id" :value="categoria.id">
                {{ categoria.nome }}
              </option>
            </select>
          </div>

          <!-- Cidades Atendidas -->
          <div class="input-group">
            <label>Cidades Atendidas:</label>
            <div class="cidades-selecionadas">
              <div v-if="usuario.cidadesSelecionadas.length === 0" class="sem-cidades">
                Nenhuma cidade selecionada.
              </div>
              <div v-else class="cidade-item" v-for="cidadeId in usuario.cidadesSelecionadas" :key="cidadeId">
                <span>{{ getCidadeNome(cidadeId) }}</span>
                <button type="button" @click="removerCidade(cidadeId)" class="remove-btn">Remover</button>
              </div>
            </div>
            <div class="adicionar-cidade">
              <select v-model="novaCidade" class="input-field">
                <option value="" disabled>Selecione uma cidade</option>
                <option v-for="cidade in cidadesDisponiveis" :key="cidade.id" :value="cidade.id">
                  {{ cidade.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCidade" :disabled="!novaCidade" class="add-btn">Adicionar Cidade</button>
            </div>
          </div>

          <!-- Portfólio -->
          <div class="portfolio-section">
            <h4>Portfólio</h4>
            <div v-for="(item, index) in usuario.portfolios" :key="index" class="portfolio-item">
              <input type="file" accept="image/*" @change="handlePortfolioUpload($event, index)" class="input-field" />
              <input v-model="item.descricao" type="text" placeholder="Descrição" class="input-field" />
              <button type="button" @click="removerPortfolio(index)" class="remove-btn">Remover</button>
              <img v-if="item.urlImagem" :src="item.urlImagem" alt="Portfolio" class="portfolio-preview" />
            </div>
            <button type="button" @click="adicionarPortfolio" class="add-btn">Adicionar Item ao Portfólio</button>
          </div>
        </div>

        <!-- Tipos de Usuário -->
        <div class="input-group">
          <label>Tipo de Usuário:</label>
          <div class="checkbox-group">
            <div>
              <input type="checkbox" id="prestador" v-model="usuario.prestador" />
              <label for="prestador">Prestador</label>
            </div>
            <div>
              <input type="checkbox" id="cliente" v-model="usuario.cliente" />
              <label for="cliente">Cliente</label>
            </div>
          </div>
        </div>

        <!-- Alterar Senha -->
        <div class="input-group">
          <button type="button" @click="toggleAlterarSenha" class="alterar-senha-btn">
            {{ alterarSenhaVisivel ? 'Cancelar Alteração de Senha' : 'Alterar Senha' }}
          </button>
        </div>
        <div v-if="alterarSenhaVisivel" class="senha-section">
          <div class="input-group">
            <label for="senhaAtual">Senha Atual:</label>
            <input v-model="usuario.senhaAtual" type="password" id="senhaAtual" required class="input-field" />
          </div>
          <div class="input-group">
            <label for="senha">Nova Senha:</label>
            <input v-model="usuario.senha" type="password" id="senha" required class="input-field" />
          </div>
          <div class="input-group">
            <label for="confirmarSenha">Confirmar Nova Senha:</label>
            <input v-model="usuario.confirmarSenha" type="password" id="confirmarSenha" required class="input-field" />
          </div>
        </div>

        <button type="submit" class="save-btn">Salvar Perfil</button>
      </form>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

export default {
  name: 'EditarPerfil',
  setup() {
    const toast = useToast();
    const userStore = useUserStore();
    const router = useRouter();
    return { toast, userStore, router };
  },
  data() {
    return {
      usuario: {
        nome: '',
        endereco: '',
        email: '',
        prestador: false,
        cliente: false,
        fotoPerfil: '',
        descricao: '',
        disponibilidade: '',
        portfolios: [],
        categoriasSelecionadas: [],
        cidadesSelecionadas: [],
        senhaAtual: '',
        senha: '',
        confirmarSenha: '',
      },
      categoriasDisponiveis: [],
      cidadesDisponiveis: [],
      novaCidade: '', // Para o select de adicionar cidade
      alterarSenhaVisivel: false,
    };
  },
  mounted() {
    this.carregarPerfil();
    this.carregarCategorias();
    this.carregarCidades();
  },
  methods: {
    async carregarPerfil() {
      try {
        const response = await api.get('/usuarios/perfil', {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        const data = response.data;
        this.usuario = {
          nome: data.nome,
          endereco: data.endereco,
          email: data.email,
          prestador: data.tipos.includes('PRESTADOR'),
          cliente: data.tipos.includes('CLIENTE'),
          fotoPerfil: data.foto,
          descricao: data.prestador?.descricao || '',
          disponibilidade: data.prestador?.disponibilidade || '',
          portfolios: data.prestador?.portfolios || [],
          categoriasSelecionadas: data.prestador?.categorias?.map(c => c.id) || [],
          cidadesSelecionadas: data.prestador?.cidades?.map(c => c.id) || [],
          senhaAtual: '',
          senha: '',
          confirmarSenha: '',
        };
      } catch (error) {
        this.toast.error('Erro ao carregar perfil.');
      }
    },
    async carregarCategorias() {
      try {
        const response = await api.get('/categorias');
        this.categoriasDisponiveis = response.data;
      } catch (error) {
        this.toast.error('Erro ao carregar categorias.');
      }
    },
    async carregarCidades() {
      try {
        const response = await api.get('/cidades');
        this.cidadesDisponiveis = response.data;
      } catch (error) {
        this.toast.error('Erro ao carregar cidades.');
      }
    },
    handleFotoUpload(event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.usuario.fotoPerfil = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },
    handlePortfolioUpload(event, index) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.usuario.portfolios[index].urlImagem = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },
    adicionarPortfolio() {
      this.usuario.portfolios.push({ id: null, urlImagem: '', descricao: '' });
    },
    removerPortfolio(index) {
      this.usuario.portfolios.splice(index, 1);
    },
    toggleAlterarSenha() {
      this.alterarSenhaVisivel = !this.alterarSenhaVisivel;
      if (!this.alterarSenhaVisivel) {
        this.usuario.senhaAtual = '';
        this.usuario.senha = '';
        this.usuario.confirmarSenha = '';
      }
    },
    adicionarCidade() {
      if (this.novaCidade && !this.usuario.cidadesSelecionadas.includes(this.novaCidade)) {
        this.usuario.cidadesSelecionadas.push(this.novaCidade);
        this.novaCidade = ''; // Limpar o select após adicionar
      } else {
        this.toast.error('Selecione uma cidade válida ou ela já foi adicionada.');
      }
    },
    removerCidade(cidadeId) {
      this.usuario.cidadesSelecionadas = this.usuario.cidadesSelecionadas.filter(id => id !== cidadeId);
    },
    getCidadeNome(cidadeId) {
      const cidade = this.cidadesDisponiveis.find(c => c.id === cidadeId);
      return cidade ? cidade.nome : 'Cidade Desconhecida';
    },
    async salvarPerfil() {
      if (!this.usuario.prestador && !this.usuario.cliente) {
        this.toast.error('Selecione pelo menos um tipo de usuário.');
        return;
      }

      if (this.alterarSenhaVisivel) {
        if (!this.usuario.senhaAtual) {
          this.toast.error('A senha atual é obrigatória para alterar a senha.');
          return;
        }
        if (!this.usuario.senha) {
          this.toast.error('A nova senha é obrigatória.');
          return;
        }
        if (this.usuario.senha !== this.usuario.confirmarSenha) {
          this.toast.error('As senhas não coincidem.');
          return;
        }
      }

      try {
        const dadosParaEnviar = {
          nome: this.usuario.nome,
          endereco: this.usuario.endereco,
          email: this.usuario.email,
          tipos: [
            ...(this.usuario.cliente ? ['CLIENTE'] : []),
            ...(this.usuario.prestador ? ['PRESTADOR'] : []),
          ],
          fotoPerfil: this.usuario.fotoPerfil,
          prestador: this.usuario.prestador ? {
            descricao: this.usuario.descricao,
            disponibilidade: this.usuario.disponibilidade,
            categorias: this.usuario.categoriasSelecionadas,
            cidades: this.usuario.cidadesSelecionadas,
            portfolios: this.usuario.portfolios,
          } : null,
        };

        if (this.alterarSenhaVisivel) {
          dadosParaEnviar.senhaAtual = this.usuario.senhaAtual;
          dadosParaEnviar.senha = this.usuario.senha;
        }

        const response = await api.put(`/usuarios/editar/${this.userStore.user.id}`, dadosParaEnviar, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        this.userStore.user = { ...this.userStore.user, ...this.usuario };
        localStorage.setItem('usuarioLogado', JSON.stringify(this.userStore.user));
        this.toast.success(response.data.message);
        this.router.push({ name: 'Perfil' });
      } catch (error) {
        this.toast.error(error.response?.data?.message || 'Erro ao salvar perfil.');
      }
    },
  },
};
</script>

<style scoped>
.editar-perfil-container {
  max-width: 600px;
  margin: 50px auto;
}

.editar-perfil-box {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.input-group {
  margin-bottom: 15px;
}

.input-field {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.checkbox-group {
  display: flex;
  gap: 20px;
}

.foto-preview, .portfolio-preview {
  max-width: 100px;
  margin-top: 10px;
}

.prestador-section {
  margin-top: 20px;
}

.senha-section {
  margin-top: 20px;
}

.portfolio-section {
  margin-top: 20px;
}

.portfolio-item {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cidades-selecionadas {
  margin-bottom: 10px;
}

.sem-cidades {
  color: #888;
}

.cidade-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px;
  border-bottom: 1px solid #eee;
}

.adicionar-cidade {
  display: flex;
  gap: 10px;
  align-items: center;
}

.remove-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.remove-btn:hover {
  background-color: #c82333;
}

.add-btn {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #218838;
}

.add-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.alterar-senha-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.alterar-senha-btn:hover {
  background-color: #5a6268;
}

.save-btn {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #0056b3;
}
</style>