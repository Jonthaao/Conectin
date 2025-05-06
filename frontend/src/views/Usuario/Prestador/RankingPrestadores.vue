<template>
  <div class="ranking-container">
    <div class="ranking-box">
      <!-- Header com logo e título -->
      <div class="ranking-header">
        <div class="logo-container">
        </div>
        <h1>Ranking de Prestadores</h1>
      </div>
      
      <p class="intro-text">
        Conheça os melhores prestadores da plataforma. Use os filtros para refinar por categoria e cidade.
        Clique em um prestador para ver mais detalhes!
      </p>

      <!-- Seção de Filtros -->
      <div class="filtros" v-if="categoria || cidade">
        <div class="filtro-info">
          <span class="filtro-label">Filtrando por:</span>
          <span v-if="categoria" class="filtro-valor">Categoria: {{ categoria }}</span>
          <span v-if="cidade" class="filtro-valor">Cidade: {{ cidade }}</span>
        </div>
        <button class="limpar-filtro" @click="limparFiltros">Limpar Filtros</button>
      </div>
      <p v-else class="intro-text filtro-status">Nenhum filtro aplicado. Mostrando ranking geral.</p>

      <!-- Indicador de Carregamento -->
      <div v-if="carregando" class="carregando">
        <div class="spinner"></div>
        <p>Carregando prestadores...</p>
      </div>

      <!-- Mensagem de Sem Resultados -->
      <div v-else-if="prestadoresFiltrados.length === 0" class="sem-resultados">
        <div class="empty-state-icon">🔍</div>
        <p>Nenhum prestador encontrado com os filtros selecionados.</p>
        <!-- <button v-if="categoria || cidade" class="limpar-filtro" @click="limparFiltros">Limpar Filtros</button> -->
      </div>

      <!-- Container de Prestadores -->
      <div v-else class="prestadores-container">
        <div class="prestador-card" v-for="(prestador, index) in prestadoresFiltrados" :key="prestador.id">
          <div class="prestador-ranking-badge">{{ index + 1 }}</div>
          <div class="prestador-info">
            <h3>{{ prestador.nome }}</h3>
            <div class="prestador-details">
              <p class="categorias" v-if="prestador.categorias && prestador.categorias.length">
                <span class="detail-icon">📋</span>
                <span class="detail-label">Categorias:</span> 
                {{ prestador.categorias.map(c => c.nome).join(', ') }}
              </p>
              <p class="cidade">
                <span class="detail-icon">📍</span>
                <span class="detail-label">Cidade:</span> 
                {{ prestador.cidade || 'Não informada' }}
              </p>
              <p class="avaliacao">
                <span class="detail-icon">⭐</span>
                <span class="detail-label">Avaliação:</span>
                <span class="rating-value">
                  {{ prestador.avaliacaoMedia ? prestador.avaliacaoMedia.toFixed(1) : 'Sem avaliações' }}
                </span>
              </p>
              <p class="disponibilidade">
                <span class="detail-icon">🕒</span>
                <span class="detail-label">Disponibilidade:</span> 
                {{ prestador.disponibilidade || 'Não informado' }}
              </p>
            </div>
          </div>
          <router-link :to="{ name: 'PerfilPrestador', params: { id: prestador.id }}" class="ver-perfil-btn">
            Ver Perfil
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

export default {
  name: 'RankingPrestadores',
  data() {
    return {
      prestadores: [], // Lista original da API
      categoria: '',   // Filtro de categoria
      cidade: '',      // Filtro de cidade
      carregando: true, // Estado de carregamento
    };
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  computed: {
    // Lógica de filtragem
    prestadoresFiltrados() {
      // Se não há filtros, retorna todos os prestadores do ranking
      if (!this.categoria && !this.cidade) {
        // Mantém a ordenação original da API (ranking)
        return this.prestadores;
      }

      // Aplica os filtros
      return this.prestadores.filter(prestador => {
        const matchCategoria = !this.categoria ||
          (prestador.categorias && prestador.categorias.some(
            c => c.nome.toLowerCase() === this.categoria.toLowerCase()
          ));

        // Garante que prestador.cidade existe antes de comparar
        const matchCidade = !this.cidade ||
          (prestador.cidade && prestador.cidade.toLowerCase() === this.cidade.toLowerCase());

        return matchCategoria && matchCidade;
      });
    }
  },
  // Usar created para buscar filtros antes da montagem
  created() {
    this.atualizarFiltrosDaRota();
    this.fetchPrestadores();
  },
  watch: {
    // Vigiar mudanças na rota para atualizar filtros
    '$route.query': {
      handler() {
        this.atualizarFiltrosDaRota();
      },
      deep: true
    }
  },
  methods: {
    // Função auxiliar para centralizar a lógica de pegar filtros
    atualizarFiltrosDaRota() {
        // Recuperar os parâmetros da URL ou localStorage
        this.categoria = this.$route.query.categoria || localStorage.getItem('selectedCategory') || '';
        this.cidade = this.$route.query.cidade || localStorage.getItem('selectedCity') || '';
    },

    async fetchPrestadores() {
      try {
        this.carregando = true; // Inicia carregamento
        const response = await api.get('/prestadores/ranking'); // Busca o ranking ordenado

        // Busca categorias e simula/adiciona cidade
        this.prestadores = await Promise.all(response.data.map(async (prestador) => {
          const categoriasResponse = await api.get(`/prestadores/${prestador.id}/categorias`);

          // Simulação de Cidade
          const cidades = [
            'Armazém', 'Braço do Norte', 'Capivari de Baixo', 'Grão-Pará', 'Gravatal',
            'Imaruí', 'Imbituba', 'Jaguaruna', 'Laguna', 'Paulo Lopes', 'Rio Fortuna',
            'São Bonifácio', 'São Martinho', 'Santa Rosa de Lima', 'Tubarão'
          ];
          const randomCidade = cidades[Math.floor(Math.random() * cidades.length)];

          return {
            ...prestador,
            categorias: categoriasResponse.data,
            cidade: prestador.cidade || randomCidade // Usa a cidade do prestador se existir, senão usa a simulada
          };
        }));

      } catch (error) {
        if (error.response && error.response.data) {
          this.toast.error(error.response.data.message);
        } else {
          this.toast.error('Erro ao carregar prestadores.');
        }
        this.prestadores = []; // Limpa em caso de erro para evitar mostrar dados antigos
      } finally {
        this.carregando = false; // Finaliza carregamento
      }
    },

    // Método para limpar filtros
    limparFiltros() {
      localStorage.removeItem('selectedCategory');
      localStorage.removeItem('selectedCity');
      this.categoria = '';
      this.cidade = '';

      // Navegar para a mesma página sem parâmetros de query
      this.$router.push({ path: this.$route.path });
    }
  }
};
</script>

<style scoped>
/* Variáveis de cores baseadas na logo Conectin */
:root {
  --conectin-blue: #1A77B5;
  --conectin-blue-light: #2089cf;
  --conectin-blue-dark: #155e8f;
  --conectin-yellow: #F4B400;
  --conectin-yellow-dark: #e1a300;
  --conectin-text: #333333;
  --conectin-white: #FFFFFF;
  --conectin-gray-light: #f5f7fa;
  --conectin-gray: #e0e0e0;
  --conectin-red: #e74c3c;
}

/* Estilos globais */
.ranking-container {
  max-width: 900px;
  margin: 20px auto;
  padding: 0 15px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.ranking-box {
  padding: 30px;
  background-color: var(--conectin-white);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(26, 119, 181, 0.15);
  position: relative;
  overflow: hidden;
}

/* Estilos do cabeçalho */
.ranking-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 25px;
  position: relative;
}

/* Logo estilizada */
.logo-container {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.conectin-logo {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--conectin-blue);
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
  position: relative;
  z-index: 2;
}

.conectin-nodes {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
}

.node {
  position: absolute;
  width: 10px;
  height: 10px;
  background-color: var(--conectin-blue-light);
  border-radius: 50%;
  border: 2px solid white;
}

.n1 { top: 15%; left: 35%; }
.n2 { top: 45%; left: 25%; }
.n3 { top: 65%; left: 40%; }
.n4 { top: 25%; left: 65%; }
.n5 { top: 55%; left: 70%; }

.user-node {
  width: 16px;
  height: 16px;
  background-color: var(--conectin-yellow);
  border: 3px solid white;
  box-shadow: 0 0 5px rgba(0,0,0,0.2);
  top: 10%; 
  left: 50%;
}

h1 {
  font-size: 2.2rem;
  color: var(--conectin-blue);
  margin: 10px 0;
  text-align: center;
  font-weight: 600;
  position: relative;
}

h1:after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background-color: var(--conectin-yellow);
  border-radius: 2px;
}

.intro-text {
  text-align: center;
  margin-bottom: 30px;
  color: var(--conectin-text);
  line-height: 1.6;
  font-size: 1.05rem;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.filtro-status {
  color: #666;
  font-style: italic;
  margin-bottom: 20px;
}

/* Estilos dos Filtros */
.filtros {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 15px 20px;
  background-color: rgba(26, 119, 181, 0.05);
  border-radius: 10px;
  border-left: 4px solid var(--conectin-blue);
  gap: 15px;
}

.filtro-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filtro-label {
  font-weight: bold;
  color: var(--conectin-blue);
}

.filtro-valor {
  background-color: var(--conectin-white);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.9rem;
  border: 1px solid var(--conectin-blue-light);
  color: var(--conectin-blue);
  box-shadow: 0 1px 3px rgba(26, 119, 181, 0.15);
}

.limpar-filtro {
  background-color: #257BB8;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(231, 76, 60, 0.2);
}

.limpar-filtro:hover {
  background-color: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.3);
}

.limpar-filtro:active {
  transform: translateY(0);
}

/* Estilos de Carregamento */
.carregando {
  text-align: center;
  padding: 50px 20px;
  color: var(--conectin-blue);
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(26, 119, 181, 0.2);
  border-top: 4px solid var(--conectin-blue);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Sem resultados */
.sem-resultados {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-state-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  opacity: 0.7;
}

.sem-resultados p {
  margin-bottom: 20px;
}

/* Estilos dos Cards de Prestadores */
.prestadores-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.prestador-card {
  border: 1px solid var(--conectin-gray);
  border-radius: 10px;
  padding: 20px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  background-color: var(--conectin-white);
  transition: all 0.3s ease;
  gap: 15px;
  position: relative;
  overflow: hidden;
}

.prestador-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(26, 119, 181, 0.15);
  border-color: var(--conectin-blue-light);
}

.prestador-ranking-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: var(--conectin-blue);
  color: white;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border-bottom-right-radius: 10px;
}

.prestador-info {
  flex: 1;
  min-width: 250px;
  padding-left: 10px;
}

.prestador-info h3 {
  margin: 0 0 15px 0;
  color: var(--conectin-blue);
  font-size: 1.3rem;
  border-bottom: 2px solid var(--conectin-yellow);
  padding-bottom: 8px;
  display: inline-block;
}

.prestador-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Estilos para os detalhes do prestador */
.categorias, .cidade, .avaliacao, .disponibilidade {
  margin: 0;
  font-size: 0.95rem;
  color: var(--conectin-text);
  line-height: 1.5;
  display: flex;
  align-items: center;
  gap: 5px;
}

.detail-icon {
  font-size: 1.1rem;
  min-width: 20px;
  text-align: center;
}

.detail-label {
  font-weight: 600;
  color: #555;
  margin-right: 5px;
}

.rating-value {
  color: var(--conectin-blue);
  font-weight: 600;
}

/* Estilo do botão Ver Perfil */
.ver-perfil-btn {
  background-color: #257BB8;
  color: #FFFFFF;
  text-decoration: none;
  padding: 10px 24px;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
  /* box-shadow: 0 3px 6px #257BB8; */
  position: relative;
  overflow: hidden;
}

.ver-perfil-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.7s;
}

.ver-perfil-btn:hover {
  background-color: #F4B400;
  color: #fff;
  text-decoration: none;
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(244, 180, 0, 0.4);
}

.ver-perfil-btn:hover:before {
  left: 100%;
}

.ver-perfil-btn:active {
  transform: translateY(0);
}

/* Media queries para responsividade */
@media (max-width: 768px) {
  .ranking-box {
    padding: 20px 15px;
  }
  
  .prestador-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .ver-perfil-btn {
    width: 100%;
    margin-top: 10px;
  }
  
  .filtros {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .limpar-filtro {
    width: 100%;
  }
}
</style>