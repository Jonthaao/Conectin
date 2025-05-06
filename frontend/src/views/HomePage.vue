<template>
  <div class="home-container">
    <!-- Navbar -->
    <!-- <nav class="navbar">
      <div class="logo-container">
        <span class="logo-icon">
          <i>C</i>
        </span>
        <a href="#" class="logo">Conectin</a>
      </div>
    </nav> -->
    
    <!-- Seção Principal -->
    <div class="header-section">
      <div class="header-text">
        <h1>Encontre o Profissional Ideal</h1>
        <p>Conecte-se com trabalhadores qualificados na sua região!</p>
      </div>
    </div>

    <!-- Seção de Categorias -->
    <div class="categories-section">
      <h2 class="section-title">Categorias</h2>
      <input
        v-model="searchCategory"
        type="text"
        placeholder="Busque por categoria (ex.: Jardineiro, Pedreiro...)"
        class="category-search"
      />
      <div class="categories-grid">
        <div
          v-for="category in filteredCategories"
          :key="category.id"
          class="category-card"
          @click="openRegionModal(category)"
        >
          <div class="category-icon">
            <!-- Ícone baseado na categoria (usando emoji como placeholder) -->
            {{ getCategoryIcon(category.nome) }}
          </div>
          <h3>{{ category.nome }}</h3>
        </div>
      </div>
    </div>

    <!-- Modal para escolha da cidade -->
    <div v-if="showRegionModal" class="modal-overlay">
      <div class="modal-content">
        <h2>Selecione sua cidade</h2>
        <input
          v-model="searchCity"
          type="text"
          placeholder="Digite para filtrar cidades..."
          class="region-input"
        />
        <div class="cities-list">
          <div
            v-for="city in filteredCities"
            :key="city.id"
            class="city-option"
            @click="selectCity(city)"
          >
            {{ city.nome }}
          </div>
        </div>
        <div class="modal-buttons">
          <button @click="closeRegionModal" class="cancel-btn">Cancelar</button>
        </div>
      </div>
    </div>
    
    <!-- Footer -->
    <footer class="footer">
      <div class="footer-logo">Conectin</div>
      <p class="footer-text">Conectando profissionais e clientes de forma simples e eficiente.</p>
      <div class="footer-links">
        <a href="#" class="footer-link">Sobre nós</a>
        <a href="#" class="footer-link">Como funciona</a>
        <a href="#" class="footer-link">Contato</a>
      </div>
      <p class="copyright">© 2025 Conectin. Todos os direitos reservados.</p>
    </footer>
  </div>
</template>

<script>
// Importando o axios
import axios from 'axios';

export default {
  name: 'HomePage',
  data() {
    return {
      searchCategory: '',
      // Inicializando categories e cities como arrays vazios
      categories: [],
      cities: [],
      showRegionModal: false,
      selectedCategory: null,
      selectedRegion: '',
      searchCity: '',
    };
  },
  computed: {
    filteredCategories() {
      // Filtrando com base em category.nome
      return this.categories.filter((category) =>
        // Garante que category.nome existe antes de chamar toLowerCase()
        category.nome && category.nome.toLowerCase().includes(this.searchCategory.toLowerCase())
      );
    },
    filteredCities() {
      // Filtrando com base em city.nome
      return this.cities.filter((city) =>
        // Garante que city.nome existe antes de chamar toLowerCase()
        city.nome && city.nome.toLowerCase().includes(this.searchCity.toLowerCase())
      );
    },
  },
  methods: {
    openRegionModal(category) {
      this.selectedCategory = category;
      this.showRegionModal = true;
      this.searchCity = ''; // Limpa a busca de cidade ao abrir o modal
    },
    closeRegionModal() {
      this.showRegionModal = false;
      this.selectedRegion = ''; // Pode manter ou remover, dependendo do uso futuro
      this.selectedCategory = null;
      this.searchCity = ''; // Limpa a busca de cidade ao fechar
    },
    selectCity(city) {
      // Salva o nome da cidade e da categoria no localStorage
      localStorage.setItem('selectedCity', city.nome);
      // Verifica se selectedCategory não é nulo antes de acessar 'nome'
      if (this.selectedCategory) {
        localStorage.setItem('selectedCategory', this.selectedCategory.nome);
      } else {
        console.warn('Categoria selecionada é nula ao tentar salvar no localStorage.');
        // Pode remover a chave antiga ou deixar como está, dependendo do comportamento desejado
        localStorage.removeItem('selectedCategory');
      }

      // Redirecionar para a página de trabalhadores/ranking (ajuste o path conforme necessário)
      // Certifique-se de que this.selectedCategory não é nulo
      if (this.selectedCategory) {
        this.$router.push({
          path: '/ranking-prestadores', // Ou '/trabalhadores' conforme sua estrutura de rotas
          query: {
            categoria: this.selectedCategory.nome,
            cidade: city.nome,
          },
        });
      } else {
         console.error('Não é possível redirecionar sem uma categoria selecionada.');
      }

      this.closeRegionModal(); // Fecha o modal após a seleção
    },

    // Método para buscar categorias da API
    async fetchCategories() {
      try {
        const response = await axios.get('http://localhost:8080/api/categorias');
        // Assume que a API retorna um array de objetos { id: ..., nome: ... }
        this.categories = response.data;
      } catch (error) {
        console.error('Erro ao buscar categorias:', error);
        // Opcional: Adicionar feedback para o usuário sobre o erro
      }
    },

    // Método para buscar cidades da API
    async fetchCities() {
      try {
        // Ajuste o endpoint se necessário
        const response = await axios.get('http://localhost:8080/api/cidades');
        // Assume que a API retorna um array de objetos { id: ..., nome: ... }
        this.cities = response.data;
      } catch (error) {
        console.error('Erro ao buscar cidades:', error);
        // Opcional: Adicionar feedback para o usuário sobre o erro
      }
    },
    
    // Método para selecionar um ícone baseado no nome da categoria
    getCategoryIcon(categoryName) {
      if (!categoryName) return "🔍";
      
      const categoryIcons = {
        'pedreiro': '🧱',
        'pintor': '🎨',
        'encanador': '🔧',
        'eletricista': '⚡',
        'faxineira': '🧹',
        'lavagem de estofados e tapetes': '🛋️',
        'jardineiro': '🌳',
        'técnico em ar condicionado': '❄️',
        'dedetizador': '🐜',
        'vidraceiro': '🪟',
        'serralheiro': '🔩',
        'marceneiro': '🪑'
};
      
      // Verifica se existe um ícone para a categoria, caso contrário retorna um ícone padrão
      const normalizedCategory = categoryName.toLowerCase();
      for (const [key, icon] of Object.entries(categoryIcons)) {
        if (normalizedCategory.includes(key)) {
          return icon;
        }
      }
      
      return "🔍"; // Ícone padrão
    }
  },
  // Hook mounted para buscar os dados quando o componente for montado
  mounted() {
    this.fetchCategories();
    this.fetchCities();
  },
};
</script>

<style scoped>
:root {
  --primary-color: #257BB8;
  --secondary-color: #F4B400;
  --text-light: #FFFFFF;
  --text-dark: #333333;
  --background-light: #F5F5F5;
  --border-radius: 10px;
  --box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', 'Roboto', sans-serif;
}

/* Hero Section */
.header-section {
  background: linear-gradient(135deg, #257BB8 0%, #1557B0 100%);
  padding: 60px 20px;
  text-align: center;
  color: #FFFFFF;
  position: relative;
  overflow: hidden;
}

.header-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M50,50 m-40,0 a40,40 0 1,0 80,0 a40,40 0 1,0 -80,0' fill='none' stroke='%23F4B400' stroke-width='1' opacity='0.2'/%3E%3C/svg%3E");
  background-size: 150px 150px;
  opacity: 0.15;
}

.header-text {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header-text h1 {
  font-size: 3rem;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.header-text p {
  font-size: 1.5rem;
  margin-bottom: 30px;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.search-button {
  background-color: #F4B400;
  color: #333333;
  border: none;
  padding: 15px 30px;
  font-size: 1.2rem;
  font-weight: bold;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.search-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}

/* Categories Section */
.categories-section {
  max-width: 1000px;
  margin: 60px auto;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  color: #257BB8;
  font-size: 2rem;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background-color: #F4B400;
  border-radius: 2px;
}

.category-search {
  padding: 15px 20px;
  width: 100%;
  max-width: 600px;
  margin: 0 auto 40px;
  display: block;
  font-size: 1.1rem;
  border: 2px solid #257BB8;
  border-radius: 30px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.category-search:focus {
  outline: none;
  border-color: #F4B400;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.category-card {
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  padding: 25px 20px;
  text-align: center;
  border: 1px solid rgba(37, 123, 184, 0.2);
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  border-color: #257BB8;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background-color: #257BB8;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.category-card:hover::before {
  transform: scaleX(1);
}

.category-icon {
  width: 60px;
  height: 60px;
  background-color: rgba(37, 123, 184, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  color: #257BB8;
  font-size: 1.8rem;
  transition: all 0.3s ease;
}

.category-card:hover .category-icon {
  background-color: #257BB8;
  color: white;
  transform: scale(1.1);
}

.category-card h3 {
  font-size: 1.3rem;
  color: #333333;
  margin: 0;
  transition: all 0.3s ease;
}

.category-card:hover h3 {
  color: #257BB8;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 450px;
  position: relative;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-content h2 {
  color: #257BB8;
  margin-bottom: 20px;
  text-align: center;
  font-size: 1.8rem;
}

.region-input {
  padding: 12px 20px;
  width: 100%;
  font-size: 1.1rem;
  border: 2px solid #257BB8;
  border-radius: 30px;
  margin: 20px 0;
  transition: all 0.3s ease;
}

.region-input:focus {
  outline: none;
  border-color: #F4B400;
}

.cities-list {
  max-height: 250px;
  overflow-y: auto;
  margin-bottom: 30px;
  border: 1px solid rgba(37, 123, 184, 0.3);
  border-radius: 8px;
  scrollbar-width: thin;
  scrollbar-color: #257BB8 #f1f1f1;
}

.cities-list::-webkit-scrollbar {
  width: 8px;
}

.cities-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.cities-list::-webkit-scrollbar-thumb {
  background: #257BB8;
  border-radius: 4px;
}

.city-option {
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(37, 123, 184, 0.1);
}

.city-option:last-child {
  border-bottom: none;
}

.city-option:hover {
  background-color: #F4B400;
  color: #333333;
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.cancel-btn {
  padding: 12px 25px;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  background-color: #257BB8;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.cancel-btn:hover {
  background-color: #1557b0;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

/* Footer */
.footer {
  background-color: #257BB8;
  color: #FFFFFF;
  padding: 40px 20px;
  text-align: center;
  margin-top: auto;
}

.footer-logo {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 20px;
}

.footer-text {
  margin-bottom: 20px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.footer-link {
  color: #FFFFFF;
  text-decoration: none;
  transition: all 0.3s ease;
}

.footer-link:hover {
  color: #F4B400;
}

.copyright {
  font-size: 0.9rem;
  opacity: 0.8;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .header-text h1 {
    font-size: 2.2rem;
  }
  
  .header-text p {
    font-size: 1.2rem;
  }
  
  .categories-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
  
  .modal-content {
    width: 90%;
    max-width: 450px;
  }
  
  .footer-links {
    flex-direction: column;
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .header-section {
    padding: 40px 15px;
  }
  
  .header-text h1 {
    font-size: 1.8rem;
  }
  
  .categories-grid {
    grid-template-columns: 1fr;
  }
  
  .search-button {
    padding: 12px 25px;
    font-size: 1rem;
  }
}
</style>