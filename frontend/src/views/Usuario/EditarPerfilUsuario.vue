<template>
  <div class="editar-perfil-container">
    <div class="editar-perfil-box">
      <h1>Editar Perfil</h1>
      <form @submit.prevent="salvarPerfil">
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
          <input
            type="file"
            id="fotoPerfil"
            accept="image/*"
            @change="handleFotoUpload"
            class="input-field"
          />
        
          <div v-if="usuario.fotoPerfil" class="relative inline-block mt-2">
            <img
              :src="usuario.fotoPerfil"
              alt="Foto de Perfil"
              class="foto-preview"
            />
            <!-- Botão da lixeira -->
            <Trash2
              class="absolute top-1 right-1 w-5 h-5 bg-white rounded-full p-1 text-red-500 hover:bg-red-100 cursor-pointer"
              @click="removerFoto"
              title="Remover foto"
            />
          </div>
        </div>

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

          <div class="input-group">
            <label>Categorias (Serviços):</label>
            <div class="categorias-selecionadas">
              <div v-if="usuario.categoriasSelecionadas.length === 0" class="sem-categorias">
                Nenhuma categoria selecionada.
              </div>
              <div v-else class="tag-container">
                <span v-for="categoriaId in usuario.categoriasSelecionadas" :key="categoriaId" class="tag-item">
                  <span class="tag-name">{{ getCategoriaNome(categoriaId) }}</span>
                  <span class="trash-icon" @click="removerCategoria(categoriaId)" title="Remover">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="#e74c3c" viewBox="0 0 16 16">
                      <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                      <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
                    </svg>
                  </span>
                </span>
              </div>
            </div>
            <div class="adicionar-categoria">
              <select v-model="novaCategoria" class="input-field">
                <option value="null" disabled>Selecione uma categoria</option>
                <option v-for="categoria in categoriasDisponiveis" :key="categoria.id" :value="categoria.id">
                  {{ categoria.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCategoria" :disabled="!novaCategoria" class="add-btn">Adicionar</button>
            </div>
          </div>

          <div class="input-group">
            <label>Cidades Atendidas:</label>
            <div class="cidades-selecionadas">
              <div v-if="usuario.cidadesSelecionadas.length === 0" class="sem-cidades">
                Nenhuma cidade selecionada.
              </div>
              <div v-else class="tag-container">
                <span v-for="cidadeId in usuario.cidadesSelecionadas" :key="cidadeId" class="tag-item">
                  <span class="tag-name">{{ getCidadeNome(cidadeId) }}</span>
                  <span class="trash-icon">
                   <Trash2
                    class="w-4 h-4 text-red-500 cursor-pointer"
                    @click="removerCidade(cidadeId)"
                    title="Remover"
                  />
                  </span>
                </span>
              </div>
            </div>
            <div class="adicionar-cidade">
              <select v-model="novaCidade" class="input-field">
                <option value="" disabled>Selecione uma cidade</option>
                <option v-for="cidade in cidadesDisponiveis" :key="cidade.id" :value="cidade.id">
                  {{ cidade.nome }}
                </option>
              </select>
              <button type="button" @click="adicionarCidade" :disabled="!novaCidade" class="add-btn">Adicionar</button>
            </div>
          </div>

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
        fotoPerfil: null,
        prestador: false,
        cliente: false,
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
      novaCidade: null,
      novaCategoria: null,
      alterarSenhaVisivel: false,
      fotoPerfilFile: null,
      portfolioFiles: {},
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

        this.usuario.nome = data.nome || '';
        this.usuario.endereco = data.endereco || '';
        this.usuario.email = data.email || '';
        this.usuario.fotoPerfil = data.foto || null;

        const isPrestador = data.tipos?.includes('PRESTADOR') || (data.prestador && typeof data.prestador === 'object');
        const isCliente = data.tipos?.includes('CLIENTE') || false;

        this.usuario.prestador = isPrestador;
        this.usuario.cliente = isCliente;

        if (this.usuario.prestador && data.prestador && typeof data.prestador === 'object') {
          const prestadorData = data.prestador;

          this.usuario.descricao = prestadorData.descricao || '';
          this.usuario.disponibilidade = prestadorData.disponibilidade || '';
          this.usuario.portfolios = (prestadorData.portfolios || []).map(p => ({
            id: p.id || [],
            urlImagem: p.urlImagem || p.imagemUrl || '',
            descricao: p.descricao || ''
          }));
          this.usuario.categoriasSelecionadas = (prestadorData.categorias || []).map(cat => cat.id);
          this.usuario.cidadesSelecionadas = (prestadorData.cidades || []).map(cid => cid.id);
        } else {
          this.usuario.descricao = '';
          this.usuario.disponibilidade = '';
          this.usuario.portfolios = [];
          this.usuario.categoriasSelecionadas = [];
          this.usuario.cidadesSelecionadas = [];
        }

        this.usuario.senhaAtual = '';
        this.usuario.senha = '';
        this.usuario.confirmarSenha = '';
        this.alterarSenhaVisivel = false;

      } catch (error) {
        console.error('Erro ao carregar perfil:', error.response || error);
        this.toast.error(error.response?.data?.message || 'Não foi possível carregar os dados do perfil.');
      }
    },

    async carregarCategorias() {
      try {
        const response = await api.get('/categorias');
        this.categoriasDisponiveis = response.data;
      } catch (error) {
        console.error('Erro ao carregar categorias:', error.response || error);
        this.toast.error('Erro ao carregar lista de categorias.');
      }
    },

    async carregarCidades() {
      try {
        const response = await api.get('/cidades');
        this.cidadesDisponiveis = response.data;
      } catch (error) {
        console.error('Erro ao carregar cidades:', error.response || error);
        this.toast.error('Erro ao carregar lista de cidades.');
      }
    },

    handleFotoUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.fotoPerfilFile = file;
        const reader = new FileReader();
        reader.onload = (e) => {
          this.usuario.fotoPerfil = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        this.fotoPerfilFile = null;
      }
      
    },
    
    

    handlePortfolioUpload(event, index) {
      const file = event.target.files[0];
      if (file) {
        this.portfolioFiles[index] = file;
        const reader = new FileReader();
        reader.onload = (e) => {
          if (!this.usuario.portfolios[index]) {
            this.usuario.portfolios[index] = { id: null, urlImagem: '', descricao: '' };
          }
          this.usuario.portfolios[index].urlImagem = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        delete this.portfolioFiles[index];
      }
    },

    adicionarPortfolio() {
      this.usuario.portfolios.push({ id: null, urlImagem: '', descricao: '' });
    },

    removerPortfolio(index) {
      this.usuario.portfolios.splice(index, 1);
      delete this.portfolioFiles[index];
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
        this.novaCidade = '';
      } else if (this.novaCidade) {
        this.toast.info('Esta cidade já foi adicionada.');
      } else {
        this.toast.error('Selecione uma cidade para adicionar.');
      }
    },

    removerCidade(cidadeId) {
      this.usuario.cidadesSelecionadas = this.usuario.cidadesSelecionadas.filter(id => id !== cidadeId);
    },

    getCidadeNome(cidadeId) {
      const cidade = this.cidadesDisponiveis.find(c => c.id === cidadeId);
      return cidade ? cidade.nome : 'ID: ' + cidadeId;
    },

    adicionarCategoria() {
      if (this.novaCategoria && !this.usuario.categoriasSelecionadas.includes(this.novaCategoria)) {
        this.usuario.categoriasSelecionadas.push(this.novaCategoria);
        this.novaCategoria = '';
      } else if (this.novaCategoria) {
        this.toast.info('Esta categoria já foi adicionada.');
      } else {
        this.toast.error('Selecione uma categoria para adicionar.');
      }
    },

    removerCategoria(categoriaId) {
      this.usuario.categoriasSelecionadas = this.usuario.categoriasSelecionadas.filter(id => id !== categoriaId);
    },

        removerfoto(fotoId) {
      this.usuario.fotoPerfil = this.usuario.fotoPerfil.filter(id => id !== fotoId);
    },

    getCategoriaNome(categoriaId) {
      const categoria = this.categoriasDisponiveis.find(c => c.id === categoriaId);
      return categoria ? categoria.nome : 'ID: ' + categoriaId;
    },

    async salvarPerfil() {
  if (!this.usuario.prestador && !this.usuario.cliente) {
    this.toast.error('Selecione pelo menos um tipo de usuário (Prestador ou Cliente).');
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
    if (this.usuario.senha.length < 6) {
      this.toast.error('A nova senha deve ter pelo menos 6 caracteres.');
      return;
    }
    if (this.usuario.senha !== this.usuario.confirmarSenha) {
      this.toast.error('A nova senha e a confirmação de senha não coincidem.');
      return;
    }
  }

  const dadosParaEnviar = {
    nome: this.usuario.nome,
    endereco: this.usuario.endereco,
    email: this.usuario.email,
    fotoPerfil: this.usuario.fotoPerfil, // Will be updated with the uploaded URL
    prestador: this.usuario.prestador,
    cliente: this.usuario.cliente,
    descricao: this.usuario.prestador ? this.usuario.descricao : null,
    disponibilidade: this.usuario.prestador ? this.usuario.disponibilidade : null,
    categorias: this.usuario.prestador ? this.usuario.cidadesSelecionadas.map(id => ({ id })) : [],
    cidades: this.usuario.prestador ? this.usuario.cidadesSelecionadas.map(id => ({ id })) : [],
    portfolios: this.usuario.prestador ? this.usuario.portfolios.map(p => ({
      id: p.id || null,
      urlImagem: p.urlImagem,
      descricao: p.descricao
    })) : [],
  };

  if (this.alterarSenhaVisivel) {
    dadosParaEnviar.senhaAtual = this.usuario.senhaAtual;
    dadosParaEnviar.senha = this.usuario.senha;
  }

  try {
    if (!this.userStore.user || !this.userStore.user.id) {
      this.toast.error("ID do usuário não encontrado. Por favor, faça login novamente.");
      return;
    }
    const userId = this.userStore.user.id;

    // Upload profile picture if a new file is selected
    if (this.fotoPerfilFile) {
      const formData = new FormData();
      formData.append('file', this.fotoPerfilFile);
      const uploadResponse = await api.post('/upload/foto', formData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'multipart/form-data',
        },
      });
      dadosParaEnviar.fotoPerfil = uploadResponse.data; // Set the returned URL
    }

    // Upload portfolio images if any
    for (let index in this.portfolioFiles) {
      const file = this.portfolioFiles[index];
      const formData = new FormData();
      formData.append('file', file);
      const uploadResponse = await api.post('/upload/foto', formData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'multipart/form-data',
        },
      });
      dadosParaEnviar.portfolios[index].urlImagem = uploadResponse.data;
    }

    // Save the profile
    const response = await api.put(`/usuarios/editar/${userId}`, dadosParaEnviar, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json',
      },
    });

    this.userStore.setUser({
      ...this.userStore.user,
      nome: dadosParaEnviar.nome,
      email: dadosParaEnviar.email,
      endereco: dadosParaEnviar.endereco,
      fotoPerfil: dadosParaEnviar.fotoPerfil,
      prestador: dadosParaEnviar.prestador,
      cliente: dadosParaEnviar.cliente,
    });

    this.toast.success(response.data.message || 'Perfil atualizado com sucesso!');
    this.router.push('/perfil'); // Redirect to profile page after success
  } catch (error) {
    console.error("Erro ao salvar perfil:", error.response || error);
    const errorMessage = error.response?.data?.message ||
      (error.response?.data?.errors ? Object.values(error.response.data.errors).join(', ') : null) ||
      error.response?.data?.error ||
      'Falha ao atualizar o perfil. Verifique os dados e tente novamente.';
    this.toast.error(errorMessage);
  }
},
        removerFoto() {
      this.usuario.fotoPerfil = null;
    },

},

};
</script>

<style scoped>
.editar-perfil-container {
  max-width: 600px;
  margin: 50px auto;
  font-family: 'Inter', sans-serif;
}

.editar-perfil-box {
  padding: 30px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.editar-perfil-box h1 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  font-size: 1.8em;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.input-field {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.input-field:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

textarea.input-field {
  min-height: 100px;
  resize: vertical;
}

.checkbox-group {
  display: flex;
  gap: 25px;
  align-items: center;
}
.checkbox-group div {
  display: flex;
  align-items: center;
}
.checkbox-group input[type="checkbox"] {
  margin-right: 8px;
  width: 18px;
  height: 18px;
  accent-color: #007bff;
}
.checkbox-group label {
  margin-bottom: 0;
  font-weight: normal;
}

.foto-preview,
.portfolio-preview {
  max-width: 120px;
  height: auto;
  margin-top: 12px;
  border-radius: 6px;
  border: 1px solid #ddd;
  padding: 4px;
  background-color: #f9f9f9;
}

.prestador-section,
.senha-section,
.portfolio-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.prestador-section h3,
.portfolio-section h4 {
  margin-bottom: 20px;
  color: #333;
}

.portfolio-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 15px;
  border: 1px solid #e7e7e7;
  border-radius: 6px;
  background-color: #fdfdfd;
}

.cidades-selecionadas,
.categorias-selecionadas {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 6px;
  background-color: #f9f9f9;
  min-height: 40px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.sem-cidades,
.sem-categorias {
  color: #777;
  font-style: italic;
  padding: 8px 0;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  padding: 6px 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  font-size: 0.9em;
  color: #333;
}

.tag-name {
  margin-right: 8px;
}

.trash-icon {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.trash-icon:hover::after {
  content: 'Remover';
  position: absolute;
  top: -30px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #333;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8em;
  white-space: nowrap;
  z-index: 10;
}

.trash-icon:hover svg {
  opacity: 0.8;
}

.adicionar-cidade,
.adicionar-categoria {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 10px;
}
.adicionar-cidade .input-field,
.adicionar-categoria .input-field {
  flex-grow: 1;
}

.remove-btn {
  display: none; /* Oculta o botão original */
}

.add-btn {
  background-color: #2ecc71;
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.add-btn:hover {
  background-color: #27ae60;
}

.add-btn:disabled {
  background-color: #bdc3c7;
  color: #7f8c8d;
  cursor: not-allowed;
  opacity: 0.7;
}

.alterar-senha-btn {
  background-color: #5bc0de;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
  display: inline-block;
  margin-bottom: 10px;
}

.alterar-senha-btn:hover {
  background-color: #31b0d5;
}

.save-btn {
  width: 100%;
  padding: 14px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.1em;
  font-weight: 500;
  transition: background-color 0.2s ease;
  margin-top: 20px;
}

.save-btn:hover {
  background-color: #0056b3;
}
</style>