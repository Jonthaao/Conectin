<template>
    <div class="avaliar-servico-container">
      <div v-if="isLoading" class="loading-spinner">Carregando...</div>
      <div v-if="!isLoading && !userStore.user && solicitacaoIdParam" class="error-message">
        Por favor, <router-link to="/login">faça login</router-link> para avaliar este serviço.
      </div>
      <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>
  
      <div v-if="solicitacao && !isLoading && !errorMsg && userStore.user && podeAvaliar">
        <h2>Avaliar Serviço: {{ solicitacao.descricaoServico || 'Detalhes do Serviço' }}</h2> <!-- Ajustado para nome de campo mais provável -->
        <p>
          Você está avaliando: <strong>{{ nomeAvaliado }}</strong>
        </p>
        <p v-if="papelUsuarioNaSolicitacao">Seu papel nesta solicitação: {{ papelUsuarioNaSolicitacao }}</p>
  
        <form @submit.prevent="submitAvaliacao" v-if="!avaliacaoJaFeita">
          <div class="form-group">
            <label for="nota">Nota (1 a 5):</label>
            <div class="star-rating">
              <span
                v-for="n in 5"
                :key="n"
                @click="setNota(n)"
                :class="{ 'filled': n <= novaAvaliacao.nota }"
                class="star"
              >
                ★
              </span>
            </div>
            <input type="hidden" v-model="novaAvaliacao.nota" />
            <div v-if="validationErrors.nota" class="validation-error">{{ validationErrors.nota }}</div>
          </div>
  
          <div class="form-group">
            <label for="comentario">Comentário:</label>
            <textarea
              id="comentario"
              v-model="novaAvaliacao.comentario"
              rows="4"
              maxlength="500"
            ></textarea>
          </div>
  
          <button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? 'Enviando...' : 'Enviar Avaliação' }}
          </button>
        </form>
  
        <div v-if="successMsg" class="success-message">{{ successMsg }}</div>
        <div v-if="solicitacao && avaliacaoJaFeita" class="info-message">
          Você já avaliou este serviço.
        </div>
      </div>
      <div v-if="solicitacao && !podeAvaliar && !isLoading && !errorMsg && userStore.user" class="info-message">
          Você não é um participante direto desta solicitação ou ela não está em um estado que permita avaliação.
      </div>
  
      <!-- Opcional: Mostrar avaliações existentes para o prestador -->
      <div v-if="avaliadoId && avaliacoesExistentes.length > 0 && !isLoading && userStore.user" class="avaliacoes-existentes">
          <h3>Avaliações anteriores para {{ nomeAvaliado }}:</h3>
          <ul>
              <li v-for="av in avaliacoesExistentes" :key="av.id">
                  <p><strong>Nota: {{ av.nota }}</strong> (Por: {{ av.avaliador.nome }})</p>
                  <p>{{ av.comentario }}</p>
                  <small>{{ new Date(av.data).toLocaleDateString() }}</small>
              </li>
          </ul>
      </div>
  
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import axios from 'axios'; // Ou seu HTTP client configurado
  import { useUserStore } from '@/stores/user'; // Importe seu store
  import { useToast } from 'vue-toastification';

  
  // --- Configuração ---
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'; // Use variáveis de ambiente se possível
  
  const route = useRoute();
  const router = useRouter();
  const userStore = useUserStore(); // Use o store
  const toast = useToast();
  
  // --- Estado Reativo ---
  const solicitacao = ref(null);
  const nomeAvaliado = ref('');
  const avaliadorId = ref(null);
  const avaliadoId = ref(null);
  const papelUsuarioNaSolicitacao = ref(''); // "Cliente" ou "Prestador"
  const podeAvaliar = ref(false); // Flag para controlar se o usuário logado pode avaliar
  
  const novaAvaliacao = ref({
    solicitacaoId: null,
    avaliadorId: null,
    avaliadoId: null,
    nota: 0,
    comentario: '',
    fotos: []
  });
  
  const avaliacoesExistentes = ref([]);
  const avaliacaoJaFeita = ref(false);
  
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const errorMsg = ref('');
  const successMsg = ref('');
  const validationErrors = ref({});
  const solicitacaoIdParam = ref(null);
  
  
  // --- Lógica ---
  
  const getToken = () => localStorage.getItem('token');
  
  const fetchSolicitacaoDetalhes = async (id) => {
    isLoading.value = true;
    errorMsg.value = '';
    solicitacao.value = null;
    podeAvaliar.value = false;
    avaliacaoJaFeita.value = false;
    avaliacoesExistentes.value = [];
  
    if (!userStore.user) {
      // O navigation guard já deve redirecionar, mas é uma checagem extra
      errorMsg.value = "Usuário não autenticado.";
      isLoading.value = false;
      // Tentar carregar o usuário se ele não estiver no store, mas houver token
      if(getToken()){
          await userStore.loadUser();
          if(!userStore.user){ // Se ainda não carregou, para aqui
              return;
          }
      } else { // Se não há token
          return;
      }
    }
  
    try {
      // IMPORTANTE: Seu endpoint GET /api/solicitacoes/${id} DEVE retornar:
      // {
      //   "id": solicitacaoId,
      //   "descricaoServico": "Nome/descrição do serviço", // ou campo similar
      //   "cliente": { "id": clienteId, "nome": "Nome Cliente" },
      //   "prestador": { "id": prestadorId, "nome": "Nome Prestador" },
      //   "status": "CONCLUIDA" // ou status relevante
      // }
      const response = await axios.get(`${API_BASE_URL}/solicitacoes/${id}`, {
        headers: { 'Authorization': `Bearer ${getToken()}` }
      });
      solicitacao.value = response.data;
      novaAvaliacao.value.solicitacaoId = solicitacao.value.id;
  
      // VERIFICAÇÕES DE ESTADO DA SOLICITAÇÃO (EXEMPLO)
      // Você pode querer adicionar uma lógica para só permitir avaliação se a solicitação estiver "CONCLUIDA", por exemplo.
      // if (solicitacao.value.status !== 'CONCLUIDA' && solicitacao.value.status !== 'FINALIZADA') {
      //   errorMsg.value = `Este serviço está com status "${solicitacao.value.status}" e não pode ser avaliado no momento.`;
      //   isLoading.value = false;
      //   return;
      // }
  
  
      // Determinar quem é o avaliador e quem é o avaliado
      const userIdLogado = userStore.user.id; // Pegar ID do store
      avaliadorId.value = userIdLogado;
      novaAvaliacao.value.avaliadorId = userIdLogado;
  
      if (solicitacao.value.cliente && solicitacao.value.prestador) {
        if (solicitacao.value.cliente.id === userIdLogado) {
          papelUsuarioNaSolicitacao.value = "Cliente";
          avaliadoId.value = solicitacao.value.prestador.id;
          nomeAvaliado.value = solicitacao.value.prestador.nome;
          novaAvaliacao.value.avaliadoId = solicitacao.value.prestador.id;
          podeAvaliar.value = solicitacao.value.status === 'CONCLUIDA'
          await fetchAvaliacoes(solicitacao.value.prestador.id, 'prestador');
        } else if (solicitacao.value.prestador.id === userIdLogado) {
          papelUsuarioNaSolicitacao.value = "Prestador";
          avaliadoId.value = solicitacao.value.cliente.id;
          nomeAvaliado.value = solicitacao.value.cliente.nome;
          novaAvaliacao.value.avaliadoId = solicitacao.value.cliente.id;
          podeAvaliar.value = solicitacao.value.status === 'CONCLUIDA'
          await fetchAvaliacoes(solicitacao.value.cliente.id, 'cliente');
        } else {
          errorMsg.value = "Você não é um participante direto desta solicitação de serviço.";
          podeAvaliar.value = false;
        }
      } else {
          errorMsg.value = "Detalhes do cliente ou prestador não encontrados na solicitação.";
          podeAvaliar.value = false;
      }
  
      if (podeAvaliar.value && avaliadoId.value) {
          // Checar se já avaliou esta solicitação específica
          // No backend, o ideal seria ter uma query tipo:
          // GET /api/avaliacoes/solicitacao/{solicitacaoId}/avaliador/{avaliadorId}
          // Por enquanto, vamos filtrar das avaliações carregadas
          const jaAvaliou = avaliacoesExistentes.value.find(
              av => av.avaliador.id === userIdLogado && av.solicitacao.id === solicitacao.value.id
          );
          if (jaAvaliou) {
              avaliacaoJaFeita.value = true;
              novaAvaliacao.value.nota = jaAvaliou.nota; // Preencher com a avaliação existente
              novaAvaliacao.value.comentario = jaAvaliou.comentario;
          }
      }
  
  
    } catch (err) {
      console.error("Erro ao buscar detalhes da solicitação:", err);
      if (err.response && err.response.status === 401) {
          errorMsg.value = "Sessão expirada ou token inválido. Por favor, faça login novamente.";
          userStore.logout();
          router.push('/login');
      } else if (err.response && err.response.status === 404) {
          errorMsg.value = "Solicitação de serviço não encontrada.";
      } else {
          errorMsg.value = "Não foi possível carregar os detalhes da solicitação. " + (err.response?.data?.message || err.message);
      }
    } finally {
      isLoading.value = false;
    }
  };
  
  // Renomeado para ser mais genérico, pode buscar avaliações feitas ou recebidas
  const fetchAvaliacoes = async (idUsuarioAlvo, papelDoAlvoNaAvaliacao) => {
    // idUsuarioAlvo: ID do usuário cujas avaliações estamos buscando
    // papelDoAlvoNaAvaliacao: se estamos buscando avaliações que ele recebeu como 'prestador' ou 'cliente'
    try {
      const endpoint = papelDoAlvoNaAvaliacao === 'prestador'
          ? `${API_BASE_URL}/avaliacoes/recebidas/prestador/${idUsuarioAlvo}`
          : `${API_BASE_URL}/avaliacoes/recebidas/cliente/${idUsuarioAlvo}`;
  
      const response = await axios.get(endpoint, {
        headers: { 'Authorization': `Bearer ${getToken()}` }
      });
      avaliacoesExistentes.value = response.data;
    } catch (err) {
      console.error(`Erro ao buscar avaliações para ${papelDoAlvoNaAvaliacao} ID ${idUsuarioAlvo}:`, err);
    }
  };
  
  
  const setNota = (nota) => {
    novaAvaliacao.value.nota = nota;
    if (validationErrors.value.nota) delete validationErrors.value.nota;
  };
  
  const validateForm = () => {
    validationErrors.value = {};
    if (novaAvaliacao.value.nota < 1 || novaAvaliacao.value.nota > 5) {
      validationErrors.value.nota = 'A nota deve ser entre 1 e 5.';
    }
    // Exemplo: comentário obrigatório se a nota for baixa
    // if (novaAvaliacao.value.nota <= 2 && (!novaAvaliacao.value.comentario || novaAvaliacao.value.comentario.trim() === '')) {
    //   validationErrors.value.comentario = 'Para notas baixas, um comentário é obrigatório.';
    // }
    return Object.keys(validationErrors.value).length === 0;
  }
  
  const submitAvaliacao = async () => {
    if (!validateForm() || !podeAvaliar.value || avaliacaoJaFeita.value) {
      if(avaliacaoJaFeita.value) toast.value = "Você já avaliou este serviço.";
      return;
    }
  
    isSubmitting.value = true;
    try {
      const payload = { ...novaAvaliacao.value };
      await axios.post(`${API_BASE_URL}/avaliacoes/criar`, payload);
      toast.success("Avaliação enviada com sucesso!");
      await fetchAvaliacoes(avaliadoId.value, papelUsuarioNaSolicitacao.value === "Cliente" ? 'prestador' : 'cliente');
    } catch (err) {
      toast.error("Erro ao enviar avaliação: " + (err.response?.data?.message || err.message));
    } finally {
      isSubmitting.value = false;
    }
  };
  
  // --- Hooks de Ciclo de Vida e Watchers ---
  onMounted(async () => {
    // Primeiro, garante que o usuário do store esteja carregado, se houver token
    if (getToken() && !userStore.user) {
      await userStore.loadUser();
    }
  
    solicitacaoIdParam.value = route.params.id; // Seu router usa 'id' como nome do param
    if (solicitacaoIdParam.value) {
      if (userStore.user) { // Se o usuário já está carregado, busca os detalhes
          fetchSolicitacaoDetalhes(parseInt(solicitacaoIdParam.value));
      } else if (!getToken()) { // Se não tem token, nem tenta e mostra msg de login
          isLoading.value = false; // Para de carregar para mostrar a mensagem de login
      }
      // Se tem token mas userStore.user ainda é null, o watch abaixo vai pegar
    } else {
      errorMsg.value = "ID da solicitação de serviço não fornecido na URL.";
      isLoading.value = false;
    }
  });
  
  // Observa se o usuário do store muda (ex: após o loadUser() inicial)
  // ou se o ID da rota muda (se o usuário navegar entre páginas de avaliação sem recarregar)
  watch([() => userStore.user, () => route.params.id], ([newUser, newRouteId]) => {
      if (newRouteId && newUser) {
          solicitacaoIdParam.value = newRouteId;
          fetchSolicitacaoDetalhes(parseInt(solicitacaoIdParam.value));
      } else if (newRouteId && !newUser && !getToken()){
          // Se a rota mudou para uma avaliação, mas não há usuário nem token,
          // o guard de navegação já deve ter redirecionado.
          // Aqui só garantimos que o isLoading pare se estava ativo.
          isLoading.value = false;
      }
  }, { immediate: false }); // immediate: false para não rodar na montagem inicial se o onMounted já for tratar
  
  </script>
  
  <style scoped>
  .avaliar-servico-container {
    max-width: 600px;
    margin: 20px auto;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 8px;
    font-family: sans-serif;
  }
  
  .loading-spinner, .error-message, .success-message, .info-message {
    padding: 10px;
    margin-bottom: 15px;
    border-radius: 4px;
    text-align: center;
  }
  
  .loading-spinner {
    color: #333;
  }
  
  .error-message {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
  }
  .validation-error {
    color: #dc3545;
    font-size: 0.9em;
    margin-top: 4px;
  }
  
  .success-message {
    background-color: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
  }
  
  .info-message {
    background-color: #e2e3e5;
    color: #383d41;
    border: 1px solid #d6d8db;
  }
  
  .form-group {
    margin-bottom: 15px;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
  }
  
  .star-rating {
    font-size: 2em;
    cursor: pointer;
  }
  
  .star {
    color: #ddd; /* Estrela vazia */
    margin-right: 2px;
  }
  
  .star.filled {
    color: #ffc107; /* Estrela preenchida */
  }
  
  textarea {
    width: 100%;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
    resize: vertical;
  }
  
  button {
    background-color: #007bff;
    color: white;
    padding: 10px 15px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1em;
  }
  
  button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
  }
  
  button:hover:not(:disabled) {
    background-color: #0056b3;
  }
  
  .avaliacoes-existentes {
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid #eee;
  }
  .avaliacoes-existentes h3 {
      margin-bottom: 10px;
  }
  .avaliacoes-existentes ul {
      list-style-type: none;
      padding: 0;
  }
  .avaliacoes-existentes li {
      background-color: #f9f9f9;
      border: 1px solid #eee;
      padding: 10px;
      margin-bottom: 10px;
      border-radius: 4px;
  }
  .avaliacoes-existentes li p {
      margin: 5px 0;
  }
  .avaliacoes-existentes li small {
      color: #777;
  }
  </style>