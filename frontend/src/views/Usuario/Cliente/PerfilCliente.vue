<template>
    <div>
      <h2>Perfil do Cliente</h2>
      <p>Nome: {{ cliente.nome }}</p>
      <p>Email: {{ cliente.email }}</p>
      <!-- Outros dados do cliente -->
      <button @click="editarPerfil">Editar Perfil</button>

      <div v-if="avaliacoes.length > 0" class="avaliacoes">
      <h3>Avaliações Recebidas como Cliente</h3>
      <ul>
        <li v-for="av in avaliacoes" :key="av.id">
          <p><strong>Nota: {{ av.nota }}</strong> - {{ av.comentario }}</p>
          <small>Por: {{ av.avaliador.nome }} em {{ new Date(av.data).toLocaleDateString() }}</small>
        </li>
      </ul>
    </div>

    </div>
  </template>
  
  <script>

  import api
   from '@/services/api';
  export default {
    data() {
      return {
        cliente: {
          nome: '', // Exemplo, substitua pela sua lógica de dados
          email: '',
        },
        avaliacoes: [],
      };
    },

    async mounted() {
      await this.carregarPerfil();
      await this.carregarAvaliacoes();
    },
    methods: {
      async carregarPerfil() {
        try {
          const response = await api.get('/usuarios/perfil');
          this.cliente = response.data;
        } catch (error) {
          console.error('Erro ao carregar perfil:', error);
        }
      },
      editarPerfil() {
      this.$router.push('/editar-perfil');
    },
    },
  };
  </script>